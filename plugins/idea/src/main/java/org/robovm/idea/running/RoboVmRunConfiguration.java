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

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunProfileWithCompileBeforeLaunchOption;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.idea.RoboVmPlugin;

import java.util.Collection;
import java.util.List;

public class RoboVmRunConfiguration extends ModuleBasedConfiguration<RoboVmRunConfigurationSettings, Element> implements RunConfigurationWithSuppressedDefaultDebugAction, RunConfigurationWithSuppressedDefaultRunAction, RunProfileWithCompileBeforeLaunchOption {
    public static final String AUTO_SIGNING_IDENTITY_LEGACY = "Auto (matches 'iPhone Developer|iOS Development')";
    public static final String AUTO_SIGNING_IDENTITY = "Auto (matches 'iPhone Developer|iOS Development|Apple Development')";
    public static final String AUTO_PROVISIONING_PROFILE = "Auto";

    public enum TargetType {
        Simulator,
        Device,
        Console
    }

    // Specifies mode of signing identity, provisioning profile or simulator entry
    // null if legacy
    public enum EntryType {
        ID,     // corresponding *name field contains identifier
        AUTO,   // auto (signing identity, provisioning profile or iphone sim)
        AUTO2   // auto iPad simulator
    }

    private TargetType targetType;
    private Arch deviceArch;
    private EntryType signingIdentityType;
    private String signingIdentity;
    private EntryType provisioningProfileType;
    private String provisioningProfile;
    private Arch simulatorArch;
    private EntryType simulatorType;
    private String simulator;
    private int simulatorSdk;
    private boolean simulatorLaunchWatch;
    private String arguments;
    private String workingDir;

    // these are used to pass information between
    // the compiler, the run configuration and the
    // runner. They are not persisted.
    private boolean isDebug;
    private Config config;
    private int debugPort;
    private AppCompiler compiler;
    private ConfigurationType type;
    private List<String> programArguments;

    public RoboVmRunConfiguration(ConfigurationType type, String name, RoboVmRunConfigurationSettings configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
        this.type = type;
        this.setDefaultValues();
    }

    private void setDefaultValues() {
        if (type instanceof RoboVmIOSConfigurationType) {
            targetType = TargetType.Device;
            deviceArch = Arch.arm64;
            signingIdentityType = EntryType.AUTO;
            provisioningProfileType = EntryType.AUTO;
            simulatorType = EntryType.AUTO;
            simulatorArch = DeviceType.DEFAULT_HOST_ARCH;
            simulatorLaunchWatch = false;
        } else if (type instanceof RoboVmConsoleConfigurationType) {
            targetType = TargetType.Console;
            deviceArch = DeviceType.DEFAULT_HOST_ARCH;
        }
    }

    @Override
    public Collection<Module> getValidModules() {
        return RoboVmPlugin.getRoboVmModules(getConfigurationModule().getProject());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        if (type instanceof RoboVmIOSConfigurationType) {
            return new RoboVmIOSRunConfigurationSettingsEditor();
        } else {
            return new RoboVmConsoleRunConfigurationSettingsEditor();
        }
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new RoboVmRunProfileState(environment);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);

        targetType = valueOf(TargetType.class, JDOMExternalizerUtil.readField(element, "targetType"));
        deviceArch = valueOf(Arch.class, JDOMExternalizerUtil.readField(element, "deviceArch"));
        signingIdentityType = valueOf(EntryType.class, JDOMExternalizerUtil.readField(element, "signingIdentityType"));
        signingIdentity = JDOMExternalizerUtil.readField(element, "signingIdentity");
        provisioningProfileType = valueOf(EntryType.class, JDOMExternalizerUtil.readField(element, "provisioningProfileType"));
        provisioningProfile = JDOMExternalizerUtil.readField(element, "provisioningProfile");
        simulatorType = valueOf(EntryType.class, JDOMExternalizerUtil.readField(element, "simulatorType"));
        simulator = JDOMExternalizerUtil.readField(element, "simulatorName");
        simulatorArch = valueOf(Arch.class, JDOMExternalizerUtil.readField(element, "simArch"));
        simulatorSdk = parseInt(JDOMExternalizerUtil.readField(element, "simulatorSdk"), -1);
        simulatorLaunchWatch = parseInt(JDOMExternalizerUtil.readField(element, "simulatorLaunchPair"), -1)  > 0;
        arguments = JDOMExternalizerUtil.readField(element, "arguments", "");
        workingDir = JDOMExternalizerUtil.readField(element, "workingDir", "");

        validateAndFix();
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);

        JDOMExternalizerUtil.writeField(element, "targetType", toStringOrNull(targetType));
        JDOMExternalizerUtil.writeField(element, "deviceArch", toStringOrNull(deviceArch));
        JDOMExternalizerUtil.writeField(element, "signingIdentityType", toStringOrNull(signingIdentityType));
        JDOMExternalizerUtil.writeField(element, "signingIdentity", signingIdentity);
        JDOMExternalizerUtil.writeField(element, "provisioningProfileType", toStringOrNull(provisioningProfileType));
        JDOMExternalizerUtil.writeField(element, "provisioningProfile", provisioningProfile);
        JDOMExternalizerUtil.writeField(element, "simArch", toStringOrNull(simulatorArch));
        JDOMExternalizerUtil.writeField(element, "simulatorType", toStringOrNull(simulatorType));
        JDOMExternalizerUtil.writeField(element, "simulatorName", simulator);
        JDOMExternalizerUtil.writeField(element, "simulatorSdk", Integer.toString(simulatorSdk));
        JDOMExternalizerUtil.writeField(element, "simulatorLaunchWatch", simulatorLaunchWatch ? "1" : "0");
        JDOMExternalizerUtil.writeField(element, "arguments", arguments);
        JDOMExternalizerUtil.writeField(element, "workingDir", workingDir);
    }

    public void setDeviceArch(Arch deviceArch) {
        this.deviceArch = deviceArch;
    }

    public Arch getDeviceArch() {
        return deviceArch;
    }

    public EntryType getSigningIdentityType() {
        return signingIdentityType;
    }

    public void setSigningIdentityType(EntryType signingIdentityType) {
        this.signingIdentityType = signingIdentityType;
    }

    public void setSigningIdentity(String signingIdentity) {
        this.signingIdentity = signingIdentity;
    }

    public String getSigningIdentity() {
        return signingIdentity;
    }

    public EntryType getProvisioningProfileType() {
        return provisioningProfileType;
    }

    public void setProvisioningProfileType(EntryType provisioningProfileType) {
        this.provisioningProfileType = provisioningProfileType;
    }

    public void setProvisioningProfile(String provisioningProfile) {
        this.provisioningProfile = provisioningProfile;
    }

    public String getProvisioningProfile() {
        return provisioningProfile;
    }

    public void setSimulatorArch(Arch simulatorArch) {
        this.simulatorArch = simulatorArch;
    }

    public Arch getSimulatorArch() {
        return simulatorArch;
    }

    public EntryType getSimulatorType() {
        return simulatorType;
    }

    public void setSimulatorType(EntryType simulatorType) {
        this.simulatorType = simulatorType;
    }

    public void setSimulator(String simulator) {
        this.simulator = simulator;
    }

    public String getSimulator() {
        return simulator;
    }

    public int getSimulatorSdk() {
        return simulatorSdk;
    }

    public void setSimulatorSdk(int simulatorSdk) {
        this.simulatorSdk = simulatorSdk;
    }

    public boolean simulatorLaunchWatch() {
        return simulatorLaunchWatch;
    }

    public void setSimulatorLaunchWatch(boolean simulatorLaunchWatch) {
        this.simulatorLaunchWatch = simulatorLaunchWatch;
    }

    public String getModuleName() {
        return getConfigurationModule().getModuleName();
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setCompiler(AppCompiler compiler) {
        this.compiler = compiler;
    }

    public AppCompiler getCompiler() {
        return compiler;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public void setProgramArguments(List<String> programArguments) {
        this.programArguments = programArguments;
    }

    public List<String> getProgramArguments() {
        return programArguments;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * validates possibly wrong values and tries to fix
     */
    private void validateAndFix() {
        if (type instanceof RoboVmIOSConfigurationType) {
            if (targetType != TargetType.Device && targetType != TargetType.Simulator)
                targetType = TargetType.Device;

            if (simulatorType == EntryType.AUTO || simulatorType == EntryType.AUTO2)
                simulatorArch = DeviceType.DEFAULT_HOST_ARCH;

            // migrate simulator to new code if legacy found
            if (AUTO_SIGNING_IDENTITY_LEGACY.equals(simulator))
                simulator = AUTO_SIGNING_IDENTITY;
        } else if (type instanceof RoboVmConsoleConfigurationType) {
            // MacOsX console target
            if (deviceArch != Arch.x86_64 && deviceArch != DeviceType.DEFAULT_HOST_ARCH)
                deviceArch = DeviceType.DEFAULT_HOST_ARCH;
            targetType = TargetType.Console;
        }
    }

    //
    // Helpers
    //
    private <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        try {
            return name != null ? Enum.valueOf(enumType, name) : null;
        } catch (IllegalArgumentException ignored){
            return null;
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private String toStringOrNull(Object v) {
        return v != null ? v.toString() : null;
    }
}
