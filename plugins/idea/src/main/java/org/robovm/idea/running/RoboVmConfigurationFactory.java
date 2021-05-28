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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RoboVmConfigurationFactory extends ConfigurationFactory {
    protected RoboVmConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new RoboVmRunConfiguration(getType(), "RoboVM Run Configuration", new RoboVmRunConfigurationSettings(project), this);
    }

    @NotNull
    @Override
    public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
    }

    @Override
    public @NotNull
    @NonNls String getId() {
        // overriding to suppress:
        // DeprecatedMethodException: The default implementation of method 'com.intellij.execution.configurations.ConfigurationFactory.getId'
        // is deprecated, you need to override it in 'class org.robovm.idea.running.RoboVmConfigurationFactory'.
        // The default implementation delegates to 'getName' which may be localized but return value of this method
        // must not depend on current localization.

        // don't have localication so just returning the name as it was before
        return getName();
    }
}
