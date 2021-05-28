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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.target.ios.IOSTarget;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;
import org.robovm.idea.RoboVmPlugin;
import org.robovm.idea.running.RoboVmRunConfiguration.EntryType;

import javax.swing.*;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.robovm.idea.running.RoboVmRunConfiguration.AUTO_PROVISIONING_PROFILE;
import static org.robovm.idea.running.RoboVmRunConfiguration.AUTO_SIGNING_IDENTITY;

public class RoboVmIOSRunConfigurationSettingsEditor extends SettingsEditor<RoboVmRunConfiguration> {
    private static final Arch[] DEVICE_ARCHS = {Arch.arm64, Arch.thumbv7};
    private static final Arch[] SIMULATOR_ARCHS = {Arch.x86_64, Arch.x86, Arch.arm64};

    public static final String AUTO_SIMULATOR_IPHONE_TITLE = "Auto (prefers '" + DeviceType.PREFERRED_IPHONE_SIM_NAME + "')";
    public static final String AUTO_SIMULATOR_IPAD_TITLE = "Auto (prefers '" + DeviceType.PREFERRED_IPAD_SIM_NAME + "')";


    private JPanel panel;
    private JTabbedPane tabbedPane1;
    private JComboBox<ModuleNameDecorator> module;
    private JRadioButton attachedDeviceRadioButton;
    private JRadioButton simulatorRadioButton;
    private JComboBox<SimTypeDecorator> simType;
    private JComboBox<SigningIdentityDecorator> signingIdentity;
    private JComboBox<ProvisioningProfileDecorator> provisioningProfile;
    private JComboBox<Arch> simArch;
    private JComboBox<Arch> deviceArch;
    private JTextArea args;
    private JCheckBox pairedWatch;

    // copy of data that is time consuming to fetch (fetched only once when dialog is created)
    private List<ModuleNameDecorator> roboVmModules;
    private List<SimTypeDecorator> simDeviceTypes;
    private final SimTypeDecorator simulatorAutoIPhone = new SimTypeDecorator(AUTO_SIMULATOR_IPHONE_TITLE, EntryType.AUTO);
    private final SimTypeDecorator simulatorAutoIPad = new SimTypeDecorator(AUTO_SIMULATOR_IPAD_TITLE, EntryType.AUTO2);
    private List<ProvisioningProfileDecorator> provisioningProfiles;
    private final ProvisioningProfileDecorator provisioningProfileAuto = new ProvisioningProfileDecorator(AUTO_PROVISIONING_PROFILE, EntryType.AUTO);
    private List<SigningIdentityDecorator> signingIdentities;
    private final SigningIdentityDecorator signingIdentityAuto = new SigningIdentityDecorator(AUTO_SIGNING_IDENTITY, EntryType.AUTO);
    private boolean moduleHasWatchApp;
    // true if editor internally updating data and listeners should ignore the events
    private boolean updatingData;

    @NotNull
    @Override
    protected JComponent createEditor() {
        // populate controls with stable data
        populateDeviceArch();
        populateSigningIdentities();
        populateProvisioningProfiles();
        populateSimulators();
        roboVmModules = null; // will be populated once modules list is known

        simType.addActionListener(e -> {
            if (!updatingData) {
                updateSimArchs((SimTypeDecorator) simType.getSelectedItem());
                updatePairedWatch(false, null);
            }
        });
        module.addActionListener(e -> {
            if (!updatingData)
                updatePairedWatch(true, null);
        } );

        return panel;
    }

    @Override
    protected void resetEditorFrom(@NotNull RoboVmRunConfiguration config) {
        try {
            updatingData = true;
            module.setSelectedItem(getModuleFromConfig(config));
            simType.setSelectedItem(getSimulatorFromConfig(config));
            simArch.setSelectedItem(populateSimulatorArch((SimTypeDecorator) simType.getSelectedItem(), config.getSimulatorArch()));
            updatePairedWatch(true, config.simulatorLaunchWatch());

            deviceArch.setSelectedItem(config.getDeviceArch());
            signingIdentity.setSelectedItem(getSigningIdentityFromConfig(config));
            provisioningProfile.setSelectedItem(getProvisioningProfileFromConfig(config));
            attachedDeviceRadioButton.setSelected(config.getTargetType() == RoboVmRunConfiguration.TargetType.Device || simType.getItemCount() == 0);
            args.setText(config.getArguments());
        } finally {
            updatingData = false;
        }
    }

    @Override
    protected void applyEditorTo(@NotNull RoboVmRunConfiguration config) throws ConfigurationException {
        // validate all data
        if (module.getSelectedItem() == null)
            throw new ConfigurationException("RoboVM module is not specified!");
        if (simType.getSelectedItem() == null)
            throw buildConfigurationException("Simulator is not specified!", () -> simType.setSelectedItem(simulatorAutoIPhone));
        if (simArch.getSelectedItem() == null)
            throw buildConfigurationException("Simulator architecture is not specified!", () -> simArch.setSelectedIndex(0));
        if (deviceArch.getSelectedItem() == null)
            throw buildConfigurationException("Device architecture is not specified!", () -> deviceArch.setSelectedItem(Arch.arm64));
        if (signingIdentity.getSelectedItem() == null)
            throw buildConfigurationException("Signing identity is not specified!", () -> signingIdentity.setSelectedItem(signingIdentityAuto));
        if (provisioningProfile.getSelectedItem() == null)
            throw buildConfigurationException("Provisioning profile is not specified!", () -> provisioningProfile.setSelectedItem(provisioningProfileAuto));

        // save all data
        config.setModuleName(Decorator.from(module).name);
        config.setTargetType(attachedDeviceRadioButton.isSelected() ? RoboVmRunConfiguration.TargetType.Device : RoboVmRunConfiguration.TargetType.Simulator);
        // device related
        config.setDeviceArch((Arch) deviceArch.getSelectedItem());
        config.setSigningIdentityType(Decorator.from(signingIdentity).entryType);
        config.setSigningIdentity(Decorator.from(signingIdentity).id);
        config.setProvisioningProfileType(Decorator.from(provisioningProfile).entryType);
        config.setProvisioningProfile(Decorator.from(provisioningProfile).id);
        // simulator related
        config.setSimulatorArch((Arch) simArch.getSelectedItem());
        config.setSimulatorType(Decorator.from(simType).entryType);
        config.setSimulator(Decorator.from(simType).id);
        config.setSimulatorSdk(-1); // legacy, will not be used
        config.setArguments(args.getText());
        config.setSimulatorLaunchWatch(pairedWatch.isSelected());
    }

    private Arch populateSimulatorArch(SimTypeDecorator simulator, Arch arch) {
        Arch result = null;
        simArch.removeAllItems();
        if (simulator != null) {
            if (simulator == simulatorAutoIPad || simulator == simulatorAutoIPhone){
                // auto simulator, use default OS arch (x86_64 or arm64 on m1)is allowed, if arch doesn't match -- override
                simArch.addItem(DeviceType.DEFAULT_HOST_ARCH);
                result = DeviceType.DEFAULT_HOST_ARCH;
            } else {
                Set<Arch> simArches = simulator.data.getArchs();
                for (Arch a : SIMULATOR_ARCHS) {
                    if (simArches.contains(a)) {
                        simArch.addItem(a);
                        if (a == arch)
                            result = a;
                    }
                }
            }
        }

        return result;
    }

    private void populateDeviceArch() {
        deviceArch.removeAllItems();
        for (Arch arch : DEVICE_ARCHS)
            deviceArch.addItem(arch);
    }

    private void populateModules(@NotNull RoboVmRunConfiguration config) {
        if (roboVmModules != null)
            return;

        this.roboVmModules = RoboVmPlugin.getRoboVmModules(config.getProject(), IOSTarget.TYPE).stream()
                .map(ModuleNameDecorator::new)
                .collect(Collectors.toList());

        // populate with RoboVM Sdk modules
        this.module.removeAllItems();
        this.roboVmModules.forEach(m -> module.addItem(m));
    }

    private void populateSigningIdentities() {
        this.signingIdentities = SigningIdentity.list().stream()
                .map(SigningIdentityDecorator::new)
                .collect(Collectors.toList());

        signingIdentity.removeAllItems();
        signingIdentity.addItem(signingIdentityAuto);
        this.signingIdentities.forEach(t -> signingIdentity.addItem(t));
    }

    private void populateProvisioningProfiles() {
        this.provisioningProfiles = ProvisioningProfile.list().stream()
                .map(ProvisioningProfileDecorator::new)
                .collect(Collectors.toList());

        provisioningProfile.removeAllItems();
        provisioningProfile.addItem(provisioningProfileAuto);
        this.provisioningProfiles.forEach(t -> provisioningProfile.addItem(t));
    }

    private void populateSimulators() {
        this.simDeviceTypes = DeviceType.listDeviceTypes().stream()
                .map(SimTypeDecorator::new)
                .collect(Collectors.toList());

        simType.removeAllItems();
        simType.addItem(simulatorAutoIPhone);
        simType.addItem(simulatorAutoIPad);
        this.simDeviceTypes.forEach(t -> simType.addItem(t));
    }

    private ModuleNameDecorator getModuleFromConfig(RoboVmRunConfiguration config) {
        populateModules(config);

        // validate if module is known
        String name = config.getModuleName();
        return getMatchingDecorator(EntryType.ID, name, (ModuleNameDecorator) module.getSelectedItem(),
                null, null, roboVmModules);
    }

    private SimTypeDecorator getSimulatorFromConfig(RoboVmRunConfiguration config) {
        String name = config.getSimulator();
        return getMatchingDecorator(config.getSimulatorType(), name,
                (SimTypeDecorator) simType.getSelectedItem(),
                null, simulatorAutoIPhone, simulatorAutoIPad,
                simDeviceTypes,  t -> SimTypeDecorator.matchesName(t, config.getSimulator(), config.getSimulatorSdk()));
    }

    private SigningIdentityDecorator getSigningIdentityFromConfig(RoboVmRunConfiguration config) {
        String name = config.getSigningIdentity();
        return getMatchingDecorator(config.getSigningIdentityType(), name,
                (SigningIdentityDecorator) signingIdentity.getSelectedItem(),
                AUTO_SIGNING_IDENTITY, signingIdentityAuto, null,
                signingIdentities, t -> Decorator.matchesName(t, name));
    }

    private ProvisioningProfileDecorator getProvisioningProfileFromConfig(RoboVmRunConfiguration config) {
        String name = config.getProvisioningProfile();
        return getMatchingDecorator(config.getProvisioningProfileType(), name,
                (ProvisioningProfileDecorator) provisioningProfile.getSelectedItem(),
                AUTO_PROVISIONING_PROFILE, provisioningProfileAuto, null,
                provisioningProfiles, t -> Decorator.matchesName(t, name));
    }


    /**
     * Universal entry point for getting decorator by name or id
     * @param entryType     type of entry if it was configured, null if legacy
     * @param value         name/id (depending on entry)
     * @param selectedItem  currently selected item
     * @param autoKey       in case of legacy, identifier of auto field
     * @param auto1         decorator matching auto
     * @param auto2         same for auto 2 (e.g. autoIpad)
     * @param values           all known values (on form of decorator)
     * @param byNamePredicate  predicate to find item by name
     * @return matching decorator
     */
    private <T extends Decorator<?>> T getMatchingDecorator(EntryType entryType, String value, T selectedItem,
                                                            String autoKey, T auto1, T auto2,
                                                            List<T> values,  Predicate<T> byNamePredicate) {
        if (entryType != null) {
            // by entry type and ID
            return getMatchingDecorator(entryType, value, selectedItem, auto1, auto2, values);
        } else {
            // legacy by name
            return getMatchingDecorator(value, selectedItem, autoKey, auto1, values, byNamePredicate);
        }
    }

    /**
     * version for case when entry type is known
     */
    private <T extends Decorator<?>> T getMatchingDecorator(EntryType entryType, String id, T selectedItem, T auto1, T auto2, List<T> values) {
        T result = null;
        switch (entryType) {
            case ID:
                // looking for exact match, quick check against selected item
                if (Decorator.matchesId(selectedItem, id)) {
                    result = selectedItem;
                } else {
                    result = values.stream().filter(t -> Decorator.matchesId(t, id)).findAny().orElse(null);
                }
                break;
            case AUTO:
                result = auto1;
                break;
            case AUTO2:
                result = auto2;
                break;
        }

        return result;
    }

    /**
     * version for legacy case, matching by name only
     */
    private <T extends Decorator<?>> T getMatchingDecorator(String name, T selectedItem, String autoKey, T auto, List<T> values, Predicate<T> byNamePredicate) {
        // backward compatibility, should not happen once saved with ID
        T result = null;
        if (name != null) {
            // lookup Auto values first
            if (name.equals(autoKey))
                result = auto;
            else if (selectedItem != null && byNamePredicate.test(selectedItem)) {
                result = selectedItem;
            } else {
                result = values.stream().filter(byNamePredicate).findAny().orElse(null);
            }
        }
        return result;
    }

    private void updateSimArchs(SimTypeDecorator simulator) {
        Arch arch = populateSimulatorArch(simulator, (Arch) simArch.getSelectedItem());
        if (arch == null && simArch.getItemCount() > 0)
            arch = simArch.getItemAt(0);
        simArch.setSelectedItem(arch);
    }

    private void updatePairedWatch(boolean moduleChanged, Boolean valueToSet) {
        boolean visible;
        boolean enabled;
        boolean selected = valueToSet != null ? valueToSet : pairedWatch.isSelected();
        String text;

        if (moduleChanged) {
            // module changed
            Decorator<Module> moduleSelected = Decorator.from(module);
            Config config = moduleSelected != null ? RoboVmPlugin.loadRawModuleConfig(moduleSelected.data) : null;
            moduleHasWatchApp = config != null && config.getWatchKitApp() != null;
        }

        if (!moduleHasWatchApp) {
            selected = false;
            enabled = false;
            visible = false;
            text = "";
        } else {
            visible = true;
            Decorator<DeviceType> simSelected = Decorator.from(simType);
            if (simSelected != null && simSelected.entryType == EntryType.ID && simSelected.data.getPair() != null) {
                // has pair
                text = "Launch paired: " + simSelected.data.getPair().getDeviceName();
                enabled = true;
            } else {
                // no pair
                text = "Not paired with watch";
                enabled = false;
                selected = false;
            }
        }

        pairedWatch.setVisible(visible);
        pairedWatch.setSelected(selected);
        pairedWatch.setEnabled(enabled);
        pairedWatch.setText(text);
    }

    /**
     * helper to build exception with quick fix action
     */
    ConfigurationException buildConfigurationException(String message, Runnable quickFix) {
        ConfigurationException exc = new ConfigurationException(message);
        exc.setQuickFix(quickFix);
        return exc;
    }

    /**
     * base decorator for items with name and identifier
     *
     * @param <T> type of data
     */
    private static abstract class Decorator<T> {
        final T data;
        final String id;
        final String name;
        final EntryType entryType;

        Decorator(T data, String id, String name, EntryType entryType) {
            this.data = data;
            this.id = id;
            this.name = name;
            this.entryType = entryType;
        }

        static boolean matchesId(Decorator<?> d, String id) {
            return d != null && d.id != null && d.id.equals(id);
        }

        static boolean matchesName(Decorator<?> d, String name) {
            return d != null && d.name != null && d.name.equals(name);
        }

        static <T> Decorator<T> from(JComboBox<? extends Decorator<T>> cb) {
            //noinspection unchecked
            return (Decorator<T>) cb.getSelectedItem();
        }
    }

    /**
     * decorator for simulator type
     */
    private static class SimTypeDecorator extends Decorator<DeviceType> {
        SimTypeDecorator(String title, EntryType entryType) {
            super(null, null, title, entryType);
        }

        SimTypeDecorator(DeviceType data) {
            super(data, data.getUdid(), data.getDeviceName(), EntryType.ID);
        }

        public static boolean matchesName(SimTypeDecorator d, String name, int version) {
            return d != null && d.data != null &&
                    d.data.getVersion().versionCode == version &&
                    d.name != null && d.name.equals(name);
        }

        @Override
        public String toString() {
            return data == null ? name : data.getSimpleDeviceName() + " - " + data.getVersion();
        }
    }

    /**
     * decorator for singing identity
     */
    private static class SigningIdentityDecorator extends Decorator<SigningIdentity> {
        SigningIdentityDecorator(String title, EntryType entryType) {
            super(null, null, title, entryType);
        }

        SigningIdentityDecorator(SigningIdentity identity) {
            super(identity, identity.getFingerprint(), identity.getName(), EntryType.ID);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * decorator for provisioning profile
     */
    private static class ProvisioningProfileDecorator extends Decorator<ProvisioningProfile> {
        ProvisioningProfileDecorator(String title, EntryType entryType) {
            super(null, null, title, entryType);
        }

        ProvisioningProfileDecorator(ProvisioningProfile profile) {
            super(profile, profile.getUuid(), profile.getName(), EntryType.ID);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * decorator for module
     */
    private static class ModuleNameDecorator extends Decorator<Module> {
        ModuleNameDecorator(Module module) {
            super(module, module.getName(), module.getName(), EntryType.ID);
        }

        @Override
        public String toString() {
            return name;
        }
    }
 }
