/*
 * Copyright 2016 Justin Shapcott.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.compiler.plugin.debug;

import org.robovm.compiler.CompilerException;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.plugin.LaunchPlugin;
import org.robovm.compiler.plugin.PluginArgument;
import org.robovm.compiler.plugin.PluginArguments;
import org.robovm.compiler.target.ConsoleTarget;
import org.robovm.compiler.target.LaunchParameters;
import org.robovm.compiler.target.Target;
import org.robovm.compiler.target.ios.IOSDeviceLaunchParameters;
import org.robovm.compiler.target.ios.IOSTarget;
import org.robovm.debugger.Debugger;
import org.robovm.debugger.DebuggerConfig;
import org.robovm.debugger.DebuggerException;
import org.robovm.debugger.hooks.IHooksConnection;
import org.robovm.libimobiledevice.IDeviceConnection;
import org.robovm.libimobiledevice.util.AppLauncherCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Demyan Kimitsa
 *
 * this launch plugin starts JDWP debug server in after launch phase
 * there is no direct reference to this class as it is picked up
 * from classloader.
 * Refer {@link org.robovm.compiler.AppCompiler} to find details about LauchPlugin calls
 * Refer {@link Config#loadPluginsFromClassPath()} to find how configs are loaded from classloader
 * Also corresponding entry has to be done in META-INF/services
 */
@SuppressWarnings({"unused", "JavadocReference"})
public class DebuggerLaunchPlugin extends LaunchPlugin {
    private final static String ARG_KEY_LOG_CONSOLE = "logconsole";
    private final static String ARG_KEY_SOURCE_PATH = "sourcepath";
    private final static String ARG_KEY_JDWP_PORT = "jdwpport";
    private final static String ARG_KEY_CLIENT_MODE = "clientmode";
    private final static String ARG_KEY_LOG_DIR = "logdir";

    private DebuggerConfig debuggerConfig;
    private Debugger debugger;

    @Override
    public PluginArguments getArguments() {
        // list of arguments as these passed by idea, check idea/compilation/RoboVMCompileTask
        List<PluginArgument> args = new ArrayList<>();
        args.add(new PluginArgument(ARG_KEY_LOG_CONSOLE, "Flag: enables debugger logs to console"));
        args.add(new PluginArgument(ARG_KEY_SOURCE_PATH, "Locations of source files"));
        args.add(new PluginArgument(ARG_KEY_JDWP_PORT, "TCP port JDWP server should listen or connects to"));
        args.add(new PluginArgument(ARG_KEY_CLIENT_MODE, "Flag: specifies that JDWP server shall connect instead of listening"));
        args.add(new PluginArgument(ARG_KEY_LOG_DIR, "Custom location of log directory"));
        return new PluginArguments("debug", args);
    }

    @Override
    public void beforeLaunch(Config config, LaunchParameters parameters) {
        cleanup();

        if (!config.isDebug())
            return;
        
        // fetch values passed from Idea/Eclipse
        Map<String, String> arguments = parseArguments(config);

        String logDir = argumentValue(arguments, ARG_KEY_LOG_DIR, config.getTmpDir().getAbsolutePath());
        int jdwpPort = argumentIntValue(arguments, ARG_KEY_JDWP_PORT);
        boolean jdwpClientMode = argumentValue(arguments, ARG_KEY_CLIENT_MODE, false);
        boolean logConsole = config.isDumpIntermediates() || argumentValue(arguments, ARG_KEY_LOG_CONSOLE, false);

        // common parameters to target
        parameters.getArguments().add("-rvm:EnableHooks");
        parameters.getArguments().add("-rvm:WaitForResume");

        Target target = config.getTarget();

        // now create debugger config
        DebuggerConfig.Builder builder = new DebuggerConfig.Builder();
        builder.setJdwpPort(jdwpPort);
        builder.setJdwpClienMode(jdwpClientMode);
        builder.setLogToConsole(logConsole);
        builder.setLogDir(new File(logDir));
        builder.setArch(DebuggerConfig.Arch.valueOf(target.getArch().name()));

        // make list of arguments for target
        if (ConsoleTarget.TYPE.equals(target.getType())) {
            File appDir = config.isSkipInstall() ? config.getTmpDir() : config.getInstallDir();
            builder.setAppfile(new File(appDir, config.getExecutableName()));

            File hooksPortFile;
            try {
                hooksPortFile = File.createTempFile("robovm-dbg-console", ".port");
                builder.setHooksPortFile(hooksPortFile);
            } catch (IOException e) {
                throw new CompilerException("Failed to create debugger port file", e);
            }

            parameters.getArguments().add("-rvm:PrintDebugPort=" + hooksPortFile.getAbsolutePath());
        } else if (IOSTarget.TYPE.equals(target.getType())) {
            File appDir = new File(config.isSkipInstall() ? config.getTmpDir() : config.getInstallDir(), config.getExecutableName() + ".app");
            builder.setAppfile(new File(appDir, config.getExecutableName()));

            if (IOSTarget.isSimulatorArch(config.getArch(), config.getEnv())) {
                // launching on simulator, it can write down port number to file on local system
                File hooksPortFile;
                try {
                    hooksPortFile = File.createTempFile("robovm-dbg-sim", ".port");
                    builder.setHooksPortFile(hooksPortFile);
                } catch (IOException e) {
                    throw new CompilerException("Failed to create simulator debuuger port file", e);
                }

                parameters.getArguments().add("-rvm:PrintDebugPort=" + hooksPortFile.getAbsolutePath());
            } else {
                // launching on device
                IOSDeviceLaunchParameters deviceLaunchParameters = (IOSDeviceLaunchParameters) parameters;
                DebuggerLauncherCallback callback = new DebuggerLauncherCallback();
                deviceLaunchParameters.setAppLauncherCallback(callback);
                deviceLaunchParameters.getArguments().add("-rvm:PrintDebugPort");

                // wait for hooks channel from launch callback
                builder.setHooksConnection(callback);
            }
        } else {
            throw new IllegalArgumentException("Unsupported target " + target.getType());
        }

        debuggerConfig = builder.build();
    }

    @Override
    public void afterLaunch(Config config, LaunchParameters parameters, Process process) {
        if (!config.isDebug())
            return;

        // create and start the debugger
        debugger = new Debugger(process, debuggerConfig);
        debugger.start();
    }

    @Override
    public void launchFailed(Config config, LaunchParameters parameters) {
        cleanup();
    }

    @Override
    public void cleanup() {
        // shutdown previous instance of debugger
        synchronized (this) {
            if (debugger != null) {
                debugger.shutdown();
            }

            debugger = null;
            debuggerConfig = null;
        }
    }

    private String argumentValue(Map<String, String> arguments, String key, String defaultValue) {
        String v = arguments.get(key);
        return v != null ? v : defaultValue;
    }

    private int argumentValue(Map<String, String> arguments, String key, int defaultValue) {
        String v = arguments.get(key);
        return v != null ? Integer.parseInt(v) : defaultValue;
    }

    private int argumentIntValue(Map<String, String> arguments, String key) {
        String v = arguments.get(key);
        if (v == null)
            throw new CompilerException("Missing required debugger argument " + key);

        return Integer.parseInt(v);
    }

    private boolean argumentValue(Map<String, String> arguments, String key, boolean defaultValue) {
        String v = arguments.get(key);
        return v != null ? Boolean.parseBoolean(v) : defaultValue;
    }

    boolean argumentBoolValue(Map<String, String> arguments, String key) {
        String v = arguments.get(key);
        if (v == null)
            throw new CompilerException("Missing required debugger argument " + key);

        return Boolean.parseBoolean(v);
    }

    /**
     * callback to receive hook port from device to connect debugger to.
     * device will print out [DEBUG] hooks: debugPort=
     * check hooks.c/_rvmHookSetupTCPChannel for details
     * implements hooks connection interface to provide in and out streams
     */
    private static class DebuggerLauncherCallback implements AppLauncherCallback, IHooksConnection {
        private final static String tag = "[DEBUG] hooks: debugPort=";
        private volatile Integer hooksPort;
        private IDeviceConnection deviceConnection;
        private String incompleteLine;
        private AppLauncherInfo launchInfo;


        @Override
        public void setAppLaunchInfo(AppLauncherInfo info) {
            launchInfo = info;
        }

        @Override
        public byte[] filterOutput(byte[] data) {
            if (hooksPort == null) {
                // port is not received yet, keep working
                String str = new String(data, StandardCharsets.UTF_8);
                if (incompleteLine != null) {
                    str = incompleteLine + str;
                    incompleteLine = null;
                }

                int lookingPos = 0;
                int newLineIdx = str.indexOf('\n');
                while (newLineIdx >= 0 ) {
                    // get next new line
                    if (str.startsWith(tag, lookingPos)) {
                        // got it
                        hooksPort = Integer.parseInt(str.substring(lookingPos + tag.length(), newLineIdx).trim());
                        break;
                    } else {
                        // move to next line
                        lookingPos = newLineIdx + 1;
                        newLineIdx = str.indexOf('\n', newLineIdx + 1);
                    }
                }

                // keep trailing line (without eol)
                if (hooksPort == null && lookingPos < str.length()) {
                    incompleteLine = lookingPos != 0 ? str.substring(lookingPos) : str;
                }
            }

            return data;
        }

        /**
         * waits till port hooks port is available and establish connection
         */
        @Override
        public void connect() {
            try {
                long ts = System.currentTimeMillis();
                while (hooksPort == null) {
                    if (System.currentTimeMillis() - ts > DebuggerConfig.TARGET_WAIT_TIMEOUT)
                        throw new DebuggerException("Timeout while waiting simulator port file");
                    Thread.sleep(200);
                }

                deviceConnection = launchInfo.getDevice().connect(hooksPort);
            } catch (InterruptedException e) {
                throw new DebuggerException(e);
            }
        }

        @Override
        public void disconnect() {
            deviceConnection.close();
        }

        @Override
        public InputStream getInputStream() {
            return deviceConnection.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return deviceConnection.getOutputStream();
        }
    }
}
