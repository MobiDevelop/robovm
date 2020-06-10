/*
 * Copyright (C) 2012 RoboVM AB
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
package org.robovm.compiler.target;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.robovm.compiler.clazz.Path;
import org.robovm.compiler.config.AppExtension;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.config.Resource;
import org.robovm.compiler.config.Resource.Walker;
import org.robovm.compiler.config.StripArchivesConfig;
import org.robovm.compiler.config.WatchKitApp;
import org.robovm.compiler.util.ToolchainUtil;
import org.simpleframework.xml.Transient;

/**
 * @author niklas
 *
 */
public abstract class AbstractTarget implements Target {
    @Transient
    protected Config config;

    protected AbstractTarget() {
    }

    @Override
    public void init(Config config) {
        this.config = config;
    }

    @Override
    public boolean canLaunch() {
        return true;
    }

    @Override
    public void prepareLaunch() throws IOException {
    }

    @Override
    public LaunchParameters createLaunchParameters() {
        return new LaunchParameters();
    }

    public String getInstallRelativeArchivePath(Path path) {
        String name = config.getArchiveName(path);
        if (path.isInBootClasspath()) {
            return "lib" + File.separator + "boot" + File.separator + name;
        }
        return "lib" + File.separator + name;
    }

    public boolean canLaunchInPlace() {
        return true;
    }

    protected List<String> getTargetExportedSymbols() {
        return Collections.emptyList();
    }

    protected List<String> getTargetCcArgs() {
        return Collections.emptyList();
    }

    protected List<String> getTargetLibs() {
        return Collections.emptyList();
    }

    public void build(List<File> objectFiles) throws IOException {
        File outFile = new File(config.getTmpDir(), config.getExecutableName());

        config.getLogger().info("Building %s binary %s", config.getTarget().getType(), outFile);

        LinkedList<String> ccArgs = new LinkedList<String>();
        LinkedList<String> libs = new LinkedList<String>();

        ccArgs.addAll(getTargetCcArgs());
        libs.addAll(getTargetLibs());

        String libSuffix = config.isUseDebugLibs() ? "-dbg" : "";

        libs.add("-lrobovm-bc" + libSuffix);
        if (config.getOs().getFamily() == OS.Family.darwin) {
            libs.add("-force_load");
            libs.add(new File(config.getOsArchDepLibDir(), "librobovm-rt" + libSuffix + ".a").getAbsolutePath());
        } else {
            libs.addAll(Arrays.asList("-Wl,--whole-archive", "-lrobovm-rt" + libSuffix, "-Wl,--no-whole-archive"));
        }
        if (config.isSkipInstall()) {
            libs.add("-lrobovm-debug" + libSuffix);
        }
        libs.addAll(Arrays.asList(
                "-lrobovm-core" + libSuffix, "-lgc" + libSuffix, "-lpthread", "-ldl", "-lm", "-lz"));
        if (config.getOs().getFamily() == OS.Family.linux) {
            libs.add("-lrt");
        }
        if (config.getOs().getFamily() == OS.Family.darwin) {
            libs.add("-liconv");
            libs.add("-lsqlite3");
            libs.add("-framework");
            libs.add("Foundation");
        }

        ccArgs.add("-L");
        ccArgs.add(config.getOsArchDepLibDir().getAbsolutePath());

        List<String> exportedSymbols = new ArrayList<String>();
        exportedSymbols.addAll(getTargetExportedSymbols());
        exportedSymbols.add("JNI_OnLoad_*");
        exportedSymbols.addAll(config.getExportedSymbols());

        if (config.getOs().getFamily() == OS.Family.linux) {
            ccArgs.add("-Wl,-rpath=$ORIGIN");
            ccArgs.add("-Wl,--gc-sections");
//            ccArgs.add("-Wl,--print-gc-sections");

            if (!exportedSymbols.isEmpty()) {
                // Create an ld version script which makes the exported symbols global
                // and all other symbols local.
                StringBuilder sb = new StringBuilder();
                sb.append("{\n    ");
                sb.append(StringUtils.join(exportedSymbols, ";\n    "));
                sb.append(";\n};\n");

                File dynamicListFile = new File(config.getTmpDir(), "exported_symbols");
                FileUtils.writeStringToFile(dynamicListFile, sb.toString());
                ccArgs.add("-Wl,--dynamic-list=" + dynamicListFile.getAbsolutePath());
            }

        } else if (config.getOs().getFamily() == OS.Family.darwin) {
            ccArgs.add("-ObjC");

            if (config.isSkipInstall()) {
                exportedSymbols.add("catch_exception_raise");
            }
            for (int i = 0; i < exportedSymbols.size(); i++) {
                // On Darwin symbols are always prefixed with a '_'. We'll prepend
                // '_' to each symbol here so the user won't have to.
                // (excluding * starting wildcards)
                String symb = exportedSymbols.get(i);
                exportedSymbols.set(i, symb.startsWith("*") ? symb : "_" + symb);
            }

            if (!config.getUnhideSymbols().isEmpty()) {
                List<String> aliasedSymbols = new ArrayList<String>();
                for (String symbol : config.getUnhideSymbols()) {
                    aliasedSymbols.add("_" + symbol + " __unhidden_" + symbol);
                }
                File aliasedSymbolsFile = new File(config.getTmpDir(), "aliased_symbols");
                FileUtils.writeLines(aliasedSymbolsFile, "ASCII", aliasedSymbols);
                ccArgs.add("-Xlinker");
                ccArgs.add("-alias_list");
                ccArgs.add("-Xlinker");
                ccArgs.add(aliasedSymbolsFile.getAbsolutePath());
                exportedSymbols.add("__unhidden_*");
            }

            File exportedSymbolsFile = new File(config.getTmpDir(), "exported_symbols");
            FileUtils.writeLines(exportedSymbolsFile, "ASCII", exportedSymbols);
            ccArgs.add("-exported_symbols_list");
            ccArgs.add(exportedSymbolsFile.getAbsolutePath());

            ccArgs.add("-Wl,-no_implicit_dylibs");
            ccArgs.add("-Wl,-dead_strip");
        }

        if (config.getOs().getFamily() == OS.Family.darwin && !config.getFrameworks().isEmpty()) {
            for (String p : config.getFrameworks()) {
                libs.add("-framework");
                libs.add(p);
            }
        }
        if (config.getOs().getFamily() == OS.Family.darwin && !config.getWeakFrameworks().isEmpty()) {
            for (String p : config.getWeakFrameworks()) {
                libs.add("-weak_framework");
                libs.add(p);
            }
        }
        if (config.getOs().getFamily() == OS.Family.darwin && !config.getFrameworkPaths().isEmpty()) {
            for (File p : config.getFrameworkPaths()) {
                ccArgs.add("-F" + p.getAbsolutePath());
            }
        }

        File swiftLibLocation = null;
        if (!config.getLibs().isEmpty()) {
            objectFiles = new ArrayList<File>(objectFiles);
            for (Config.Lib lib : config.getLibs()) {
                String p = lib.getValue();
                if (p.endsWith(".o")) {
                    objectFiles.add(new File(p));
                } else if (p.endsWith(".a")) {
                    // .a file
                    if (config.getOs().getFamily() == OS.Family.darwin) {
                        if (lib.isForce()) {
                            libs.add("-force_load");
                        }
                        libs.add(new File(p).getAbsolutePath());
                    } else {
                        if (lib.isForce()) {
                            libs.add("-Wl,--whole-archive");
                        }
                        libs.add(new File(p).getAbsolutePath());
                        if (lib.isForce()) {
                            libs.add("-Wl,--no-whole-archive");
                        }
                    }
                } else if (p.startsWith("libswift") && p.endsWith(".dylib") && ! new File(p).exists()) {
                    // workaround:
                    // if project links against static swift library it requires
                    // dynamic swift libraries to be included. and these are not automatically
                    // resolved yet, allow user to specify them in library list
                    // link via -l by removing prefix "lib" and sufix ".dylib"
                    libs.add("-l" + p.substring(3, p.length() - 6));
                    if (swiftLibLocation == null) {
                        swiftLibLocation = getSwiftDir(config);
                        ccArgs.add("-L" + swiftLibLocation.getAbsolutePath());
                    }
                } else if (p.endsWith(".dylib") || p.endsWith(".so")) {
                    libs.add(new File(p).getAbsolutePath());
                } else {
                    // link via -l if suffix is omitted
                    libs.add("-l" + p);
                }
            }
        }

        ccArgs.add("-fPIC");
        if (config.getOs() == OS.macosx) {
            if (!config.getFrameworks().contains("CoreServices")) {
                libs.add("-framework");
                libs.add("CoreServices");
            }
        } else if (config.getOs() == OS.ios) {
            if (!config.getFrameworks().contains("MobileCoreServices")) {
                libs.add("-framework");
                libs.add("MobileCoreServices");
            }
        }

        doBuild(outFile, ccArgs, objectFiles, libs);
    }

    protected void doBuild(File outFile, List<String> ccArgs, List<File> objectFiles,
            List<String> libs) throws IOException {

        ToolchainUtil.link(config, ccArgs, objectFiles, libs, outFile);
    }

    protected File getAppDir() {
        if (!config.isSkipInstall()) {
            return config.getInstallDir();
        } else {
            return config.getTmpDir();
        }
    }

    protected String getExecutable() {
        return config.getExecutableName();
    }

    protected String getBundleId() {
        return config.getMainClass() != null ? config.getMainClass() : config.getExecutableName();
    }

    @Override
    public void buildFat(Map<Arch, File> slices) throws IOException {
        File destFile = new File(config.getTmpDir(), getExecutable());
        List<File> files = new ArrayList<>(slices.values());
        if (slices.size() > 1) {
            if (config.getOs() == OS.linux) {
                throw new UnsupportedOperationException("Fat binaries are not supported when building linux binaries");
            }
            config.getLogger().info("Building fat binary for archs %s", StringUtils.join(slices.keySet()));
            ToolchainUtil.lipo(config, destFile, files);
        } else if (!files.get(0).equals(destFile)) {
            FileUtils.copyFile(files.get(0), destFile);
            destFile.setExecutable(true, false);
        }
    }

    protected void copyResources(File destDir) throws IOException {
        for (Resource res : config.getResources()) {
            res.walk(new Walker() {
                @Override
                public boolean processDir(Resource resource, File dir, File destDir) throws IOException {
                    return AbstractTarget.this.processDir(resource, dir, destDir);
                }
                @Override
                public void processFile(Resource resource, File file, File destDir)
                        throws IOException {

                    copyFile(resource, file, destDir);
                }
            }, destDir);
        }
    }

    protected void copyDynamicFrameworks(File destDir) throws IOException {
        final Set<String> swiftLibraries = new HashSet<>();
        File frameworksDir = new File(destDir, "Frameworks");

        for (String framework : config.getFrameworks()) {
            boolean isCustomFramework = false;
            File frameworkDir = null;
            for (File path : config.getFrameworkPaths()) {
                frameworkDir = new File(path, framework + ".framework");
                if (frameworkDir.exists() && frameworkDir.isDirectory()) {
                    isCustomFramework = true;
                    break;
                }
            }

            if (isCustomFramework) {
                // check if this is a dynamic framework by finding
                // at least ony dylib in the root folder
                boolean isDynamicFramework = false;
                for(File file: frameworkDir.listFiles()) {
                    if(file.isFile() && isDynamicLibrary(file)) {
                        isDynamicFramework = true;
                        break;
                    }
                }

                if(isDynamicFramework) {
                    config.getLogger().info("Copying framework %s from %s to %s", framework, frameworkDir, destDir);
                    new Resource(frameworkDir).walk(new Walker() {
                        @Override
                        public boolean processDir(Resource resource, File dir, File destDir) throws IOException {
                            return !(dir.getName().equals("Headers") || dir.getName().equals("PrivateHeaders")
                                    || dir.getName().equals("Modules") || dir.getName().equals("Versions") || dir.getName()
                                    .equals("Documentation"));
                        }

                        @Override
                        public void processFile(Resource resource, File file, File destDir) throws IOException {
                            if (!isStaticLibrary(file)) {
                                copyFile(resource, file, destDir);

                                if (isDynamicLibrary(file)) {
                                    // remove simulator and deprecated archs, also strip bitcode if not used
                                    if (config.getOs() == OS.ios && config.getArch().isArm()) {
                                        File libFile = new File(destDir, file.getName());
                                        stripExtraArches(libFile);
                                        if (!config.isEnableBitcode())
                                            stripBitcode(libFile);
                                    }

                                    // check if this dylib depends on Swift
                                    // and register those libraries to be copied
                                    // to bundle.app/Frameworks
                                    String dependencies = ToolchainUtil.otool(file);
                                    Pattern swiftLibraryPattern = Pattern.compile("libswift.+\\.dylib");
                                    Matcher matcher = swiftLibraryPattern.matcher(dependencies);
                                    while (matcher.find()) {
                                        String library = dependencies.substring(matcher.start(), matcher.end());
                                        swiftLibraries.add(library);
                                    }
                                }
                            }
                        }

                    }, frameworksDir);
                }
            }
        }

        // workaround: check if libs contain reference to swift lib
        // if project links against static swift library it requires
        // dynamic swift libraries to be included. and these are not automatically
        // resolved yet, allow user to specify them in library list
        for (Config.Lib lib : config.getLibs()) {
            String p = lib.getValue();
            if (p.startsWith("libswift") && p.endsWith(".dylib") && !new File(p).exists()) {
                swiftLibraries.add(p);
            }
        }

        // copy Swift libraries if required
        if (!swiftLibraries.isEmpty()) {
            copySwiftLibs(swiftLibraries, frameworksDir, true);
        }
    }

    protected void copyAppExtensions(File destDir) throws IOException {
        File pluginsDir = new File(destDir, "PlugIns");

        List<File> extPaths = config.getAppExtensionPaths();
        String appBundleId = getBundleId();
        for (AppExtension extensionVo : config.getAppExtensions()) {
            String extensionName = extensionVo.getNameWithExt(".appex");
            File extensionDir = findDirectoryInLocations(extensionName, extPaths);
            if (extensionDir == null)
                throw new IllegalArgumentException(String.format("Specified app extension `%s` not found in ext paths", extensionVo.getName()));

            config.getLogger().info("Copying app-extension %s from %s to %s", extensionName, extensionDir, pluginsDir);
            new Resource(extensionDir).walk(new Walker() {
                @Override
                public boolean processDir(Resource resource, File dir, File destDir) throws IOException {
                    return true;
                }

                @Override
                public void processFile(Resource resource, File file, File destDir) throws IOException {
                    copyFile(resource, file, destDir);

                    if (config.getOs() == OS.ios && config.getArch().isArm()) {
                        // remove simulator and deprecated archs, also strip bitcode if not used
                        if (isAppExtension(file)) {
                            File libFile = new File(destDir, file.getName());
                            stripExtraArches(libFile);
                            if (!config.isEnableBitcode())
                                stripBitcode(libFile);
                        }
                    }
                }
            }, pluginsDir);

            // app extension shall extend app id, e.g. if app id = com.sample.app
            // all app extensions shall have "com.sample.app." as prefix.
            // just override all app extension ids by adding extension name to app id
            // read app ext info.plist
            updateAppExtensionBundleId(new File(pluginsDir, extensionName), extensionVo, appBundleId);
        }
    }

    /**
     * finds directory in specified location list
     * @return path to directory or null if not found
     */
    protected File findDirectoryInLocations(String dirName, List<File> locations) {
        for (File path : locations) {
            File extPath = new File(path, dirName);
            if (extPath.exists() && extPath.isDirectory())
                return extPath;
        }

        return null;
    }

    /**
     * updates application extension bundle id
     * @param extensionPath  directory of extension
     * @param config         config of app extension
     * @param parentBundleId bundle id of application (parent)
     * @return updated bundle ID
     */
    protected String updateAppExtensionBundleId(File extensionPath, AppExtension config, String parentBundleId) {
        // read existing bundle id from Info.plist
        NSDictionary infoPlist;
        String appExBundleId;
        File infoPlistFile = new File(extensionPath, "Info.plist");
        try {
            infoPlist = (NSDictionary) PropertyListParser.parse(infoPlistFile);
            appExBundleId = infoPlist.get("CFBundleIdentifier").toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read bundle id of extension " + extensionPath);
        }

        if (config.getSuffix() == null) {
            // keep existing bundle id, just validate it matches parent pattern
            if (!appExBundleId.startsWith(parentBundleId + "."))
                throw new RuntimeException(String.format("AppExtension (%s) shall extend parent bundle(%s) id while skipSigning!",
                        extensionPath.getName(), parentBundleId));
        } else if (config.getSuffix() != null) {
            // update and write back
            appExBundleId = parentBundleId + "." + config.getSuffix();
            try {
                infoPlist = (NSDictionary) PropertyListParser.parse(infoPlistFile);
                infoPlist.put("CFBundleIdentifier", appExBundleId);
                PropertyListParser.saveAsXML(infoPlist, infoPlistFile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update bundle id of extension " + extensionPath);
            }
        }

        return appExBundleId;
    }

    /**
     * Copies watch app and its extensions
     * @param installDir application install dir
     */
    protected void copyWatchApp(File installDir) throws IOException {
        WatchKitApp waConfig = config.getWatchKitApp();
        if (waConfig == null)
            return;

        // just copy entire extensions
        String waName = waConfig.getWatchAppName();
        File waSrcDir = findDirectoryInLocations(waName, config.getAppExtensionPaths());
        if (waSrcDir == null)
            throw new IllegalArgumentException("WatchApp with name " + waName + " not found in app extension paths!");

        File waDestDir = new File(installDir, "Watch/" + waName);
        config.getLogger().info("Copying Watch App from %s to %s", waSrcDir, waDestDir);
        FileUtils.copyDirectory(waSrcDir, waDestDir);

        // update bundle id of application
        String waKitBundleId = updateAppExtensionBundleId(waDestDir, waConfig.getApp(), getBundleId());

        // update bundle ids of its extension
        Map<String, AppExtension> extensionsMap = new HashMap<>();
        waConfig.getExtensions().forEach(e -> extensionsMap.put(e.getNameWithExt(".appex"), e));

        // move through all extensions in directory
        File pluginsDir = new File(waDestDir, "PlugIns");
        for (File extPath : pluginsDir.listFiles()) {
            if (!extPath.isDirectory() || !extPath.getName().endsWith(".appex")) {
                config.getLogger().info("Skipping not expected file/dir '%s' in PlugIns folder: %s",
                        extPath.getName(), pluginsDir.getAbsolutePath());
                continue;

            }

            String name = extPath.getName();
            AppExtension extension = extensionsMap.get(name);
            if (extension == null) {
                extension = AppExtension.DEFAULT_RULE;
                updateAppExtensionBundleId(extPath, extension, waKitBundleId);
            }
        }
    }

    private boolean isValidSwiftDir(File swiftDir) {
        // FIXME: simplest criteria is to check for one of core libs
        return new File(swiftDir, "libswiftCore.dylib").exists();
    }

    private File getSwiftDir(Config config) throws IOException {
        String system;
        if (config.getOs() == OS.ios) {
            if (config.getArch().isArm()) {
                system = "iphoneos";
            } else {
                system = "iphonesimulator";
            }
        } else {
            system = "mac";
        }
        return getSwiftDir(system);
    }

    private File getSwiftDir(String system) throws IOException {
        // FIXME: dkimitsa: its a temporal for finding location of swift libraries
        // FIXME: as in XCode 11 these are not under swift subdir anymore (but in swift-5.0).
        // FIXME: while its a workaround and hardcode for specific swift version and proper way of
        // FIXME: finding swift library location to be used
        String[] versions = new String[]{"swift", "swift-5.0"};
        String xcodePath = ToolchainUtil.findXcodePath();
        for (String v: versions) {
            File candidate = new File(xcodePath, "Toolchains/XcodeDefault.xctoolchain/usr/lib/" + v + "/" + system);
            if (isValidSwiftDir(candidate))
                return candidate;
        }

        throw new IOException("Failed to locate Swift Library directory!");
    }

	protected void copySwiftLibs(Collection<String> swiftLibraries, File targetDir, boolean strip) throws IOException {
		File swiftDir = getSwiftDir(config);

		// dkimitsa: there is hidden dependencies possible between swift libraries.
		// e.g. one swiftLib has dependency that is not listed in included framework
		// solve this by moving through all swiftLibs and resolve their not listed dependencies
		Set<String> libsToResolve = new HashSet<>(swiftLibraries);
		Set<String> extendedSwiftLibraries = new HashSet<>(swiftLibraries);
		while (!libsToResolve.isEmpty()) {
			for (String library : new HashSet<>(libsToResolve)) {
				libsToResolve.remove(library);

				File swiftLibrary = new File(swiftDir, library);
				String dependencies = ToolchainUtil.otool(swiftLibrary);
				Pattern swiftLibraryPattern = Pattern.compile("libswift.+\\.dylib");
				Matcher matcher = swiftLibraryPattern.matcher(dependencies);
				while (matcher.find()) {
					String lib = dependencies.substring(matcher.start(), matcher.end());
					if (extendedSwiftLibraries.add(lib)) {
						// new dependency, add it heere
						libsToResolve.add(lib);
					}
				}
			}
		}

		for (String library : extendedSwiftLibraries) {
			config.getLogger().info("Copying swift lib %s from %s to %s", library, swiftDir, targetDir);
			File swiftLibrary = new File(swiftDir, library);
			FileUtils.copyFileToDirectory(swiftLibrary, targetDir);

			// don't strip if libraries goes to SwiftSupport folder
			if (strip) {
                // remove simulator and deprecated archs, also strip bitcode if not used
                if (config.getOs() == OS.ios && config.getArch().isArm()) {
                    File libFile = new File(targetDir, swiftLibrary.getName());
                    stripExtraArches(libFile);
                    if (!config.isEnableBitcode())
                        stripBitcode(libFile);
                }
            }
        }
	}

    /**
     * removes all architectures extra architectures other than binary is build for from mach-o binary (framework, lib, appext)
     */
	protected void stripExtraArches(File libFile) throws IOException {
        String archs = ToolchainUtil.lipoInfo(config, libFile);
        List<String> archesToRemove = new ArrayList<>();

        // simulator ones
        if(archs.contains(Arch.x86.getClangName())) {
            archesToRemove.add(Arch.x86.getClangName());
        }
        if(archs.contains(Arch.x86_64.getClangName())) {
            archesToRemove.add(Arch.x86_64.getClangName());
        }
        // also arm64e has to be removed since Xcode10.1
        if (archs.contains("arm64e")) {
            archesToRemove.add("arm64e");
        }

        if (!archesToRemove.isEmpty()) {
            File tmpFile = new File(libFile.getAbsolutePath() + ".tmp");
            ToolchainUtil.lipoRemoveArchs(config, libFile, tmpFile, archesToRemove.toArray(new String[0]));
            FileUtils.copyFile(tmpFile, libFile);
            tmpFile.delete();
        }
    }

    /** removes bitcode from frameworks/libraries to minimize size */
    protected void stripBitcode(File libFile) throws IOException {
        File tmpFile = new File(libFile.getAbsolutePath() + ".tmp");
        ToolchainUtil.bitcodeStrip(config, libFile, tmpFile);
        FileUtils.copyFile(tmpFile, libFile);
        tmpFile.delete();
    }

    protected boolean isDynamicLibrary(File file) throws IOException {
        String result = ToolchainUtil.file(file);
        return result.contains("shared library");
    }

    protected boolean isStaticLibrary(File file) throws IOException {
        String result = ToolchainUtil.file(file);
        return result.contains("ar archive");
    }

    protected boolean isAppExtension(File file) throws IOException {
        String result = ToolchainUtil.file(file);
        return result.contains("Mach-O 64-bit executable") || result.contains("Mach-O executable");
    }

    protected boolean processDir(Resource resource, File dir, File destDir) throws IOException {
        return true;
    }

    protected void copyFile(Resource resource, File file, File destDir) throws IOException {
        config.getLogger().info("Copying resource %s to %s", file, destDir);
        FileUtils.copyFileToDirectory(file, destDir, true);
    }

    public void install() throws IOException {
        config.getLogger().info("Installing %s binary to %s", config.getTarget().getType(), config.getInstallDir());
        config.getInstallDir().mkdirs();
        doInstall(config.getInstallDir(), config.getExecutableName(), config.getInstallDir());
    }

    @Override
    public List<Arch> getDefaultArchs() {
        return Collections.emptyList();
    }

    @Override
    public void archive() throws IOException {
        throw new UnsupportedOperationException("Archiving is not supported for this target");
    }

    protected void doInstall(File installDir, String image, File resourcesDir) throws IOException {
        if (!config.getTmpDir().equals(installDir) || !image.equals(config.getExecutableName())) {
            File destFile = new File(installDir, image);
            FileUtils.copyFile(new File(config.getTmpDir(), config.getExecutableName()), destFile);
            destFile.setExecutable(true, false);
        }
        for (File f : config.getOsArchDepLibDir().listFiles()) {
            if (f.getName().matches(".*\\.(so|dylib)(\\.1)?")) {
                FileUtils.copyFileToDirectory(f, installDir);
            }
        }
        stripArchives(installDir);
        copyResources(resourcesDir);
        copyDynamicFrameworks(installDir);
        copyAppExtensions(installDir);
        copyWatchApp(installDir);
    }

    public Process launch(LaunchParameters launchParameters) throws IOException {
        if (config.isSkipLinking()) {
            throw new IllegalStateException("Cannot skip linking if target should be run");
        }

        // Add -rvm:log=warn to command line arguments if no logging level has been set explicitly
        boolean add = true;
        for (String arg : launchParameters.getArguments()) {
            if (arg.startsWith("-rvm:log=")) {
                add = false;
                break;
            }
        }
        if (add) {
            List<String> args = new ArrayList<String>(launchParameters.getArguments());
            args.add(0, "-rvm:log=warn");
            launchParameters.setArguments(args);
        }

        Map<String, String> env = new HashMap<>(launchParameters.getEnvironment() != null
                ? launchParameters.getEnvironment() : Collections.<String, String>emptyMap());
        env.put("ROBOVM_LAUNCH_MODE", config.isDebug() ? "debug" : "release");
        launchParameters.setEnvironment(env);

        return doLaunch(launchParameters);
    }

    protected Process doLaunch(LaunchParameters launchParameters) throws IOException {
        return createLauncher(launchParameters).execAsync();
    }

    protected Launcher createLauncher(LaunchParameters launchParameters) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected Target build(Config config) {
        return this;
    }

    protected void stripArchives(File installDir) throws IOException {
        List<Path> allPaths = new ArrayList<Path>();
        allPaths.addAll(config.getClazzes().getPaths());
        allPaths.addAll(config.getResourcesPaths());
        for (Path path : allPaths) {
            File destJar = new File(installDir, getInstallRelativeArchivePath(path));
            if (!destJar.getParentFile().exists()) {
                destJar.getParentFile().mkdirs();
            }
            stripArchive(path, destJar);
        }
    }

    protected void stripArchive(Path path, File output) throws IOException {

        if (!config.isClean() && output.exists() && !path.hasChangedSince(output.lastModified())) {
            config.getLogger().info("Not creating stripped archive file %s for unchanged path %s",
                    output, path.getFile());
            return;
        }

        config.getLogger().info("Creating stripped archive file %s", output);

        ZipOutputStream out = null;
        final ArrayList<StripArchivesConfig.Pattern> patterns = config.getStripArchivesConfig().getPatterns();
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output)));

            if (path.getFile().isFile()) {
                ZipFile archive = null;
                try {
                    archive = new ZipFile(path.getFile());
                    Enumeration<? extends ZipEntry> entries = archive.entries();
                    entryLoop : while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().startsWith("META-INF/robovm/")) {
                            // Don't include anything under META-INF/robovm/
                            continue;
                        }
                        for(StripArchivesConfig.Pattern pattern : patterns){
                        	if(pattern.matches(entry.getName())){
                        		if(pattern.isInclude()){
                        			break;
                        		}
                        		continue entryLoop;
                        	}
                        }
                        ZipEntry newEntry = new ZipEntry(entry.getName());
                        newEntry.setTime(entry.getTime());
                        out.putNextEntry(newEntry);
                        InputStream in = null;
                        try {
                            in = archive.getInputStream(entry);
                            IOUtils.copy(in, out);
                            out.closeEntry();
                        } finally {
                            IOUtils.closeQuietly(in);
                        }
                    }
                } finally {
                    try {
                        archive.close();
                    } catch (Throwable t) {}
                }
            } else {
                String basePath = path.getFile().getAbsolutePath();
                @SuppressWarnings("unchecked")
                Collection<File> files = FileUtils.listFiles(path.getFile(), null, true);
                fileLoop: for (File f : files) {
                    String entryName = f.getAbsolutePath().substring(basePath.length() + 1);
                    if (entryName.startsWith("META-INF/robovm/")) {
                        // Don't include anything under META-INF/robovm/
                        continue;
                    }
                    for(StripArchivesConfig.Pattern pattern : patterns){
                    	if(pattern.matches(entryName)){
                    		if(pattern.isInclude()){
                    			break;
                    		}
                    		continue fileLoop;
                    	}
                    }
                    ZipEntry newEntry = new ZipEntry(entryName);
                    newEntry.setTime(f.lastModified());
                    out.putNextEntry(newEntry);
                    InputStream in = null;
                    try {
                        in = new FileInputStream(f);
                        IOUtils.copy(in, out);
                        out.closeEntry();
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                }
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(out);
            output.delete();
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
