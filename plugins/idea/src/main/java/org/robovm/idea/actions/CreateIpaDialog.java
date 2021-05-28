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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.target.ios.IOSTarget;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;
import org.robovm.idea.RoboVmPlugin;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateIpaDialog extends DialogWrapper {
    private static final String MODULE_NAME = "robovm.ipaConfig.moduleName";
    private static final String SIGNING_IDENTITY = "robovm.ipaConfig.signingIdentity";
    private static final String PROVISIONING_PROFILE = "robovm.ipaConfig.provisioningProfile";
    private static final String ARCHS = "robovm.ipaConfig.archs";
    private static final String DESTINATION_DIR = "robovm.ipaConfig.destinationDir";

    private static final String ARCHS_ALL = "All - 32-bit (thumbv7) + 64-bit (arm64)";
    private static final String ARCHS_32BIT = "32-bit (thumbv7)";
    private static final String ARCHS_64BIT = "64-bit (arm64)";
    private JPanel panel;
    private JComboBox<String> archs;
    private JComboBox<String> signingIdentity;
    private JComboBox<String> provisioningProfile;
    private JButton browseButton;
    private JTextField destinationDir;
    private JComboBox<String> module;
    private Project project;

    protected CreateIpaDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("Create IPA");
        populateControls();
    }

    private void populateControls() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String configModule = properties.getValue(MODULE_NAME, "");
        String configSigning = properties.getValue(SIGNING_IDENTITY, "");
        String configProvisioning = properties.getValue(PROVISIONING_PROFILE, "");
        String configDestDir = properties.getValue(DESTINATION_DIR, "");
        String configArchs = properties.getValue(ARCHS, "");

        for(Module module: RoboVmPlugin.getRoboVmModules(project, IOSTarget.TYPE)) {
            this.module.addItem(module.getName());
            if(module.getName().equals(configModule)) {
                this.module.setSelectedIndex(this.module.getItemCount()-1);
            }
        }

        // populate signing identities
        for(SigningIdentity identity: SigningIdentity.list()) {
            signingIdentity.addItem(identity.getName());
            if(identity.getName().equals(configSigning)) {
                this.signingIdentity.setSelectedIndex(this.signingIdentity.getItemCount()-1);
            }
        }

        // populate provisioning profiles
        for(ProvisioningProfile profile: ProvisioningProfile.list()) {
            provisioningProfile.addItem(profile.getName());
            if(profile.getName().equals(configProvisioning)) {
                this.provisioningProfile.setSelectedIndex(this.provisioningProfile.getItemCount()-1);
            }
        }

        // populate architectures
        for(String arch: new String[] { ARCHS_ALL, ARCHS_32BIT, ARCHS_64BIT }) {
            archs.addItem(arch);
            if(arch.equals(configArchs)) {
                this.archs.setSelectedIndex(this.archs.getItemCount()-1);
            }
        }

        if(!configDestDir.isEmpty()) {
            destinationDir.setText(configDestDir);
        }

        browseButton.addActionListener(e -> {
            FileChooserDialog fileChooser = FileChooserFactory.getInstance()
                    .createFileChooser(new FileChooserDescriptor(true, false, false, false, false, false) {
                        @Override
                        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                            return file.isDirectory();
                        }

                        @Override
                        public boolean isFileSelectable(VirtualFile file) {
                            return file.isDirectory();
                        }
                    }, null, panel);
            VirtualFile[] dir = fileChooser.choose(project);
            if (dir.length > 0) {
                destinationDir.setText(dir[0].getCanonicalPath());
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if(this.module.getSelectedIndex() == -1) {
            return new ValidationInfo("No RoboVM module selected");
        }
        if(this.destinationDir.getText() == null || this.destinationDir.getText().length() == 0) {
            return new ValidationInfo("Specify a destination directory");
        }
        File destDir = new File(this.destinationDir.getText());
        if(!destDir.exists()) {
            return new ValidationInfo("Destination directory does not exist");
        }
        if(!destDir.isDirectory()) {
            return new ValidationInfo("Destination is not a directory");
        }
        if(signingIdentity.getItemCount() == 0) {
            return new ValidationInfo("No signing identity found");
        }
        if(provisioningProfile.getItemCount() == 0) {
            return new ValidationInfo("No provisioning profile found");
        }
        return null;
    }

    public CreateIpaAction.IpaConfig getIpaConfig() {
        saveProperties();
        Module module = ModuleManager.getInstance(project).findModuleByName((String) Objects.requireNonNull(this.module.getSelectedItem()));
        String signingIdentity = (String) Objects.requireNonNull(this.signingIdentity.getSelectedItem());
        String provisioningProile = (String) Objects.requireNonNull(this.provisioningProfile.getSelectedItem());
        String arch = (String) Objects.requireNonNull(this.archs.getSelectedItem());
        Arch[] archs;
        switch (arch) {
            case ARCHS_ALL:
                archs = new Arch[]{Arch.thumbv7, Arch.arm64};
                break;
            case ARCHS_32BIT:
                archs = new Arch[]{Arch.thumbv7};
                break;
            case ARCHS_64BIT:
                archs = new Arch[]{Arch.arm64};
                break;
            default:
                throw new IllegalStateException("Unknown arch configuration!");
        }
        return new CreateIpaAction.IpaConfig(module, new File(this.destinationDir.getText()), signingIdentity, provisioningProile, archs);
    }

    private void saveProperties() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(MODULE_NAME, (String)module.getSelectedItem());
        properties.setValue(SIGNING_IDENTITY, (String)signingIdentity.getSelectedItem());
        properties.setValue(PROVISIONING_PROFILE, (String)provisioningProfile.getSelectedItem());
        properties.setValue(ARCHS, (String)archs.getSelectedItem());
        properties.setValue(DESTINATION_DIR, destinationDir.getText());
    }
}
