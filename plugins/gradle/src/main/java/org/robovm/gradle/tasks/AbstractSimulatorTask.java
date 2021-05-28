/*
 * Copyright (C) 2015 RoboVM AB.
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

import java.io.File;

import org.gradle.api.GradleException;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Environment;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.target.ios.IOSSimulatorLaunchParameters;

/**
 *
 */
public abstract class AbstractSimulatorTask extends AbstractRoboVMTask {

    protected void launch(DeviceType type) {
        try {
            AppCompiler compiler = build(getOs(), getArch(), Environment.Simulator, getTargetType());

            if (extension.isSkipLaunch()) {
                return;
            }

            Config config = compiler.getConfig();
            IOSSimulatorLaunchParameters launchParameters = (IOSSimulatorLaunchParameters) config.getTarget().createLaunchParameters();
            launchParameters.setDeviceType(type);

            if (extension.getStdoutFifo() != null) {
                File stdoutFifo = new File(extension.getStdoutFifo());
                boolean isWritable;

                if (stdoutFifo.exists()) {
                    isWritable = stdoutFifo.isFile() && stdoutFifo.canWrite();
                } else {
                    File parent = stdoutFifo.getParentFile();
                    isWritable = parent != null && parent.isDirectory() && parent.canWrite();
                }

                if (!isWritable) {
                    throw new GradleException("Unwritable 'stdoutFifo' specified for RoboVM compile: " + stdoutFifo);
                }

                launchParameters.setStdoutFifo(stdoutFifo);
            }

            if (extension.getStderrFifo() != null) {
                File stderrFifo = new File(extension.getStderrFifo());
                boolean isWritable;

                if (stderrFifo.exists()) {
                    isWritable = stderrFifo.isFile() && stderrFifo.canWrite();
                } else {
                    File parent = stderrFifo.getParentFile();
                    isWritable = parent != null && parent.isDirectory() && parent.canWrite();
                }

                if (!isWritable) {
                    throw new GradleException("Unwritable 'stderrFifo' specified for RoboVM compile: " + stderrFifo);
                }

                launchParameters.setStderrFifo(stderrFifo);
            }

            compiler.launch(launchParameters);
        } catch (Throwable t) {
            throw new GradleException("Failed to launch simulator", t);
        }
    }

    protected abstract String getTargetType();

    protected abstract OS getOs();
    
    protected abstract Arch getArch();

    protected DeviceType getDeviceType(DeviceType.DeviceFamily family) {
        String deviceName = (String) project.getProperties().get("robovm.device.name");
        String sdkVersion = (String) project.getProperties().get("robovm.sdk.version");
        return DeviceType.getBestDeviceType(getArch(), family, deviceName, sdkVersion);
    }
}
