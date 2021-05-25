/*
 * Copyright (C) 2012 RoboVM AB
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
package org.robovm.compiler.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.robovm.compiler.DependencyGraph;
import org.robovm.compiler.ITable;
import org.robovm.compiler.MarshalerLookup;
import org.robovm.compiler.VTable;
import org.robovm.compiler.Version;
import org.robovm.compiler.clazz.Clazz;
import org.robovm.compiler.clazz.Clazzes;
import org.robovm.compiler.clazz.Path;
import org.robovm.compiler.config.OS.Family;
import org.robovm.compiler.config.StripArchivesConfig.StripArchivesBuilder;
import org.robovm.compiler.config.tools.Tools;
import org.robovm.compiler.llvm.DataLayout;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.plugin.CompilerPlugin;
import org.robovm.compiler.plugin.LaunchPlugin;
import org.robovm.compiler.plugin.Plugin;
import org.robovm.compiler.plugin.PluginArgument;
import org.robovm.compiler.plugin.TargetPlugin;
import org.robovm.compiler.plugin.annotation.AnnotationImplPlugin;
import org.robovm.compiler.plugin.debug.DebugInformationPlugin;
import org.robovm.compiler.plugin.debug.DebuggerLaunchPlugin;
import org.robovm.compiler.plugin.desugar.StringConcatRewriterPlugin;
import org.robovm.compiler.plugin.lambda.LambdaPlugin;
import org.robovm.compiler.plugin.objc.InterfaceBuilderClassesPlugin;
import org.robovm.compiler.plugin.objc.ObjCBlockPlugin;
import org.robovm.compiler.plugin.objc.ObjCMemberPlugin;
import org.robovm.compiler.plugin.objc.ObjCProtocolToObjCObjectPlugin;
import org.robovm.compiler.plugin.objc.ObjCProtocolProxyPlugin;
import org.robovm.compiler.target.ConsoleTarget;
import org.robovm.compiler.target.Target;
import org.robovm.compiler.target.framework.FrameworkTarget;
import org.robovm.compiler.target.ios.IOSTarget;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;
import org.robovm.compiler.util.DigestUtil;
import org.robovm.compiler.util.InfoPList;
import org.robovm.compiler.util.io.RamDiskTools;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.filter.PlatformFilter;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import org.simpleframework.xml.transform.RegistryMatcher;
import org.simpleframework.xml.transform.Transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Holds compiler configuration.
 */
@Root
public class Config {
    

	/**
     * The max file name length of files stored in the cache. OS X has a limit
     * of 255 characters. Class names are very unlikely to be this long but some
     * JVM language compilers (e.g. the Scala compiler) are known to generate
     * very long class names for auto-generated classes. See #955.
     */
    private static final int MAX_FILE_NAME_LENGTH = 255;

    public enum Cacerts {
        full
    }

    public enum TreeShakerMode {
        none, conservative, aggressive
    }

    @Element(required = false)
    private File installDir = null;
    @Element(required = false)
    private String executableName = null;
    @Element(required = false)
    private String imageName = null;
    @Element(required = false)
    private Boolean skipRuntimeLib = null;
    @Element(required = false)
    private File mainJar;
    @Element(required = false)
    private String mainClass;
    @Element(required = false)
    private Cacerts cacerts = null;
    @Element(required = false)
    private OS os = null;
    @ElementList(required = false, inline = true)
    private ArrayList<Arch> archs = null;
    @ElementList(required = false, entry = "root")
    private ArrayList<String> roots;
    @ElementList(required = false, entry = "pattern")
    private ArrayList<String> forceLinkClasses;
    @ElementList(required = false, entry = "entry")
    private ArrayList<ForceLinkMethodsConfig> forceLinkMethods;
    @ElementList(required = false, entry = "lib")
    private ArrayList<Lib> libs;
    @ElementList(required = false, entry = "symbol")
    private ArrayList<String> exportedSymbols;
    @ElementList(required = false, entry = "symbol")
    private ArrayList<String> unhideSymbols;
    @ElementList(required = false, entry = "framework")
    private ArrayList<String> frameworks;
    @ElementList(required = false, entry = "framework")
    private ArrayList<String> weakFrameworks;
    @ElementList(required = false, entry = "path")
    private ArrayList<QualifiedFile> frameworkPaths;
    @ElementList(required = false, entry = "extension")
    private ArrayList<AppExtension> appExtensions;
    @ElementList(required = false, entry = "path")
    private ArrayList<QualifiedFile> appExtensionPaths;
    @Element(required = false)
    private SwiftSupport swiftSupport = null;
    @ElementList(required = false, entry = "resource")
    private ArrayList<Resource> resources;   
    @ElementList(required = false, entry = "classpathentry")
    private ArrayList<File> bootclasspath;
    @ElementList(required = false, entry = "classpathentry")
    private ArrayList<File> classpath;
    @ElementList(required = false, entry = "argument")
    private ArrayList<String> pluginArguments;
    @Element(required = false, name = "target")
    private String targetType;
    @Element(required = false, name = "stripArchives")
    private StripArchivesConfig stripArchivesConfig;
    @Element(required = false, name = "treeShaker")
    private TreeShakerMode treeShakerMode;
    @Element(required = false, name = "smartSkipRebuild")
    private Boolean smartSkipRebuild;

    @Element(required = false)
    private String iosSdkVersion;
    @Element(required = false, name = "iosInfoPList")
    private File iosInfoPListFile = null;
    @Element(required = false, name = "infoPList")
    private File infoPListFile = null;    
    @Element(required = false)
    private File iosEntitlementsPList;

    @Element(required = false)
    private WatchKitApp watchKitApp;

    @Element(required = false)
    private Tools tools;

    @Element(required = false)
    private Boolean enableBitcode;

    private SigningIdentity iosSignIdentity;
    private ProvisioningProfile iosProvisioningProfile;
    private String iosDeviceType;
    private InfoPList infoPList;

    private boolean iosSkipSigning = false;

    private Properties properties = new Properties();

    private Home home = null;
    private File tmpDir;
    private File cacheDir = new File(System.getProperty("user.home"), ".robovm/cache");
    private File ccBinPath = null;

    private boolean clean = false;
    private boolean debug = false;
    private boolean useDebugLibs = false;
    private boolean skipLinking = false;
    private boolean skipInstall = false;
    private boolean dumpIntermediates = false;
    private boolean manuallyPreparedForLaunch = false;
    private int threads = Runtime.getRuntime().availableProcessors();
    private Logger logger = Logger.NULL_LOGGER;

    /*
     * The fields below are all initialized in build() and must not be included
     * when constructing Config clone. We mark them as transient which will make
     * the builder() method skip them.
     */

    private transient final UUID buildUuid;
    private transient List<Plugin> plugins = new ArrayList<>();
    private transient Target target = null;
    private transient File osArchDepLibDir;
    private transient File osArchCacheDir;
    private transient Clazzes clazzes;
    private transient VTable.Cache vtableCache;
    private transient ITable.Cache itableCache;
    private transient List<Path> resourcesPaths = new ArrayList<>();
    private transient DataLayout dataLayout;
    private transient MarshalerLookup marshalerLookup;
    private transient Config configBeforeBuild;
    private transient DependencyGraph dependencyGraph;
    private transient Arch sliceArch;
    private transient StripArchivesBuilder stripArchivesBuilder;

    protected Config(UUID uuid) {
        // save session uuid
        this.buildUuid = uuid;

        // Add standard plugins
        this.plugins.addAll(0, Arrays.asList(
                new InterfaceBuilderClassesPlugin(),
                new ObjCProtocolProxyPlugin(),
                new ObjCProtocolToObjCObjectPlugin(),
                new ObjCMemberPlugin(),
                new ObjCBlockPlugin(),
                new AnnotationImplPlugin(),
                new StringConcatRewriterPlugin(),
                new LambdaPlugin(),         
                new DebugInformationPlugin(),
                new DebuggerLaunchPlugin()
                ));
        this.loadPluginsFromClassPath();
    }

    /**
     * Returns a new {@link Builder} which builds exactly this {@link Config}
     * when {@link Builder#build()} is called.
     */
    public Builder builder() throws IOException {
        return new Builder(clone(configBeforeBuild));
    }

    public UUID getBuildUuid() {
        return buildUuid;
    }

    public Home getHome() {
        return home;
    }

    public File getInstallDir() {
        return installDir;
    }

    public String getExecutableName() {
        return executableName;
    }

    public String getImageName() {
        return imageName;
    }

    public File getExecutablePath() {
        return new File(installDir, getExecutableName());
    }

    public File getImagePath() {
        return getExecutablePath();
    }

    public File getCacheDir() {
        return osArchCacheDir;
    }

    public File getCcBinPath() {
        return ccBinPath;
    }

    public OS getOs() {
        return os;
    }

    public Arch getArch() {
        return sliceArch;
    }

    public List<Arch> getArchs() {
        return archs == null ? Collections.emptyList()
                : Collections.unmodifiableList(archs);
    }
    
    public String getTriple() {
        return sliceArch.getLlvmName() + "-unknown-" + os.getLlvmName();
    }

    public String getClangTriple() {
        return sliceArch.getClangName() + "-unknown-" + os.getLlvmName();
    }

    public DataLayout getDataLayout() {
        return dataLayout;
    }

    public boolean isClean() {
        return clean;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isUseDebugLibs() {
        return useDebugLibs;
    }

    public boolean isDumpIntermediates() {
        return dumpIntermediates;
    }

    public boolean isManuallyPreparedForLaunch() {
        return manuallyPreparedForLaunch;
    }

    public boolean isSkipRuntimeLib() {
        return skipRuntimeLib != null && skipRuntimeLib;
    }

    public boolean isSkipLinking() {
        return skipLinking;
    }

    public boolean isSkipInstall() {
        return skipInstall;
    }

    public int getThreads() {
        return threads;
    }

    public File getMainJar() {
        return mainJar;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Cacerts getCacerts() {
        return cacerts == null ? Cacerts.full : cacerts;
    }

    public List<Path> getResourcesPaths() {
        return resourcesPaths;
    }

    public void addResourcesPath(Path path) {
        resourcesPaths.add(path);
    }
    
    public StripArchivesConfig getStripArchivesConfig() {
       return stripArchivesConfig == null ? StripArchivesConfig.DEFAULT : stripArchivesConfig;
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }
    
    public File getTmpDir() {
        if (tmpDir == null) {
            try {
                tmpDir = File.createTempFile("robovm", ".tmp");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            tmpDir.delete();
            tmpDir.mkdirs();
        }
        return tmpDir;
    }

    public List<String> getForceLinkClasses() {
        return forceLinkClasses == null ? Collections.emptyList()
                : Collections.unmodifiableList(forceLinkClasses);
    }

    public List<ForceLinkMethodsConfig> getForceLinkMethods() {
        return forceLinkMethods == null ? Collections.emptyList()
                : Collections.unmodifiableList(forceLinkMethods);
    }

    public List<String> getExportedSymbols() {
        return exportedSymbols == null ? Collections.emptyList()
                : Collections.unmodifiableList(exportedSymbols);
    }

    public List<String> getUnhideSymbols() {
        return unhideSymbols == null ? Collections.emptyList()
                : Collections.unmodifiableList(unhideSymbols);
    }
    
    public List<Lib> getLibs() {
        return libs == null ? Collections.emptyList()
                : Collections.unmodifiableList(libs);
    }

    public List<String> getFrameworks() {
        return frameworks == null ? Collections.emptyList()
                : Collections.unmodifiableList(frameworks);
    }

    public List<String> getWeakFrameworks() {
        return weakFrameworks == null ? Collections.emptyList()
                : Collections.unmodifiableList(weakFrameworks);
    }

    public List<File> getFrameworkPaths() {
        return frameworkPaths == null ? Collections.emptyList()
                : frameworkPaths.stream()
                .filter(this::isQualified)
                .map(f -> f.entry)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public List<AppExtension> getAppExtensions() {
        return appExtensions == null ? Collections.emptyList()
                : Collections.unmodifiableList(appExtensions);
    }

    public List<File> getAppExtensionPaths() {
        return appExtensionPaths == null ? Collections.emptyList()
                : appExtensionPaths.stream()
                .filter(this::isQualified)
                .map(e -> e.entry)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public SwiftSupport getSwiftSupport() {
        return swiftSupport;
    }

    public boolean hasSwiftSupport() {
        return swiftSupport != null;
    }

    public List<File> getSwiftLibPaths() {
        return swiftSupport == null ? Collections.emptyList()
                : swiftSupport.getSwiftLibPaths().stream()
                .filter(this::isQualified)
                .map(f -> f.entry)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public List<Resource> getResources() {
        return resources == null ? Collections.emptyList()
                : Collections.unmodifiableList(resources);
    }

    public File getOsArchDepLibDir() {
        return osArchDepLibDir;
    }

    public Clazzes getClazzes() {
        return clazzes;
    }

    public VTable.Cache getVTableCache() {
        return vtableCache;
    }

    public ITable.Cache getITableCache() {
        return itableCache;
    }

    public MarshalerLookup getMarshalerLookup() {
        return marshalerLookup;
    }

    public List<CompilerPlugin> getCompilerPlugins() {
        List<CompilerPlugin> compilerPlugins = new ArrayList<>();
        for (Plugin plugin : plugins) {
            if (plugin instanceof CompilerPlugin) {
                compilerPlugins.add((CompilerPlugin) plugin);
            }
        }
        return compilerPlugins;
    }

    public List<LaunchPlugin> getLaunchPlugins() {
        List<LaunchPlugin> launchPlugins = new ArrayList<>();
        for (Plugin plugin : plugins) {
            if (plugin instanceof LaunchPlugin) {
                launchPlugins.add((LaunchPlugin) plugin);
            }
        }
        return launchPlugins;
    }

    public List<TargetPlugin> getTargetPlugins() {
        List<TargetPlugin> targetPlugins = new ArrayList<>();
        for (Plugin plugin : plugins) {
            if (plugin instanceof TargetPlugin) {
                targetPlugins.add((TargetPlugin) plugin);
            }
        }
        return targetPlugins;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public List<String> getPluginArguments() {
        return pluginArguments == null ? Collections.emptyList()
                : Collections.unmodifiableList(pluginArguments);
    }

    public List<File> getBootclasspath() {
        return bootclasspath == null ? Collections.emptyList()
                : Collections.unmodifiableList(bootclasspath);
    }

    public List<File> getClasspath() {
        return classpath == null ? Collections.emptyList()
                : Collections.unmodifiableList(classpath);
    }

    public Properties getProperties() {
        return properties;
    }

    public Logger getLogger() {
        return logger;
    }

    public Target getTarget() {
        return target;
    }

    public String getTargetType() {
        return targetType;
    }

    public TreeShakerMode getTreeShakerMode() {
        return treeShakerMode == null ? TreeShakerMode.none : treeShakerMode;
    }

    public boolean isSmartSkipRebuild(){
        return smartSkipRebuild != null && smartSkipRebuild;
    }

    public String getIosSdkVersion() {
        return iosSdkVersion;
    }

    public String getIosDeviceType() {
        return iosDeviceType;
    }

    public InfoPList getIosInfoPList() {
        return getInfoPList();
    }

    public InfoPList getInfoPList() {
        if (infoPList == null && iosInfoPListFile != null) {
            infoPList = new InfoPList(iosInfoPListFile);
        } else if (infoPList == null && infoPListFile != null) {
            infoPList = new InfoPList(infoPListFile);
        }
        return infoPList;
    }

    public File getIosEntitlementsPList() {
        return iosEntitlementsPList;
    }

    public SigningIdentity getIosSignIdentity() {
        return iosSignIdentity;
    }

    public ProvisioningProfile getIosProvisioningProfile() {
        return iosProvisioningProfile;
    }

    public boolean isIosSkipSigning() {
        return iosSkipSigning;
    }

    public boolean isEnableBitcode() {return enableBitcode != null && enableBitcode && shouldEmitBitcode(); }

    public boolean shouldEmitBitcode() {
        // emit bitcode to object file even if it is not enabled.
        // as currently only `__LLVM,__asm` is being added
        // but build should match criteria
        return !debug && os == OS.ios && (sliceArch == Arch.arm64 || sliceArch == Arch.thumbv7);
    }

    public Tools getTools() {
        return tools;
    }

    public WatchKitApp getWatchKitApp() {
        return watchKitApp;
    }

    private static File makeFileRelativeTo(File dir, File f) {
        if (f.getParentFile() == null) {
            return dir;
        }
        return new File(makeFileRelativeTo(dir, f.getParentFile()), f.getName());
    }

    public String getArchiveName(Path path) {
        if (path.getFile().isFile()) {
            return path.getFile().getName();
        } else {
            return "classes" + path.getIndex() + ".jar";
        }
    }

    static String getFileName(Clazz clazz, String ext) {
        return getFileName(clazz.getInternalName(), ext, MAX_FILE_NAME_LENGTH);
    }

    static String getFileName(String internalName, String ext, int maxFileNameLength) {
        String packagePath = internalName.substring(0, internalName.lastIndexOf('/') + 1);
        String className = internalName.substring(internalName.lastIndexOf('/') + 1);
        String suffix = ext.startsWith(".") ? ext : "." + ext;

        int length = className.length() + suffix.length();
        if (length > maxFileNameLength) {
            String sha1 = DigestUtil.sha1(className);
            className = className.substring(0, Math.max(0, maxFileNameLength - suffix.length() - sha1.length())) + sha1;
        }
        return packagePath.replace('/', File.separatorChar) + className + suffix;
    }

    public File getLlFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.ll"));
    }

    public File getCFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.c"));
    }

    public File getBcFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.bc"));
    }

    public File getSFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.s"));
    }

    public File getOFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.o"));
    }

    public File getLinesOFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.lines.o"));
    }

    public File getLinesLlFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.lines.ll"));
    }

    public File getDebugInfoOFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.debuginfo.o"));
    }

    public File getDebugInfoLlFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.debuginfo.ll"));
    }

    public File getInfoFile(Clazz clazz) {
        return new File(getCacheDir(clazz.getPath()), getFileName(clazz, "class.info"));
    }

    public File getCacheDir(Path path) {
        File srcRoot = path.getFile().getAbsoluteFile().getParentFile();
        String name = path.getFile().getName();
        try {
            return new File(makeFileRelativeTo(osArchCacheDir, srcRoot.getCanonicalFile()), name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the directory where generated classes are stored for the
     * specified {@link Path}. Generated classes are stored in the cache
     * directory in a dir at the same level as the cache dir for the
     * {@link Path} with <code>.generated</code> appended to the dir name.
     */
    public File getGeneratedClassDir(Path path) {
        File pathCacheDir = getCacheDir(path);
        return new File(pathCacheDir.getParentFile(), pathCacheDir.getName() + ".generated");
    }

    /**
     * tests if qualified item matches this config
     */
    protected boolean isQualified(Qualified qualified) {
        if (qualified.filterPlatforms() != null) {
            if (!Arrays.asList(qualified.filterPlatforms()).contains(os))
                return false;
        }
        if (qualified.filterArch() != null) {
            if (!Arrays.asList(qualified.filterArch()).contains(sliceArch))
                return false;
        }
        // TODO: there is no platform variant, just guess it from arch, applies to iOS temporally
        if (os == OS.ios && qualified.filterPlatformVariants() != null) {
            PlatformVariant variant = sliceArch.isArm() ? PlatformVariant.device : PlatformVariant.simulator;
            if (!Arrays.asList(qualified.filterPlatformVariants()).contains(variant))
                return false;
        }

        return true;
    }

    private static Map<Object, Object> getManifestAttributes(File jarFile) throws IOException {
        try (JarFile jf = new JarFile(jarFile)) {
            return new HashMap<>(jf.getManifest().getMainAttributes());
        }
    }

    private static String getImplementationVersion(File jarFile) throws IOException {
        return (String) getManifestAttributes(jarFile).get(Attributes.Name.IMPLEMENTATION_VERSION);
    }

    private static String getMainClass(File jarFile) throws IOException {
        return (String) getManifestAttributes(jarFile).get(Attributes.Name.MAIN_CLASS);
    }

    private File extractIfNeeded(Path path) throws IOException {
        if (path.getFile().isFile()) {
            File pathCacheDir = getCacheDir(path);
            File target = new File(pathCacheDir.getParentFile(), pathCacheDir.getName() + ".extracted");

            if (!target.exists() || path.getFile().lastModified() > target.lastModified()) {
                FileUtils.deleteDirectory(target);
                target.mkdirs();
                try (ZipFile zipFile = new ZipFile(path.getFile())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().startsWith("META-INF/robovm/") && !entry.isDirectory()) {
                            File f = new File(target, entry.getName());
                            f.getParentFile().mkdirs();
                            try (InputStream in = zipFile.getInputStream(entry);
                                 OutputStream out = new FileOutputStream(f)) {

                                IOUtils.copy(in, out);
                                if (entry.getTime() != -1) {
                                    f.setLastModified(entry.getTime());
                                }
                            }
                        }
                    }
                }
                target.setLastModified(path.getFile().lastModified());
            }

            return target;
        } else {
            return path.getFile();
        }
    }

    private <T> ArrayList<T> mergeLists(ArrayList<T> from, ArrayList<T> to) {
        if (from == null) {
            return to;
        }
        to = to != null ? to : new ArrayList<>();
        for (T o : from) {
            if (!to.contains(o)) {
                to.add(o);
            }
        }
        return to;
    }

    private void mergeConfig(Config from, Config to) {
        to.exportedSymbols = mergeLists(from.exportedSymbols, to.exportedSymbols);
        to.unhideSymbols = mergeLists(from.unhideSymbols, to.unhideSymbols);
        to.forceLinkClasses = mergeLists(from.forceLinkClasses, to.forceLinkClasses);
        to.forceLinkMethods = mergeLists(from.forceLinkMethods, to.forceLinkMethods);
        to.frameworkPaths = mergeLists(from.frameworkPaths, to.frameworkPaths);
        to.frameworks = mergeLists(from.frameworks, to.frameworks);
        to.libs = mergeLists(from.libs, to.libs);
        to.resources = mergeLists(from.resources, to.resources);
        to.weakFrameworks = mergeLists(from.weakFrameworks, to.weakFrameworks);
    }

    private void mergeConfigsFromClasspath() throws IOException {
        List<String> dirs = Arrays.asList(
                "META-INF/robovm/" + os + "/" + sliceArch,
                "META-INF/robovm/" + os);

        // The algorithm below preserves the order of config data from the
        // classpath. Last the config from this object is added.

        // First merge all configs on the classpath to an empty Config
        Config config = new Config(this.buildUuid);
        for (Path path : clazzes.getPaths()) {
            for (String dir : dirs) {
                if (path.contains(dir + "/robovm.xml")) {
                    File configXml = new File(new File(extractIfNeeded(path), dir), "robovm.xml");
                    Builder builder = new Builder();
                    builder.read(configXml);
                    mergeConfig(builder.config, config);
                    break;
                }
            }
        }

        // Then merge with this Config
        mergeConfig(this, config);

        // Copy back to this Config
        this.exportedSymbols = config.exportedSymbols;
        this.unhideSymbols = config.unhideSymbols;
        this.forceLinkClasses = config.forceLinkClasses;
        this.forceLinkMethods = config.forceLinkMethods;
        this.frameworkPaths = config.frameworkPaths;
        this.frameworks = config.frameworks;
        this.libs = config.libs;
        this.resources = config.resources;
        this.weakFrameworks = config.weakFrameworks;
    }

    private static <T> List<T> toList(Iterator<T> it) {
        List<T> l = new ArrayList<>();
        while (it.hasNext()) {
            l.add(it.next());
        }
        return l;
    }

    private void loadPluginsFromClassPath() {
        ClassLoader classLoader = getClass().getClassLoader();
        ServiceLoader<CompilerPlugin> compilerPluginLoader = ServiceLoader.load(CompilerPlugin.class, classLoader);
        ServiceLoader<LaunchPlugin> launchPluginLoader = ServiceLoader.load(LaunchPlugin.class, classLoader);
        ServiceLoader<TargetPlugin> targetPluginLoader = ServiceLoader.load(TargetPlugin.class, classLoader);

        plugins.addAll(toList(compilerPluginLoader.iterator()));
        plugins.addAll(toList(launchPluginLoader.iterator()));
        plugins.addAll(toList(targetPluginLoader.iterator()));
    }

    private static Config clone(Config config) throws IOException {
        Config clone = new Config(config.buildUuid);
        for (Field f : Config.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
                f.setAccessible(true);
                try {
                    Object o = f.get(config);
                    if (o instanceof Collection && o instanceof Cloneable) {
                        // Clone collections. Assume the class has a public
                        // clone() method.
                        Method m = o.getClass().getMethod("clone");
                        o = m.invoke(o);
                    }
                    f.set(clone, o);
                } catch (Throwable t) {
                    throw new Error(t);
                }
            }
        }
        return clone;
    }

    private Config build() throws IOException {
        // Create a clone of this Config before we have done anything with it so
        // that builder() has a fresh Config it can use.
        this.configBeforeBuild = clone(this);

        if (home == null) {
            home = Home.find();
        }

        if (bootclasspath == null) {
            bootclasspath = new ArrayList<>();
        }
        if (classpath == null) {
            classpath = new ArrayList<>();
        }

        if (mainJar != null) {
            mainClass = getMainClass(mainJar);
            classpath.add(mainJar);
        }

        if (executableName == null && imageName != null) {
            executableName = imageName;
        }

        if (!skipLinking && executableName == null && mainClass == null) {
            throw new IllegalArgumentException("No target and no main class specified");
        }

        if (!skipLinking && classpath.isEmpty()) {
            throw new IllegalArgumentException("No classpath specified");
        }

        if (skipLinking) {
            skipInstall = true;
        }

        if (executableName == null) {
            executableName = mainClass;
        }

        if (imageName == null || !imageName.equals(executableName)) {
            imageName = executableName;
        }

        List<File> realBootclasspath = bootclasspath == null ? new ArrayList<>() : bootclasspath;
        if (!isSkipRuntimeLib()) {
            realBootclasspath = new ArrayList<>(bootclasspath);
            realBootclasspath.add(0, home.rtPath);
        }

        this.vtableCache = new VTable.Cache();
        this.itableCache = new ITable.Cache();
        this.marshalerLookup = new MarshalerLookup(this);

        if (!skipInstall) {
            if (installDir == null) {
                installDir = new File(".", executableName);
            }
            installDir.mkdirs();
        }

        if (targetType != null) {
            if (ConsoleTarget.TYPE.equals(targetType)) {
                target = new ConsoleTarget();
            } else if (IOSTarget.TYPE.equals(targetType)) {
                target = new IOSTarget();
            } else if (FrameworkTarget.TYPE.equals(targetType)) {
                target = new FrameworkTarget();
            } else {
                for (TargetPlugin plugin : getTargetPlugins()) {
                    if (plugin.getTarget().getType().equals(targetType)) {
                        target = plugin.getTarget();
                        break;
                    }
                }
                if (target == null) {
                    throw new IllegalArgumentException("Unsupported target '" + targetType + "'");
                }
            }
        } else {
            // Auto
            if (os == OS.ios) {
                target = new IOSTarget();
            } else {
                target = new ConsoleTarget();
            }
        }

        if (!getArchs().isEmpty()) {
            sliceArch = getArchs().get(0);
        }

        target.init(this);

        os = target.getOs();
        sliceArch = target.getArch();
        dataLayout = new DataLayout(getTriple());

        osArchDepLibDir = new File(new File(home.libVmDir, os.toString()),
                sliceArch.toString());

        if (treeShakerMode != null && treeShakerMode != TreeShakerMode.none 
                && os.getFamily() == Family.darwin && sliceArch == Arch.x86) {

            logger.warn("Tree shaking is not supported when building "
                    + "for OS X/iOS x86 32-bit due to a bug in Xcode's linker. No tree "
                    + "shaking will be performed. Run in 64-bit mode instead to "
                    + "use tree shaking.");
            treeShakerMode = TreeShakerMode.none;
        }
        dependencyGraph = new DependencyGraph(getTreeShakerMode());

        RamDiskTools ramDiskTools = new RamDiskTools();
        ramDiskTools.setupRamDisk(this, this.cacheDir, this.tmpDir);
        this.cacheDir = ramDiskTools.getCacheDir();
        this.tmpDir = ramDiskTools.getTmpDir();

        File osDir = new File(cacheDir, os.toString());
        File archDir = new File(osDir, sliceArch.toString());
        osArchCacheDir = new File(archDir, debug ? "debug" : "release");
        osArchCacheDir.mkdirs();

        this.clazzes = new Clazzes(this, realBootclasspath, classpath);
        
        if(this.stripArchivesConfig == null) {
            if(stripArchivesBuilder == null) {
                this.stripArchivesConfig = StripArchivesConfig.DEFAULT;
            }
            else {
                this.stripArchivesConfig = stripArchivesBuilder.build();
            }
        }

        mergeConfigsFromClasspath();

        return this;
    }

    public static class Home {
        private File binDir;
        private File libVmDir;
        private File rtPath;
        private Map<Cacerts, File> cacertsPath;
        private boolean dev = false;

        public Home(File homeDir) {
            this(homeDir, true);
        }

        protected Home(File homeDir, boolean validate) {
            if (validate) {
                validate(homeDir);
            }
            binDir = new File(homeDir, "bin");
            libVmDir = new File(homeDir, "lib/vm");
            rtPath = new File(homeDir, "lib/robovm-rt.jar");
            cacertsPath = new HashMap<>();
            cacertsPath.put(Cacerts.full, new File(homeDir, "lib/robovm-cacerts-full.jar"));
        }

        private Home(File devDir, File binDir, File libVmDir, File rtPath) {
            this.binDir = binDir;
            this.libVmDir = libVmDir;
            this.rtPath = rtPath;
            cacertsPath = new HashMap<>();
            cacertsPath.put(Cacerts.full, new File(devDir,
                    "cacerts/full/target/robovm-cacerts-full-" + Version.getVersion() + ".jar"));
            this.dev = true;
        }

        public boolean isDev() {
            return dev;
        }

        public File getBinDir() {
            return binDir;
        }

        public File getLibVmDir() {
            return libVmDir;
        }

        public File getRtPath() {
            return rtPath;
        }

        public File getCacertsPath(Cacerts cacerts) {
            return cacertsPath.get(cacerts);
        }

        public static Home find() {
            // Check if ROBOVM_DEV_ROOT has been set. If set it should be
            // pointing at the root of a complete RoboVM source tree.
            if (System.getenv("ROBOVM_DEV_ROOT") != null) {
                File dir = new File(System.getenv("ROBOVM_DEV_ROOT"));
                return validateDevRootDir(dir);
            }
            if (System.getProperty("ROBOVM_DEV_ROOT") != null) {
                File dir = new File(System.getProperty("ROBOVM_DEV_ROOT"));
                return validateDevRootDir(dir);
            }

            if (System.getenv("ROBOVM_HOME") != null) {
                File dir = new File(System.getenv("ROBOVM_HOME"));
                return new Home(dir);
            }

            List<File> candidates = new ArrayList<>();
            File userHome = new File(System.getProperty("user.home"));
            candidates.add(new File(userHome, "Applications/robovm"));
            candidates.add(new File(userHome, ".robovm/home"));
            candidates.add(new File("/usr/local/lib/robovm"));
            candidates.add(new File("/opt/robovm"));
            candidates.add(new File("/usr/lib/robovm"));

            for (File dir : candidates) {
                if (dir.exists()) {
                    return new Home(dir);
                }
            }

            throw new IllegalArgumentException("ROBOVM_HOME not set and no RoboVM "
                    + "installation found in " + candidates);
        }

        public static void validate(File dir) {
            String error = "Path " + dir + " is not a valid RoboVM install directory: ";
            // Check for required dirs and match the compiler version with our
            // version.
            if (!dir.exists()) {
                throw new IllegalArgumentException(error + "no such path");
            }

            if (!dir.isDirectory()) {
                throw new IllegalArgumentException(error + "not a directory");
            }

            File libDir = new File(dir, "lib");
            if (!libDir.exists() || !libDir.isDirectory()) {
                throw new IllegalArgumentException(error + "lib/ missing or invalid");
            }
            File binDir = new File(dir, "bin");
            if (!binDir.exists() || !binDir.isDirectory()) {
                throw new IllegalArgumentException(error + "bin/ missing or invalid");
            }
            File libVmDir = new File(libDir, "vm");
            if (!libVmDir.exists() || !libVmDir.isDirectory()) {
                throw new IllegalArgumentException(error + "lib/vm/ missing or invalid");
            }
            File rtJarFile = new File(libDir, "robovm-rt.jar");
            if (!rtJarFile.exists() || !rtJarFile.isFile()) {
                throw new IllegalArgumentException(error
                        + "lib/robovm-rt.jar missing or invalid");
            }

            // Compare the version of this compiler with the version of the
            // robovm-rt.jar in the home dir. They have to match.
            try {
                String thisVersion = Version.getVersion();
                String thatVersion = getImplementationVersion(rtJarFile);
                if (thisVersion == null || !thisVersion.equals(thatVersion)) {
                    throw new IllegalArgumentException(error + "version mismatch (expected: "
                            + thisVersion + ", was: " + thatVersion + ")");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(error
                        + "failed to get version of rt jar", e);
            }
        }

        private static Home validateDevRootDir(File dir) {
            String error = "Path " + dir + " is not a valid RoboVM source tree: ";
            // Check for required dirs.
            if (!dir.exists()) {
                throw new IllegalArgumentException(error + "no such path");
            }

            if (!dir.isDirectory()) {
                throw new IllegalArgumentException(error + "not a directory");
            }

            File vmBinariesDir = new File(dir, "vm/target/binaries");
            if (!vmBinariesDir.exists() || !vmBinariesDir.isDirectory()) {
                throw new IllegalArgumentException(error + "vm/target/binaries/ missing or invalid");
            }
            File binDir = new File(dir, "bin");
            if (!binDir.exists() || !binDir.isDirectory()) {
                throw new IllegalArgumentException(error + "bin/ missing or invalid");
            }

            String rtJarName = "robovm-rt-" + Version.getVersion() + ".jar";
            File rtJar = new File(dir, "rt/target/" + rtJarName);
            File rtClasses = new File(dir, "rt/target/classes/");
            File rtSource = rtJar;
            if (!rtJar.exists() || rtJar.isDirectory()) {
                if (!rtClasses.exists() || rtClasses.isFile()) {
                    throw new IllegalArgumentException(error
                            + "rt/target/" + rtJarName + " missing or invalid");
                } else {
                    rtSource = rtClasses;
                }
            }

            return new Home(dir, binDir, vmBinariesDir, rtSource);
        }
    }

    /**
     * reads configuration from disk without any analysis
     */
    public static Config loadRawConfig(File contentRoot) throws IOException {
        // dkimitsa: config retrieved this way shall not be used for any compilation needs
        // just for IB and other UI related things
        Builder builder = new Builder();
        builder.readProjectProperties(contentRoot, false);
        builder.readProjectConfig(contentRoot, false);
        return builder.config;
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static class Builder {
        protected final Config config;

        Builder(Config config) {
            this.config = config;
        }

        public Builder() {
            this.config = new Config(UUID.randomUUID());
        }

        public Builder os(OS os) {
            config.os = os;
            return this;
        }

        public Builder arch(Arch arch) {
            return archs(arch);
        }

        public Builder archs(Arch ... archs) {
            return archs(Arrays.asList(archs));
        }

        public Builder archs(List<Arch> archs) {
            if (config.archs == null) {
                config.archs = new ArrayList<>();
            }
            config.archs.clear();
            config.archs.addAll(archs);
            return this;
        }

        public Builder clearClasspathEntries() {
            if (config.classpath != null) {
                config.classpath.clear();
            }
            return this;
        }

        public Builder addClasspathEntry(File f) {
            if (config.classpath == null) {
                config.classpath = new ArrayList<>();
            }
            config.classpath.add(f);
            return this;
        }

        public Builder clearBootClasspathEntries() {
            if (config.bootclasspath != null) {
                config.bootclasspath.clear();
            }
            return this;
        }

        public Builder addBootClasspathEntry(File f) {
            if (config.bootclasspath == null) {
                config.bootclasspath = new ArrayList<>();
            }
            config.bootclasspath.add(f);
            return this;
        }

        public Builder mainJar(File f) {
            config.mainJar = f;
            return this;
        }

        public Builder installDir(File installDir) {
            config.installDir = installDir;
            return this;
        }

        public Builder executableName(String executableName) {
            config.executableName = executableName;
            return this;
        }

        public Builder imageName(String imageName) {
            config.imageName = imageName;
            return this;
        }

        public Builder home(Home home) {
            config.home = home;
            return this;
        }

        public Builder cacheDir(File cacheDir) {
            config.cacheDir = cacheDir;
            return this;
        }

        public Builder clean(boolean b) {
            config.clean = b;
            return this;
        }

        public Builder ccBinPath(File ccBinPath) {
            config.ccBinPath = ccBinPath;
            return this;
        }

        public Builder debug(boolean b) {
            config.debug = b;
            return this;
        }

        public Builder useDebugLibs(boolean b) {
            config.useDebugLibs = b;
            return this;
        }

        public Builder dumpIntermediates(boolean b) {
            config.dumpIntermediates = b;
            return this;
        }

        public Builder manuallyPreparedForLaunch(boolean b) {
            config.manuallyPreparedForLaunch = b;
            return this;
        }

        public Builder skipRuntimeLib(boolean b) {
            config.skipRuntimeLib = b;
            return this;
        }

        public Builder skipLinking(boolean b) {
            config.skipLinking = b;
            return this;
        }

        public Builder skipInstall(boolean b) {
            config.skipInstall = b;
            return this;
        }

        public Builder threads(int threads) {
            config.threads = threads;
            return this;
        }

        public Builder mainClass(String mainClass) {
            config.mainClass = mainClass;
            return this;
        }

        public Builder tmpDir(File tmpDir) {
            config.tmpDir = tmpDir;
            return this;
        }

        public Builder logger(Logger logger) {
            config.logger = logger;
            return this;
        }

        public Builder treeShakerMode(TreeShakerMode treeShakerMode) {
            config.treeShakerMode = treeShakerMode;
            return this;
        }

        public Builder smartSkipRebuild(boolean smartSkipRebuild){
            config.smartSkipRebuild = smartSkipRebuild;
            return this;
        }

        public Builder clearForceLinkClasses() {
            if (config.forceLinkClasses != null) {
                config.forceLinkClasses.clear();
            }
            return this;
        }

        public Builder addForceLinkClass(String pattern) {
            if (config.forceLinkClasses == null) {
                config.forceLinkClasses = new ArrayList<>();
            }
            config.forceLinkClasses.add(pattern);
            return this;
        }

        public Builder clearExportedSymbols() {
            if (config.exportedSymbols != null) {
                config.exportedSymbols.clear();
            }
            return this;
        }

        public Builder addExportedSymbol(String symbol) {
            if (config.exportedSymbols == null) {
                config.exportedSymbols = new ArrayList<>();
            }
            config.exportedSymbols.add(symbol);
            return this;
        }

        public Builder clearUnhideSymbols() {
            if (config.unhideSymbols != null) {
                config.unhideSymbols.clear();
            }
            return this;
        }

        public Builder addUnhideSymbol(String symbol) {
            if (config.unhideSymbols == null) {
                config.unhideSymbols = new ArrayList<>();
            }
            config.unhideSymbols.add(symbol);
            return this;
        }

        public Builder clearLibs() {
            if (config.libs != null) {
                config.libs.clear();
            }
            return this;
        }

        public Builder addLib(Lib lib) {
            if (config.libs == null) {
                config.libs = new ArrayList<>();
            }
            config.libs.add(lib);
            return this;
        }

        public Builder clearFrameworks() {
            if (config.frameworks != null) {
                config.frameworks.clear();
            }
            return this;
        }

        public Builder addFramework(String framework) {
            if (config.frameworks == null) {
                config.frameworks = new ArrayList<>();
            }
            config.frameworks.add(framework);
            return this;
        }

        public Builder clearWeakFrameworks() {
            if (config.weakFrameworks != null) {
                config.weakFrameworks.clear();
            }
            return this;
        }

        public Builder addWeakFramework(String framework) {
            if (config.weakFrameworks == null) {
                config.weakFrameworks = new ArrayList<>();
            }
            config.weakFrameworks.add(framework);
            return this;
        }

        public Builder clearFrameworkPaths() {
            if (config.frameworkPaths != null) {
                config.frameworkPaths.clear();
            }
            return this;
        }

        public Builder addFrameworkPath(File frameworkPath) {
            if (config.frameworkPaths == null) {
                config.frameworkPaths = new ArrayList<>();
            }
            config.frameworkPaths.add(new QualifiedFile(frameworkPath));
            return this;
        }

        public Builder clearExtensions() {
            if (config.appExtensions != null) {
                config.appExtensions.clear();
            }
            return this;
        }

        public Builder addExtension(String name, String profile) {
            if (config.appExtensions == null) {
                config.appExtensions = new ArrayList<>();
            }
            AppExtension extension = new AppExtension();
            extension.name = name;
            extension.profile = profile;
            config.appExtensions.add(extension);
            return this;
        }

        public Builder clearExtensionPaths() {
            if (config.appExtensionPaths != null) {
                config.appExtensionPaths.clear();
            }
            return this;
        }

        public Builder addExtenaionPath(File extensionPath) {
            if (config.appExtensionPaths == null) {
                config.appExtensionPaths = new ArrayList<>();
            }
            config.appExtensionPaths.add(new QualifiedFile(extensionPath));
            return this;
        }

        public Builder clearResources() {
            if (config.resources != null) {
                config.resources.clear();
            }
            return this;
        }

        public Builder addResource(Resource resource) {
            if (config.resources == null) {
                config.resources = new ArrayList<>();
            }
            config.resources.add(resource);
            return this;
        }
        
        public Builder stripArchivesBuilder(StripArchivesBuilder stripArchivesBuilder) {
           this.config.stripArchivesBuilder = stripArchivesBuilder;
           return this;
       }

        public Builder targetType(String targetType) {
            config.targetType = targetType;
            return this;
        }

        public Builder clearProperties() {
            config.properties.clear();
            return this;
        }

        public Builder addProperties(Properties properties) {
            config.properties.putAll(properties);
            return this;
        }

        public Builder addProperties(File file) throws IOException {
            Properties props = new Properties();
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                props.load(reader);
                addProperties(props);
            }
            return this;
        }

        public Builder addProperty(String name, String value) {
            config.properties.put(name, value);
            return this;
        }

        public Builder cacerts(Cacerts cacerts) {
            config.cacerts = cacerts;
            return this;
        }

        public Builder tools(Tools tools) {
            config.tools = tools;
            return this;
        }

        public Builder iosSdkVersion(String sdkVersion) {
            config.iosSdkVersion = sdkVersion;
            return this;
        }

        public Builder iosDeviceType(String deviceType) {
            config.iosDeviceType = deviceType;
            return this;
        }

        public Builder iosInfoPList(File infoPList) {
            config.iosInfoPListFile = infoPList;
            return this;
        }

        public Builder infoPList(File infoPList) {
            config.infoPListFile = infoPList;
            return this;
        }

        public Builder iosEntitlementsPList(File entitlementsPList) {
            config.iosEntitlementsPList = entitlementsPList;
            return this;
        }

        public Builder iosSignIdentity(SigningIdentity signIdentity) {
            config.iosSignIdentity = signIdentity;
            return this;
        }

        public Builder iosProvisioningProfile(ProvisioningProfile iosProvisioningProfile) {
            config.iosProvisioningProfile = iosProvisioningProfile;
            return this;
        }

        public Builder iosSkipSigning(boolean b) {
            config.iosSkipSigning = b;
            return this;
        }

        public Builder addCompilerPlugin(CompilerPlugin compilerPlugin) {
            config.plugins.add(compilerPlugin);
            return this;
        }

        public Builder addLaunchPlugin(LaunchPlugin plugin) {
            config.plugins.add(plugin);
            return this;
        }

        public Builder addTargetPlugin(TargetPlugin plugin) {
            config.plugins.add(plugin);
            return this;
        }

        public Builder enableBitcode(boolean enableBitcode) {
            config.enableBitcode = enableBitcode;
            return this;
        }

        public void addPluginArgument(String argName) {
            if (config.pluginArguments == null) {
                config.pluginArguments = new ArrayList<>();
            }
            config.pluginArguments.add(argName);
        }

        public Config build() throws IOException {
            for (CompilerPlugin plugin : config.getCompilerPlugins()) {
                plugin.beforeConfig(this, config);
            }

            return config.build();
        }

        /**
         * Reads properties from a project basedir. If {@code isTest} is
         * {@code true} this method will first attempt to load a
         * {@code robovm.test.properties} file in {@code basedir}.
         * <p>
         * If no test specific file is found or if {@code isTest} is
         * {@code false} this method attempts to load a
         * {@code robovm.properties} and a {@code robovm.local.properties} file
         * in {@code basedir} and merges them so that properties from the local
         * file (if it exists) override properties in the non-local file.
         * <p>
         * If {@code isTest} is {@code true} and no test specific properties
         * file was found this method will append {@code Test} to the
         * {@code app.id} and {@code app.name} properties (if they exist).
         * <p>
         * If none of the files can be found found this method does nothing.
         */
        public void readProjectProperties(File basedir, boolean isTest) throws IOException {
            File testPropsFile = new File(basedir, "robovm.test.properties");
            File localPropsFile = new File(basedir, "robovm.local.properties");
            File propsFile = new File(basedir, "robovm.properties");
            if (isTest && testPropsFile.exists()) {
                config.logger.info("Loading test RoboVM config properties file: "
                        + testPropsFile.getAbsolutePath());
                addProperties(testPropsFile);
            } else {
                Properties props = new Properties();
                if (propsFile.exists()) {
                    config.logger.info("Loading default RoboVM config properties file: "
                            + propsFile.getAbsolutePath());
                    try (Reader reader = new InputStreamReader(new FileInputStream(propsFile), StandardCharsets.UTF_8)) {
                        props.load(reader);
                    }
                }
                if (localPropsFile.exists()) {
                    config.logger.info("Loading local RoboVM config properties file: "
                            + localPropsFile.getAbsolutePath());
                    try (Reader reader = new InputStreamReader(new FileInputStream(localPropsFile), StandardCharsets.UTF_8)) {
                        props.load(reader);
                    }
                }
                if (isTest) {
                    modifyPropertyForTest(props, "app.id");
                    modifyPropertyForTest(props, "app.name");
                    modifyPropertyForTest(props, "app.executable");
                }
                addProperties(props);
            }
        }

        private void modifyPropertyForTest(Properties props, String propName) {
            String propValue = props.getProperty(propName);
            if (propValue != null && !propValue.endsWith("Test")) {
                String newPropValue = propValue + "Test";
                config.logger.info("Changing %s property from '%s' to '%s'", propName, propValue, newPropValue);
                props.setProperty(propName, newPropValue);
            }
        }

        /**
         * Reads a config file from a project basedir. If {@code isTest} is
         * {@code true} this method will first attempt to load a
         * {@code robovm.test.xml} file in {@code basedir}.
         * <p>
         * If no test-specific file is found or if {@code isTest} is
         * {@code false} this method attempts to load a {@code robovm.xml} file
         * in {@code basedir}.
         * <p>
         * If none of the files can be found found this method does nothing.
         */
        public void readProjectConfig(File basedir, boolean isTest) throws IOException {
            File testConfigFile = new File(basedir, "robovm.test.xml");
            File configFile = new File(basedir, "robovm.xml");
            if (isTest && testConfigFile.exists()) {
                config.logger.info("Loading test RoboVM config file: "
                        + testConfigFile.getAbsolutePath());
                read(testConfigFile);
            } else if (configFile.exists()) {
                config.logger.info("Loading default RoboVM config file: "
                        + configFile.getAbsolutePath());
                read(configFile);
            }
        }

        public void read(File file) throws IOException {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                read(reader, file.getAbsoluteFile().getParentFile());
            }
        }

        public void read(Reader reader, File wd) throws IOException {
            try {
                Serializer serializer = createSerializer(config, wd);
                serializer.read(config, reader);
            } catch (IOException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
            // <roots> was renamed to <forceLinkClasses> but we still support
            // <roots>. We need to copy <roots> to <forceLinkClasses> and set
            // <roots> to null.
            if (config.roots != null && !config.roots.isEmpty()) {
                if (config.forceLinkClasses == null) {
                    config.forceLinkClasses = new ArrayList<>();
                }
                config.forceLinkClasses.addAll(config.roots);
                config.roots = null;
            }
        }

        public void write(File file) throws IOException {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                write(writer, file.getAbsoluteFile().getParentFile());
            }
        }

        public void write(Writer writer, File wd) throws IOException {
            try {
                Serializer serializer = createSerializer(config, wd);
                serializer.write(config, writer);
            } catch (IOException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public static Serializer createSerializer(Config config, final File wd) throws Exception {
            RelativeFileConverter fileConverter = new RelativeFileConverter(wd);

            Serializer resourceSerializer = new Persister(
                    new RegistryStrategy(new Registry().bind(File.class, fileConverter)),
                    new PlatformFilter(config.properties), new Format(2));

            Registry registry = new Registry();
            RegistryStrategy registryStrategy = new RegistryStrategy(registry);
            RegistryMatcher matcher = new RegistryMatcher();
            Serializer serializer = new Persister(registryStrategy,
                    new PlatformFilter(config.properties), matcher, new Format(2));

            registry.bind(File.class, fileConverter);
            registry.bind(Lib.class, new RelativeLibConverter(fileConverter));
            registry.bind(Resource.class, new ResourceConverter(fileConverter, resourceSerializer));
            registry.bind(StripArchivesConfig.class, new StripArchivesConfigConverter());

            // converters for attributes (comma separated arrays)
            // adding file converter to matcher, as it fails to pick it from registry when writing
            // tag text of custom object (such as QualifiedFile)
            matcher.bind(File.class, fileConverter);
            matcher.bind(OS[].class, new EnumArrayConverter<>(OS.class));
            matcher.bind(Arch[].class, new EnumArrayConverter<>(Arch.class));
            matcher.bind(PlatformVariant[].class, new EnumArrayConverter<>(PlatformVariant.class));

            return serializer;
        }

        /**
         * Fetches the {@link PluginArgument}s of all registered plugins for
         * parsing.
         */
        public Map<String, PluginArgument> fetchPluginArguments() {
            Map<String, PluginArgument> args = new TreeMap<>();
            for (Plugin plugin : config.plugins) {
                for (PluginArgument arg : plugin.getArguments().getArguments()) {
                    args.put(plugin.getArguments().getPrefix() + ":" + arg.getName(), arg);
                }
            }
            return args;
        }

        public List<Plugin> getPlugins() {
            return config.getPlugins();
        }
    }

    public static final class Lib {
        private final String value;
        private final boolean force;

        public Lib(String value, boolean force) {
            this.value = value;
            this.force = force;
        }

        public String getValue() {
            return value;
        }

        public boolean isForce() {
            return force;
        }

        @Override
        public String toString() {
            return "Lib [value=" + value + ", force=" + force + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (force ? 1231 : 1237);
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Lib other = (Lib) obj;
            if (force != other.force) {
                return false;
            }
            if (value == null) {
                return other.value == null;
            } else return value.equals(other.value);
        }
    }

    /**
     * Container for file entry with platform/arch constraints
     */
    public static final class QualifiedFile extends AbstractQualified {
        @Text File entry;

        protected QualifiedFile() {
        }

        public QualifiedFile(File file) {
            entry = file;
        }

        public File getEntry() {
            return entry;
        }

        @Override
        public String toString() {
            return entry + " " + super.toString();
        }
    }


    private static final class RelativeLibConverter implements Converter<Lib> {
        private final RelativeFileConverter fileConverter;

        public RelativeLibConverter(RelativeFileConverter fileConverter) {
            this.fileConverter = fileConverter;
        }

        @Override
        public Lib read(InputNode node) throws Exception {
            String value = node.getValue();
            if (value == null) {
                return null;
            }
            InputNode forceNode = node.getAttribute("force");
            boolean force = forceNode == null || Boolean.parseBoolean(forceNode.getValue());
            if (value.endsWith(".a") || value.endsWith(".o")) {
                return new Lib(fileConverter.read(value).getAbsolutePath(), force);
            } else if (value.endsWith(".dylib") || value.endsWith(".so")) {
                File f = fileConverter.read(value);
                return new Lib(f.isFile() ? f.getAbsolutePath() : value, force);
            } else {
                return new Lib(value, force);
            }
        }

        @Override
        public void write(OutputNode node, Lib lib) throws Exception {
            String value = lib.getValue();
            boolean force = lib.isForce();
            if (value.endsWith(".a") || value.endsWith(".o")) {
                fileConverter.write(node, new File(value));
            } else {
                node.setValue(value);
            }
            if (!force) {
                node.setAttribute("force", "false");
            }
        }
    }

    /**
     * transformer for xml attribute/entry that transforms comma separated values into
     * enum array
     */
    private static final class EnumArrayConverter<T extends Enum<T>> implements Transform<T[]> {
        private final Class<T> enumClass;

        private EnumArrayConverter(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public T[] read(String s) {
            s = s.trim();
            if (s.isEmpty())
                return null;

            String[] tokens = s.split(",");
            @SuppressWarnings("unchecked")
            T[] res = (T[])Array.newInstance(enumClass, tokens.length);
            for (int idx = 0; idx < tokens.length; idx++)
                res[idx] = Enum.valueOf(enumClass, tokens[idx].trim());
            return res;
        }

        @Override
        public String write(T[] ts) {
            return Arrays.stream(ts).map(Enum::name).collect(Collectors.joining());
        }
    }

    private static final class RelativeFileConverter implements Converter<File>, Transform<File> {
        private final String wdPrefix;

        public RelativeFileConverter(File wd) {
            if (wd.isFile()) {
                wd = wd.getParentFile();
            }
            String prefix = wd.getAbsolutePath();
            if (prefix.endsWith(File.separator)) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            wdPrefix = prefix;
        }

        @Override
        public File read(String value) {
            if (value == null) {
                return null;
            }
            File file = new File(value);
            if (!file.isAbsolute()) {
                file = new File(wdPrefix, value);
            }
            return file;
        }

        @Override
        public File read(InputNode node) throws Exception {
            return read(node.getValue());
        }

        @Override
        public String write(File value) {
            String path = value.isAbsolute() ? value.getAbsolutePath() : value.getPath();
            if (value.isAbsolute() && path.startsWith(wdPrefix)) {
                if (path.length() == wdPrefix.length())
                    path = "";
                else
                    path = path.substring(wdPrefix.length() + 1);
            }
            return path;
        }

        @Override
        public void write(OutputNode node, File value) throws Exception {
            String path = write(value);
            if (path.isEmpty()) {
                if ("directory".equals(node.getName())) {
                    // Skip
                    node.remove();
                } else {
                    node.setValue("");
                }
            } else {
                node.setValue(path);
            }
        }
    }

    private static final class ResourceConverter implements Converter<Resource> {
        private final RelativeFileConverter fileConverter;
        private final Serializer serializer;

        public ResourceConverter(RelativeFileConverter fileConverter, Serializer serializer) {
            this.fileConverter = fileConverter;
            this.serializer = serializer;
        }

        @Override
        public Resource read(InputNode node) throws Exception {
            String value = node.getValue();
            if (value != null && value.trim().length() > 0) {
                return new Resource(fileConverter.read(value));
            }
            return serializer.read(Resource.class, node);
        }

        @Override
        public void write(OutputNode node, Resource resource) throws Exception {
            File path = resource.getPath();
            if (path != null) {
                fileConverter.write(node, path);
            } else {
                node.remove();
                serializer.write(resource, node.getParent());
            }
        }
    }
    
    private static final class StripArchivesConfigConverter implements Converter<StripArchivesConfig> {

   	 @Override
   	 public StripArchivesConfig read (InputNode node) throws Exception {
   		 StripArchivesBuilder cfgBuilder = new StripArchivesBuilder();
   		 InputNode childNode;
   		 while((childNode = node.getNext()) != null){
   			 if(childNode.isElement() && !childNode.isEmpty() && childNode.getName().equals("include") || childNode.getName().equals("exclude")){
   				 boolean isInclude = childNode.getName().equals("include");
   				 cfgBuilder.add(isInclude, childNode.getValue());
   			 }
   		 }
   		 return cfgBuilder.build();
   	 }

   	 @Override
   	 public void write (OutputNode node, StripArchivesConfig config) throws Exception {
   		 if(config.getPatterns() != null && !config.getPatterns().isEmpty()){
   			 for(StripArchivesConfig.Pattern pattern : config.getPatterns()){
   				 OutputNode child = node.getChild(pattern.isInclude() ? "include" : "exclude");
   				 child.setValue(pattern.getPatternAsString());
   				 child.commit();
   			 }


   		 }
   		 node.commit();
   	 }

    }
}
