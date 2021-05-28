/*
 * Copyright (C) 2014 RoboVM AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.gradle.tasks;

import org.gradle.api.GradleException;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Environment;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.LaunchParameters;
import org.robovm.compiler.target.ios.IOSTarget;
import org.robovm.compiler.target.ios.IOSDeviceLaunchParameters;

/**
 *
 * @author Junji Takakura
 */
public class IOSDeviceTask extends AbstractRoboVMTask {

    @Override
    public void invoke() {
        try {
            Arch arch = Arch.arm64;
            if (extension.getArch() != null && extension.getArch().equals(Arch.thumbv7.toString())) {
                arch = Arch.thumbv7;
            }

            AppCompiler compiler = build(OS.ios, arch, Environment.Native, IOSTarget.TYPE);
            if (extension.isSkipLaunch()) {
                return;
            }
            Config config = compiler.getConfig();
            IOSDeviceLaunchParameters launchParameters = (IOSDeviceLaunchParameters) config.getTarget().createLaunchParameters();
			String udid = (String) project.getProperties().get("robovm.device.udid");	
			if(udid != null && !udid.isEmpty()){
				launchParameters.setDeviceId(udid);
			}
            compiler.launch(launchParameters);
        } catch (Throwable t) {
            throw new GradleException("Failed to launch IOS Device", t);
        }
    }
}
