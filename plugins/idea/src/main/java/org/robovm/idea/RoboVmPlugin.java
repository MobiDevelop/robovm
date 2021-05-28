/*
 * Copyright (C) 2015 RoboVM AB
 * Copyright (C) 2018 Daniel Thommes, NeverNull GmbH, <daniel.thommes@nevernull.io>
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
package org.robovm.idea;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.util.PlatformUtils;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.robovm.compiler.Version;
import org.robovm.compiler.config.*;
import org.robovm.compiler.log.Logger;
import org.robovm.idea.compilation.RoboVmCompileTask;
import org.robovm.idea.config.RoboVmGlobalConfig;
import org.robovm.idea.sdk.RoboVmSdkType;
import org.robovm.idea.utils.RoboFileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * Provides util for the other components of the plugin such
 * as logging.
 */
public class RoboVmPlugin {
    public enum OS {
        MacOsX,
        Windows,
        Linux
    }

    static {
        if (System.getProperty("os.name").contains("Mac")) {
            os = OS.MacOsX;
        } else if (System.getProperty("os.name").contains("Windows")) {
            os = OS.Windows;
        } else if (System.getProperty("os.name").contains("Linux")) {
            os = OS.Linux;
        }
    }

    private static final String ROBOVM_TOOLWINDOW_ID = "RoboVM";
    private static OS os;
    static volatile Map<Project, ConsoleView> consoleViews = new ConcurrentHashMap<>();
    static volatile Map<Project, ToolWindow> toolWindows = new ConcurrentHashMap<>();
    static final List<UnprintedMessage> unprintedMessages = new ArrayList<>();

    /**
     * Formatter for the time stamp printed by the logger
     */
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS ");

    public static OS getOs() {
        return os;
    }

    static class UnprintedMessage {
        final String string;
        final ConsoleViewContentType type;

        public UnprintedMessage(String string, ConsoleViewContentType type) {
            this.string = string;
            this.type = type;
        }
    }

    public static void logBalloon(final Project project, final MessageType messageType, final String message) {
        UIUtil.invokeLaterIfNeeded(() -> {
            if (project != null) {
                // this may throw an exception, see #88. It appears to be a timing
                // issue
                try {
                    ToolWindowManager.getInstance(project).notifyByBalloon(ROBOVM_TOOLWINDOW_ID, messageType, message);
                } catch (Throwable t) {
                    logError(project, message, t);
                }
            }
        });
    }

    private static String getFormattedTimeStamp() {
        return LocalDateTime.now().format(formatter);
    }

    public static void logInfo(Project project, String format, Object... args) {
        log(project, ConsoleViewContentType.SYSTEM_OUTPUT, "[INFO] " + getFormattedTimeStamp() + format, args);
    }

    public static void logError(Project project, String format, Object... args) {
        log(project, ConsoleViewContentType.ERROR_OUTPUT, "[ERROR] " + getFormattedTimeStamp() + format, args);
    }

    public static void logErrorThrowable(Project project, String s, Throwable t, boolean showBalloon) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        log(project, ConsoleViewContentType.ERROR_OUTPUT, "[ERROR] %s\n%s", s, stringWriter.toString());
        if (showBalloon)
            logBalloon(project, MessageType.ERROR, s);
    }

    public static void logWarn(Project project, String format, Object... args) {
        log(project, ConsoleViewContentType.ERROR_OUTPUT, "[WARNING] " + getFormattedTimeStamp() + format, args);
    }

    public static void logDebug(Project project, String format, Object... args) {
        log(project, ConsoleViewContentType.NORMAL_OUTPUT, "[DEBUG] " + getFormattedTimeStamp() + format, args);
    }

    private static void log(final Project project, final ConsoleViewContentType type, String format, Object... args) {
        final String s = String.format(format, args) + "\n";
        UIUtil.invokeLaterIfNeeded(() -> {
            ConsoleView consoleView = project == null ? null : consoleViews.get(project);
            if (consoleView != null) {
                for (UnprintedMessage unprinted : unprintedMessages) {
                    consoleView.print(unprinted.string, unprinted.type);
                }
                unprintedMessages.clear();
                consoleView.print(s, type);
            } else {
                unprintedMessages.add(new UnprintedMessage(s, type));
                if (type == ConsoleViewContentType.ERROR_OUTPUT) {
                    System.err.print(s);
                } else {
                    System.out.print(s);
                }
            }
        });
    }

    public static Logger getLogger(final Project project) {
        return new Logger() {
            @Override
            public void debug(String s, Object... objects) {
                logDebug(project, s, objects);
            }

            @Override
            public void info(String s, Object... objects) {
                logInfo(project, s, objects);
            }

            @Override
            public void warn(String s, Object... objects) {
                logWarn(project, s, objects);
            }

            @Override
            public void error(String s, Object... objects) {
                logError(project, s, objects);
            }
        };
    }

    public static void initializeProject(final Project project) {
        // initialize our tool window to which we
        // log all messages
        UIUtil.invokeLaterIfNeeded(() -> {
            if (project.isDisposed()) {
                return;
            }
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(ROBOVM_TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "Console", true);
            toolWindow.getContentManager().addContent(content);
            toolWindow.setIcon(RoboVmIcons.ROBOVM_SMALL);
            consoleViews.put(project, consoleView);
            toolWindows.put(project, toolWindow);
            logInfo(project, "RoboVM plugin initialized");
        });

        // initialize virtual file change listener so we can
        // trigger recompiles on file saves
        BulkFileListener listener = new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                // handle the events
                compileIfChanged(events, project);
            }
        };
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, listener);
    }

    private static void compileIfChanged(List<? extends VFileEvent> events, final Project project) {
        if (!RoboVmGlobalConfig.isCompileOnSave()) {
            return;
        }

        // find out if RoboVM module was changed
        for (VFileEvent event : events) {
            VirtualFile file = event.getFile();
            if (file == null)
                continue;

            for (Module module : ModuleManager.getInstance(project).getModules()) {
                if (ModuleRootManager.getInstance(module).getFileIndex().isInContent(file) && isRoboVmModule(module)) {
                    OrderEntry orderEntry = ModuleRootManager.getInstance(module).getFileIndex().getOrderEntryForFile(file);
                    if (orderEntry != null && orderEntry.getFiles(OrderRootType.SOURCES).length != 0) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (!CompilerManager.getInstance(project).isCompilationActive()) {
                                CompileScope scope = CompilerManager.getInstance(project).createModuleCompileScope(module, true);
                                CompilerManager.getInstance(project).compile(scope, null);
                            }
                        });
                    }

                    // processed
                    return;
                }
            }
        }
    }

    public static void unregisterProject(Project project) {
        ConsoleView consoleView = consoleViews.remove(project);
        if (consoleView != null) {
            consoleView.dispose();
        }
        toolWindows.remove(project);
        ToolWindowManager.getInstance(project).unregisterToolWindow(ROBOVM_TOOLWINDOW_ID);
    }

    public static void extractSdk() {
        File sdkHome = getSdkHomeBase();
        if (!sdkHome.exists()) {
            if (!sdkHome.mkdirs()) {
                logError(null, "Couldn't create sdk dir in %s", sdkHome.getAbsolutePath());
                throw new RuntimeException("Couldn't create sdk dir in " + sdkHome.getAbsolutePath());
            }
        }
        extractArchive("robovm-dist", sdkHome);

        // create an SDK if it doesn't exist yet
        RoboVmSdkType.createSdkIfNotExists();
    }

    public static File getSdkHome() {
        return new File(getSdkHomeBase(), "robovm-" + Version.getVersion());
    }

    public static File getSdkHomeBase() {
        return new File(System.getProperty("user.home"), ".robovm-sdks");
    }

    public static Sdk getSdk() {
        RoboVmSdkType sdkType = new RoboVmSdkType();
        for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
            if (sdkType.suggestSdkName(null, null).equals(sdk.getName())) {
                return sdk;
            }
        }
        return null;
    }

    private static void extractArchive(String archive, File dest) {
        archive = "/" + archive;
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GZIPInputStream(RoboVmPlugin.class.getResourceAsStream(archive)))) {
            boolean filesWereUpdated = false;
            ArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                File f = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    RoboFileUtils.mkdirs(f);
                } else {
                    // skip extracting if file looks to be same as in archive (ts and size matches)
                    if (f.exists() && f.lastModified() == entry.getLastModifiedDate().getTime() && f.length() == entry.getSize()) {
                        continue;
                    }
                    RoboFileUtils.mkdirs(f.getParentFile());
                    try (OutputStream out = new FileOutputStream(f)) {
                        IOUtils.copy(in, out);
                    }

                    // set last modification time stamp as it was inside tar otherwise
                    // robovm will see new time stamp and will rebuild all classes that were inside jar
                    RoboFileUtils.setLastModified(f, entry.getLastModifiedDate().getTime());

                    // mark that there was a change to SDK files
                    filesWereUpdated = true;
                }
            }
            logInfo(null, "Installed RoboVM SDK %s to %s", Version.getVersion(), dest.getAbsolutePath());

            if (filesWereUpdated) {
                File cacheLog = new File(System.getProperty("user.home"), ".robovm/cache");
                logInfo(null, "Clearing ~/.robovm/cache folder due SDK files changed.");
                try {
                    FileUtils.deleteDirectory(cacheLog);
                } catch (IOException ignored) {
                }
            }

            for (File file : RoboFileUtils.listFiles(new File(getSdkHome(), "bin"))) {
                RoboFileUtils.setExecutable(file, true);
            }
        } catch (Throwable t) {
            logError(null, "Couldn't extract SDK to %s", dest.getAbsolutePath());
            throw new RuntimeException("Couldn't extract SDK to " + dest.getAbsolutePath(), t);
        }
    }

    /**
     * @return all sdk runtime libraries and their source jars
     */
    public static List<File> getSdkLibraries() {
        List<File> libs = new ArrayList<>();

        Config.Home home = getRoboVmHome();
        if (home.isDev()) {
            // ROBOVM_DEV_ROOT has been set (rtPath points to $ROBOVM_DEV_ROOT/rt/target/robovm-rt-<version>.jar).
            File rootDir = home.getRtPath().getParentFile().getParentFile().getParentFile();
            libs.add(new File(rootDir, "objc/target/robovm-objc-" + Version.getVersion() + ".jar"));
            libs.add(new File(rootDir, "objc/target/robovm-objc-" + Version.getVersion() + "-sources.jar"));
            libs.add(new File(rootDir, "cocoatouch/target/robovm-cocoatouch-" + Version.getVersion() + ".jar"));
            libs.add(new File(rootDir, "cocoatouch/target/robovm-cocoatouch-" + Version.getVersion() + "-sources.jar"));
            libs.add(new File(rootDir, "rt/target/robovm-rt-" + Version.getVersion() + ".jar"));
            libs.add(new File(rootDir, "rt/target/robovm-rt-" + Version.getVersion() + "-sources.jar"));
            libs.add(new File(rootDir, "cacerts/full/target/robovm-cacerts-full-" + Version.getVersion() + ".jar"));
        } else {
            // normal run
            File libsDir = new File(getSdkHome(), "lib");
            try {
                libs.addAll(Arrays.asList(RoboFileUtils.listFiles(libsDir, (dir, name) -> name.endsWith(".jar") && !name.contains("cacerts"))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return libs;
    }

    /**
     * @return the source jars of all runtime libraries
     */
    public static List<File> getSdkLibrariesWithoutSources() {
        List<File> libs = getSdkLibraries();
        libs.removeIf(file -> file.getName().endsWith("-sources.jar") || file.getName().endsWith("-javadoc.jar"));
        return libs;
    }

    /**
     * @return the source jars of all runtime libraries
     */
    public static List<File> getSdkLibrarySources() {
        List<File> libs = getSdkLibraries();
        libs.removeIf(file -> !file.getName().endsWith("-sources.jar"));
        return libs;
    }

    public static Config.Home getRoboVmHome() {
        try {
            return Config.Home.find();
        } catch (Throwable t) {
            return new Config.Home(getSdkHome());
        }
    }

    public static List<Module> getRoboVmModules(Project project) {
        return getRoboVmModules(project, null);
    }

    public static List<Module> getRoboVmModules(Project project, String targetType) {
        List<Module> validModules = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            if (!isRoboVmModule(module))
                continue;

            // dkimitsa: if target type is specified return only matching modules. E.g. don't allow to run Framework
            // target in Console runner
            if (targetType != null) {
                Config config = loadRawModuleConfig(module);
                if (config == null)
                    continue;
                if (!targetType.equals(config.getTargetType()))
                    continue;
            }
            validModules.add(module);
        }
        return validModules;
    }

    public static boolean isRoboVmModule(Module module) {
        // HACK! to identify if the module uses a robovm sdk
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (sdk != null && sdk.getSdkType().getName().toLowerCase().contains("robovm")) {
            return true;
        }

        // check if there's any RoboVM RT libs in the classpath
        OrderEnumerator classes = ModuleRootManager.getInstance(module).orderEntries().recursively().withoutSdk().compileOnly();
        for (String path : classes.getPathsList().getPathList()) {
            if (isSdkLibrary(path)) {
                return true;
            }
        }

        // check if there's a robovm.xml file in the root of the module
        for (VirtualFile file : ModuleRootManager.getInstance(module).getContentRoots()) {
            if (file.findChild("robovm.xml") != null) {
                return true;
            }
        }

        return false;
    }

    public static Config loadRawModuleConfig(Module module) {
        for (VirtualFile file : ModuleRootManager.getInstance(module).getContentRoots()) {
            if (file.findChild("robovm.xml") != null) {
                try {
                    File contentRoot = new File(file.getPath());
                    return Config.loadRawConfig(contentRoot);
                } catch (IOException ignored) {
                }
            }
        }

        return null;
    }


    public static void focusToolWindow(final Project project) {
        UIUtil.invokeLaterIfNeeded(() -> {
            ToolWindow toolWindow = toolWindows.get(project);
            if (toolWindow != null) {
                toolWindow.show(() -> {
                });
            }
        });
    }

    public static File getModuleLogDir(Module module) {
        File logDir = new File(getModuleBaseDir(module), "robovm-build/logs/");
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                throw new RuntimeException("Couldn't create log dir '" + logDir.getAbsolutePath() + "'");
            }
        }
        return logDir;
    }

    public static File getModuleXcodeDir(Module module) {
        File buildDir = new File(getModuleBaseDir(module), "robovm-build/xcode/");
        if (!buildDir.exists()) {
            if (!buildDir.mkdirs()) {
                throw new RuntimeException("Couldn't create build dir '" + buildDir.getAbsolutePath() + "'");
            }
        }
        return buildDir;
    }

    public static File getModuleBuildDir(Module module, String runConfigName, org.robovm.compiler.config.OS os, Arch arch, Environment env) {
        File buildDir = new File(getModuleBaseDir(module), "robovm-build/tmp/" + runConfigName + "/" + os + "/" + arch +
                env.asLlvmSuffix("-"));
        if (!buildDir.exists()) {
            if (!buildDir.mkdirs()) {
                throw new RuntimeException("Couldn't create build dir '" + buildDir.getAbsolutePath() + "'");
            }
        }
        return buildDir;
    }

    public static File getModuleClassesDir(String moduleBaseDir) {
        File classesDir = new File(moduleBaseDir, "robovm-build/classes/");
        if (!classesDir.exists()) {
            if (!classesDir.mkdirs()) {
                throw new RuntimeException("Couldn't create classes dir '" + classesDir.getAbsolutePath() + "'");
            }
        }
        return classesDir;
    }

    public static File getModuleBaseDir(Module module) {
        File root = new File(ModuleRootManager.getInstance(module).getContentRoots()[0].getPath());
        while (root.exists()) {
            File robovmXml = new File(root, "robovm.xml");
            if (robovmXml.exists()) break;
            if (root.getParent() != null) root = root.getParentFile();
            else break;
        }
        return root;
    }

    public static Set<File> getModuleResourcePaths(Module module) {
        try {
            File moduleBaseDir = getModuleBaseDir(module);
            Config.Builder configBuilder = new Config.Builder();
            configBuilder.home(RoboVmPlugin.getRoboVmHome());
            configBuilder.addClasspathEntry(new File(".")); // Fake a classpath to make Config happy
            configBuilder.skipLinking(true);
            RoboVmCompileTask.loadConfig(module.getProject(), configBuilder, moduleBaseDir, false);
            Config config = configBuilder.build();
            Set<File> paths = new HashSet<>();
            for (Resource r : config.getResources()) {
                if (r.getPath() != null) {
                    if (r.getPath().exists() && r.getPath().isDirectory()) {
                        paths.add(r.getPath());
                    }
                } else if (r.getDirectory() != null) {
                    if (r.getDirectory().exists() && r.getDirectory().isDirectory()) {
                        paths.add(r.getDirectory());
                    }
                }
            }
            return paths;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSdkLibrary(String path) {
        String name = new File(path).getName();

        return name.startsWith("robovm-rt") ||
                name.startsWith("robovm-objc") ||
                name.startsWith("robovm-cocoatouch");
    }

    public static boolean isBootClasspathLibrary(File path) {
        return path.getName().startsWith("robovm-rt");
    }

    /**
     * finds out if plugin is running in android studio
     */
    public static boolean isAndroidStudio() {
        return "AndroidStudio".equals(PlatformUtils.getPlatformPrefix());
    }
}
