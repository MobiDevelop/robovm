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
package org.robovm.idea.running;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.target.ConsoleTarget;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.idea.RoboVmPlugin;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;

public class RoboVmConsoleRunConfigurationSettingsEditor extends SettingsEditor<RoboVmRunConfiguration> {
    private JComboBox<String> module;
    private JPanel panel;
    private JTextArea args;
    private JTextField workingDir;
    private JButton browseButton;
    private JComboBox<Arch> deviceArch;

    @Override
    protected void resetEditorFrom(@NotNull RoboVmRunConfiguration config) {
        populateControls(config);
    }

    @Override
    protected void applyEditorTo(@NotNull RoboVmRunConfiguration config) throws ConfigurationException {
        if (module.getSelectedItem() == null)
            throw new ConfigurationException("RoboVM module is not specified!");
        if (deviceArch.getSelectedItem() == null)
            throw buildConfigurationException("Device architecture is not specified!", () -> deviceArch.setSelectedItem(DeviceType.DEFAULT_HOST_ARCH));
        config.setModuleName(module.getSelectedItem().toString());
        config.setDeviceArch((Arch)deviceArch.getSelectedItem());
        config.setTargetType(RoboVmRunConfiguration.TargetType.Console);
        config.setArguments(args.getText());
        config.setWorkingDir(workingDir.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

    private void populateControls(final RoboVmRunConfiguration config) {
        // populate with RoboVM Sdk modules
        this.module.removeAllItems();
        List<Module> roboVmModules = RoboVmPlugin.getRoboVmModules(config.getProject(), ConsoleTarget.TYPE);
        roboVmModules.sort(Comparator.comparing(Module::getName));
        for(Module module: roboVmModules) {
            this.module.addItem(module.getName());
            if(module.getName().equals(config.getModuleName())) {
                this.module.setSelectedIndex(this.module.getItemCount() - 1);
            }
        }

        // populate arch
        deviceArch.removeAllItems();
        if (DeviceType.DEFAULT_HOST_ARCH == Arch.arm64)
            deviceArch.addItem(Arch.arm64);
        deviceArch.addItem(Arch.x86_64);
        if (config.getDeviceArch() == Arch.x86_64 || config.getDeviceArch() == DeviceType.DEFAULT_HOST_ARCH)
            deviceArch.setSelectedItem(config.getDeviceArch());

        this.args.setText(config.getArguments());
        String dir = config.getWorkingDir();
        if(dir == null || dir.trim().isEmpty()) {
            dir = config.getProject().getBasePath();
        }
        this.workingDir.setText(dir);
        this.browseButton.addActionListener(e -> {
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
            VirtualFile[] dir1 = fileChooser.choose(config.getProject());
            if(dir1.length > 0) {
                workingDir.setText(dir1[0].getCanonicalPath());
            }
        });
    }

    /**
     * helper to build exception with quick fix action
     */
    ConfigurationException buildConfigurationException(String message, Runnable quickFix) {
        ConfigurationException exc = new ConfigurationException(message);
        exc.setQuickFix(quickFix);
        return exc;
    }
}
