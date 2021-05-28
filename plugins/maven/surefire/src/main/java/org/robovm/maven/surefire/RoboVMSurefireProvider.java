/*
 * Copyright (C) 2014 RoboVM AB
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
package org.robovm.maven.surefire;

import org.apache.commons.exec.CommandLine;
import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.plugin.surefire.log.api.PrintStreamLogger;
import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.common.junit4.JUnit4RunListenerFactory;
import org.apache.maven.surefire.common.junit4.JUnit4TestChecker;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.*;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestListResolver;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.RunOrderCalculator;
import org.apache.maven.surefire.util.ScanResult;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.Version;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.Home;
import org.robovm.compiler.config.Environment;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.target.LaunchParameters;
import org.robovm.compiler.target.ios.DeviceType;
import org.robovm.compiler.target.ios.IOSSimulatorLaunchParameters;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;
import org.robovm.junit.client.TestClient;
import org.robovm.maven.resolver.RoboVMResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class RoboVMSurefireProvider extends AbstractProvider {
    private final static String PROP_LOG_DEBUG = "robovm.test.enableDebugLogging";
    private final static String PROP_SERVER_DEBUG = "robovm.test.enableServerLogging";
    private final static String PROP_OS = "robovm.test.os";
    private final static String PROP_ARCH = "robovm.test.arch";
    private final static String PROP_ENVIRONMENT = "robovm.test.environment";
    private final static String PROP_CONFIG_FILE = "robovm.test.configFile";
    private final static String PROP_PROPERTIES_FILE = "robovm.test.propertiesFile";
    private final static String PROP_IOS_SIGNING_IDENTITY = "robovm.test.iosSignIdentity";
    private final static String PROP_IOS_PROVISIONING_PROFILE = "robovm.test.iosProvisioningProfile";
    private final static String PROP_IOS_SKIP_SIGNING = "robovm.test.iosSkipSigning";
    private final static String PROP_IOS_SIMULATOR_NAME = "robovm.test.device.name";
    private final static String PROP_CACHE_DIR = "robovm.test.cacheDir";
    private final static String PROP_RUN_ARGS = "robovm.test.runArgs";

    private final ClassLoader testClassLoader;
    private final List<org.junit.runner.notification.RunListener> customRunListeners;
    private final JUnit4TestChecker jUnit4TestChecker;
    private final TestListResolver testResolver;
    private final ProviderParameters providerParameters;
    private final RunOrderCalculator runOrderCalculator;
    private final ScanResult scanResult;
    private TestsToRun testsToRun;

    public RoboVMSurefireProvider(ProviderParameters booterParameters) {
        providerParameters = booterParameters;
        testClassLoader = booterParameters.getTestClassLoader();
        scanResult = booterParameters.getScanResult();
        runOrderCalculator = booterParameters.getRunOrderCalculator();
        customRunListeners = JUnit4RunListenerFactory.
                createCustomListeners(booterParameters.getProviderProperties().get("listener"));
        jUnit4TestChecker = new JUnit4TestChecker(testClassLoader);
        testResolver = booterParameters.getTestRequest().getTestListResolver();
    }

    @Override
    public Iterable<Class<?>> getSuites() {
        testsToRun = scanClassPath();
        return Arrays.asList(testsToRun.getLocatedClasses());
    }

    private TestsToRun scanClassPath() {
        final TestsToRun scannedClasses = scanResult.applyFilter(jUnit4TestChecker, testClassLoader);
        return runOrderCalculator.orderTestClasses(scannedClasses);
    }

    @Override
    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException {
        if (testsToRun == null) {
            if (forkTestSet instanceof TestsToRun) {
                testsToRun = (TestsToRun) forkTestSet;
            } else if (forkTestSet instanceof Class) {
                testsToRun = TestsToRun.fromClass((Class<?>) forkTestSet);
            } else {
                testsToRun = scanClassPath();
            }
        }

        final ReporterFactory reporterFactory = providerParameters.getReporterFactory();
        final RunListener reporter = reporterFactory.createReporter();
        final ConsoleLogger consoleLogger = (reporter instanceof ConsoleLogger)
                ? (ConsoleLogger) reporter : new PrintStreamLogger(System.out);
        ConsoleOutputCapture.startCapture((ConsoleOutputReceiver) reporter);
        final JUnit4RunListener jUnit4TestSetReporter = new JUnit4RunListener(reporter);
        Result result = new Result();
        final RunNotifier runNotifier = getRunNotifier(jUnit4TestSetReporter, result, customRunListeners);

        TestClient testClient = new TestClient();
        testClient.setRunListener(new org.junit.runner.notification.RunListener() {
            public void testRunStarted(Description description) throws Exception {
                runNotifier.fireTestRunStarted(description);
            }
            public void testRunFinished(Result result) throws Exception {
                runNotifier.fireTestRunFinished(result);
            }
            public void testStarted(Description description) throws Exception {
                runNotifier.fireTestStarted(description);
            }
            public void testFinished(Description description) throws Exception {
                runNotifier.fireTestFinished(description);
            }
            public void testFailure(Failure failure) throws Exception {
                runNotifier.fireTestFailure(failure);
            }
            public void testAssumptionFailure(Failure failure) {
                runNotifier.fireTestAssumptionFailed(failure);
            }
            public void testIgnored(Description description) throws Exception {
                runNotifier.fireTestIgnored(description);
            }
        });

        String runArgs = System.getProperty(PROP_RUN_ARGS, "");
        if (!runArgs.isEmpty()) {
            testClient.setRunArgs(
                    new ArrayList<>(
                            Arrays.asList(
                                    CommandLine.parse("cmd " + runArgs).getArguments())));
        }
        
        Process process = null;
        try {
            Config config = testClient.configure(createConfig(consoleLogger), isIOS()).build();
            config.getLogger().info("Building RoboVM tests for: %s (%s%s)", config.getOs(), config.getArch(), config.getEnv().asLlvmSuffix("-"));
            config.getLogger().info("This could take a while, especially the first time round");
            AppCompiler appCompiler = new AppCompiler(config);
            appCompiler.build();
            
            LaunchParameters launchParameters = config.getTarget().createLaunchParameters();
            if (Boolean.getBoolean(PROP_SERVER_DEBUG)) {
                launchParameters.getArguments().add("-rvm:Drobovm.debug=true");
            }
            if (System.getProperty(PROP_IOS_SIMULATOR_NAME) != null && launchParameters instanceof IOSSimulatorLaunchParameters) {
                DeviceType type = DeviceType.getDeviceType(System.getProperty(PROP_IOS_SIMULATOR_NAME));
                ((IOSSimulatorLaunchParameters) launchParameters).setDeviceType(type);
            } else if(launchParameters instanceof IOSSimulatorLaunchParameters) {
                ((IOSSimulatorLaunchParameters) launchParameters).setDeviceType(DeviceType.getBestDeviceType(config.getArch(), null, null, null));
            }
            process = appCompiler.launchAsync(launchParameters);
            
            runNotifier.fireTestRunStarted(null);
            for (Class<?> clazz : testsToRun) {
                executeTestSet(testClient, clazz, reporter, runNotifier);
            }
            testClient.terminate();
            process.waitFor();
            runNotifier.fireTestRunFinished(result);
            JUnit4RunListener.rethrowAnyTestMechanismFailures(result);
        } catch (Throwable t) {
            throw new RuntimeException("RoboVM test run failed", t);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return reporterFactory.close();
    }

    private boolean isIOS() {
        if (System.getProperty(PROP_OS) != null) {
            return OS.valueOf(System.getProperty(PROP_OS)) == OS.ios;
        } else {
            return false;
        }
    }

    private String[] testToRunToClassPatterns(Class<?> clazz) throws Throwable {
        List<String> result = new ArrayList<>();
        if (!testResolver.isWildcard() && !testResolver.isEmpty()) {
            AllDefaultPossibilitiesBuilder b = new AllDefaultPossibilitiesBuilder(false);
            Runner runner = b.runnerForClass(clazz);
            for (Description d : runner.getDescription().getChildren()) {
                if (testResolver.shouldRun(d.getClassName(), d.getMethodName())) {
                    result.add(d.getClassName() + "#" + d.getMethodName());
                }
            }
        } else {
            result.add(clazz.getName());
        }
        return result.toArray(new String[result.size()]);
    }

    private void executeTestSet(TestClient testClient, Class<?> clazz, RunListener reporter, RunNotifier listeners)
            throws ReporterException, TestSetFailedException {

        final TestSetReportEntry report = new SimpleReportEntry(this.getClass().getName(), clazz.getName());
        reporter.testSetStarting(report);

        try {
            testClient.runTests(testToRunToClassPatterns(clazz)).flush();
        } catch (Throwable e) {
            reporter.testError(SimpleReportEntry.withException(report.getSourceName(), report.getName(),
                    new PojoStackTraceWriter(report.getSourceName(),
                            report.getName(), e)));
        } finally {
            reporter.testSetCompleted(report);
        }
    }

    private Config.Builder createConfig(final ConsoleLogger consoleLogger) throws IOException {
        Config.Builder configBuilder = new Config.Builder();

        final Logger logger = new Logger() {
            public void debug(String format, Object... args) {
                if (Boolean.getBoolean(PROP_LOG_DEBUG)) {
                    consoleLogger.debug(String.format(format, args));
                }
            }
            public void info(String format, Object... args) {
                consoleLogger.info(String.format(format, args));
            }
            public void warn(String format, Object... args) {
                consoleLogger.warning(String.format(format, args));
            }
            public void error(String format, Object... args) {
                consoleLogger.error(String.format(format, args));
            }
        };
        configBuilder.logger(logger);
        
        RoboVMResolver roboVMResolver = new RoboVMResolver();
        roboVMResolver.setLogger(new org.robovm.maven.resolver.Logger() {
            public void info(String logLine) {
                logger.info(logLine);
            }
            public void debug(String logLine) {
                logger.debug(logLine);
            }
        });

        Home home = null;
        try {
            home = Home.find();
        } catch (Throwable t) {}
        if (home == null || !home.isDev()) {
            home = new Home(roboVMResolver.resolveAndUnpackRoboVMDistArtifact(Version.getVersion()));
        }
        configBuilder.home(home);
        if (home.isDev()) {
            configBuilder.useDebugLibs(Boolean.getBoolean("robovm.useDebugLibs"));
            configBuilder.dumpIntermediates(true);
        }

        File basedir = new File(System.getProperty("basedir"));
        if (System.getProperties().containsKey(PROP_PROPERTIES_FILE)) {
            File propertiesFile = new File(System.getProperty(PROP_PROPERTIES_FILE));
            if (!propertiesFile.exists()) {
                throw new FileNotFoundException("Failed to find specified "
                        + PROP_PROPERTIES_FILE + ": " + propertiesFile.getAbsolutePath());
            }
            logger.debug("Loading RoboVM config properties from " 
                    + propertiesFile.getAbsolutePath());
            configBuilder.addProperties(propertiesFile);
        } else {
            configBuilder.readProjectProperties(basedir, true);
        }

        if (System.getProperties().containsKey(PROP_CONFIG_FILE)) {
            File configFile = new File(System.getProperty(PROP_CONFIG_FILE));
            if (!configFile.exists()) {
                throw new FileNotFoundException("Failed to find specified "
                        + PROP_CONFIG_FILE + ": " + configFile.getAbsolutePath());
            }
            logger.debug("Loading RoboVM config from " + configFile.getAbsolutePath());
            configBuilder.read(configFile);
        } else {
            configBuilder.readProjectConfig(basedir, true);
        }
        
        if (System.getProperty(PROP_OS) != null) {
            configBuilder.os(OS.valueOf(System.getProperty(PROP_OS)));
        }
        if (System.getProperty(PROP_ARCH) != null) {
            configBuilder.arch(Arch.valueOf(System.getProperty(PROP_ARCH)));
        }
        if (System.getProperty(PROP_ENVIRONMENT) != null) {
            configBuilder.env(Environment.valueOf(System.getProperty(PROP_ENVIRONMENT)));
        }
        if (Boolean.getBoolean(PROP_IOS_SKIP_SIGNING)) {
            configBuilder.iosSkipSigning(true);
        } else {
            if (System.getProperty(PROP_IOS_SIGNING_IDENTITY) != null) {
                String iosSignIdentity = System.getProperty(PROP_IOS_SIGNING_IDENTITY);
                logger.debug("Using explicit iOS Signing identity: " + iosSignIdentity);
                configBuilder.iosSignIdentity(SigningIdentity.find(
                        SigningIdentity.list(), iosSignIdentity));
            }
            if (System.getProperty(PROP_IOS_PROVISIONING_PROFILE) != null) {
                String iosProvisioningProfile = System.getProperty(PROP_IOS_PROVISIONING_PROFILE);
                logger.debug("Using explicit iOS provisioning profile: " + iosProvisioningProfile);
                configBuilder.iosProvisioningProfile(ProvisioningProfile.find(
                        ProvisioningProfile.list(), iosProvisioningProfile));
            }
        }
        
        if (System.getProperty(PROP_CACHE_DIR) != null) {
            File cacheDir = new File(System.getProperty(PROP_CACHE_DIR));
            logger.debug("Using explicit cache dir: " + cacheDir);
            configBuilder.cacheDir(cacheDir);
        }

        // Ignore any classpath entries in the loaded robovm.xml file.
        configBuilder.clearClasspathEntries();
        
        configBuilder.addClasspathEntry(roboVMResolver.resolveArtifact("com.mobidevelop.robovm:robovm-junit-server:" + Version.getVersion()).asFile());
        if(isIOS()) {
            configBuilder.addClasspathEntry(roboVMResolver.resolveArtifact("com.mobidevelop.robovm:robovm-rt:" + Version.getVersion()).asFile());
            configBuilder.addClasspathEntry(roboVMResolver.resolveArtifact("com.mobidevelop.robovm:robovm-objc:" + Version.getVersion()).asFile());
            configBuilder.addClasspathEntry(roboVMResolver.resolveArtifact("com.mobidevelop.robovm:robovm-cocoatouch:" + Version.getVersion()).asFile());
        }
        for (String p : System.getProperty("java.class.path").split(File.pathSeparator)) {
            configBuilder.addClasspathEntry(new File(p));
        }

        if (testClassLoader.getClass().getName().equals("org.apache.maven.surefire.booter.IsolatedClassLoader")) {
            // Not fork mode. We need to get to the URLs of the IsolatedClassLoader.
            // Only way is to use reflection.
            try {
                Field f = testClassLoader.getClass().getDeclaredField("urls");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                Collection<URL> urls = (Collection<URL>) f.get(testClassLoader);
                for (URL url :urls) {
                    File file = new File(url.getFile());
                    if (file.isDirectory() || file.getName().toLowerCase().matches(".*\\.(jar|zip)$")) {
                        configBuilder.addClasspathEntry(file);
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get classpath URLs from IsolatedClassLoader using reflection", t);
            }
        } else {
            Map<String, String> props = providerParameters.getProviderProperties();
            for (int i = 0; true; i++) {
                String path = props.get("classPathUrl." + i);
                if (path == null) {
                    break;
                }
                configBuilder.addClasspathEntry(new File(path));
            }
        }
        
        for (Class<?> c : testsToRun.getLocatedClasses()) {
            configBuilder.addForceLinkClass(c.getName());
        }
        configBuilder.skipInstall(true);

        return configBuilder;
    }
    
    private RunNotifier getRunNotifier(org.junit.runner.notification.RunListener main, Result result,
            List<org.junit.runner.notification.RunListener> others) {

        RunNotifier notifier = new RunNotifier();
        notifier.addListener(main);
        notifier.addListener(result.createListener());
        for (org.junit.runner.notification.RunListener listener : others) {
            notifier.addListener(listener);
        }
        return notifier;
    }
}
