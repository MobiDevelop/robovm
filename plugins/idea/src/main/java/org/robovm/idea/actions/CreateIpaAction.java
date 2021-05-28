/*
 * Copyright (C) 2015 RoboVM AB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
package org.robovm.idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.task.ProjectTaskManager;
import org.jetbrains.annotations.NotNull;
import org.robovm.compiler.config.Arch;
import org.robovm.idea.RoboVmPlugin;
import org.robovm.idea.compilation.RoboVmCompileTask;

import java.io.File;
import java.util.List;

public class CreateIpaAction extends AnAction {
    public static final Key<IpaConfig> IPA_CONFIG_KEY = Key.create("IPA_CONFIG");

    public void actionPerformed(final AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            RoboVmPlugin.logBalloon(null, MessageType.ERROR, "Oops. Project is missing.");
            return;
        }

        final CreateIpaDialog dialog = new CreateIpaDialog(e.getProject());
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            // create IPA
            IpaConfig ipaConfig = dialog.getIpaConfig();
            ProjectTaskManager.getInstance(project)
                    .build(ipaConfig.module)
                    .onSuccess(r -> {
                        if (r.hasErrors()) {
                            RoboVmPlugin.logInfo(project, "IPA creation failed due errors");
                        } else if (r.isAborted()) {
                            RoboVmPlugin.logInfo(project, "IPA creation aborted");
                        } else
                            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building IPA ", true) {
                                @Override
                                public void run(@NotNull ProgressIndicator progressIndicator) {
                                    try {
                                        RoboVmCompileTask.compileForIpa(project, progressIndicator, ipaConfig);
                                    } catch (Exception e) {
                                        RoboVmPlugin.logErrorThrowable(project, "Couldn't create IPA", e, false);
                                    }
                                }
                            });
                    });
        }
    }

    public static class IpaConfig {
        private final Module module;
        private final String signingIdentity;
        private final String provisioningProfile;
        private final Arch[] archs;
        private final File destinationDir;

        public IpaConfig(Module module, File destinationDir, String signingIdentity, String provisioningProile, Arch[] archs) {
            this.module = module;
            this.destinationDir = destinationDir;
            this.signingIdentity = signingIdentity;
            this.provisioningProfile = provisioningProile;
            this.archs = archs;
        }

        public Module getModule() {
            return module;
        }

        public String getSigningIdentity() {
            return signingIdentity;
        }

        public String getProvisioningProfile() {
            return provisioningProfile;
        }

        public Arch[] getArchs() {
            return archs;
        }

        public File getDestinationDir() {
            return destinationDir;
        }
    }
}
