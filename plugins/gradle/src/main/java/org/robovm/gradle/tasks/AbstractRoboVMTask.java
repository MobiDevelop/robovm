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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Environment;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;
import org.robovm.gradle.RoboVMPlugin;
import org.robovm.gradle.RoboVMPluginExtension;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Junji Takakura
 */
abstract public class AbstractRoboVMTask extends DefaultTask {

    protected final Project project;
    protected final RoboVMPluginExtension extension;
    protected final RepositorySystem repositorySystem;
    protected final RepositorySystemSession repositorySystemSession;
    protected final List<RemoteRepository> remoteRepositories;
    protected Logger roboVMLogger;

    public AbstractRoboVMTask() {
        project = getProject();
        extension = (RoboVMPluginExtension) project.getExtensions().getByName(RoboVMPluginExtension.NAME);
        repositorySystem = createRepositorySystem();
        repositorySystemSession = createRepositorySystemSession();
        remoteRepositories = createRemoteRepositories();
        setGroup("MobiVM");
    }

    public AppCompiler build(OS os, Arch arch, Environment env, String targetType) {
        getLogger().info("Building RoboVM app for: " + os + " (" + arch + env.asLlvmSuffix("-")+ ")");

        Config.Builder builder;
        builder = new Config.Builder();

        configure(builder).os(os).arch(arch).env(env).targetType(targetType);

        // execute the RoboVM build
        Config config;

        try {
            getLogger().info("Compiling RoboVM app, this could take a while, especially the first time round");
            config = builder.build();
            AppCompiler compiler = new AppCompiler(config);
            compiler.build();
            getLogger().info("Compile RoboVM app completed.");
            return compiler;
        } catch (IOException e) {
            throw new GradleException("Error building RoboVM executable for app", e);
        }
    }

    protected Config.Builder configure(Config.Builder builder) {
        builder.logger(getRoboVMLogger());

        if (extension.getPropertiesFile() != null) {
            File propertiesFile = new File(extension.getPropertiesFile());

            if (!propertiesFile.exists()) {
                throw new GradleException("Invalid 'propertiesFile' specified for RoboVM compile: " + propertiesFile);
            }
            try {
                getLogger().debug(
                        "Including properties file in RoboVM compiler config: " + propertiesFile.getAbsolutePath());
                builder.addProperties(propertiesFile);
            } catch (IOException e) {
                throw new GradleException("Failed to add properties file to RoboVM config: " + propertiesFile);
            }
        } else {
            try {
                builder.readProjectProperties(project.getProjectDir(), false);
            } catch (IOException e) {
                throw new GradleException(
                        "Failed to read RoboVM project properties file(s) in "
                                + project.getProjectDir().getAbsolutePath(), e);
            }
        }

        if (extension.getConfigFile() != null) {
            File configFile = new File(extension.getConfigFile());

            if (!configFile.exists()) {
                throw new GradleException("Invalid 'configFile' specified for RoboVM compile: " + configFile);
            }
            try {
                getLogger().debug("Loading config file for RoboVM compiler: " + configFile.getAbsolutePath());
                builder.read(configFile);
            } catch (Exception e) {
                throw new GradleException("Failed to read RoboVM config file: " + configFile);
            }
        } else {
            try {
                builder.readProjectConfig(project.getProjectDir(), false);
            } catch (Exception e) {
                throw new GradleException(
                        "Failed to read project RoboVM config file in "
                                + project.getProjectDir().getAbsolutePath(), e);
            }
        }

        File installDir = null;
        if (extension.getInstallDir() != null) {
            installDir = new File(extension.getInstallDir());
        } else {
            installDir = new File(project.getBuildDir(), "robovm");
        }
        File cacheDir = null;
        if(extension.getCacheDir() != null) {
            cacheDir = new File(extension.getCacheDir());
        } else {
            cacheDir = new File(System.getProperty("user.home"), ".robovm/cache");
        }
        File temporaryDirectory = new File(project.getBuildDir(), "robovm.tmp");
        try {
            FileUtils.deleteDirectory(temporaryDirectory);
        } catch (IOException e) {
            throw new GradleException("Failed to clean output dir " + temporaryDirectory, e);
        }
        temporaryDirectory.mkdirs();

        builder.home(new Config.Home(unpack()))
                .tmpDir(temporaryDirectory)
                .skipInstall(true)
                .installDir(installDir)
                .cacheDir(cacheDir);

        if (project.hasProperty("mainClassName")) {
            builder.mainClass((String) project.property("mainClassName"));
        }

        if (extension.isDebug()) {
            builder.debug(true);
            if (extension.getDebugPort() != -1) {
                builder.addPluginArgument("debug:jdwpport=" + extension.getDebugPort());
            }
        }

        if (extension.isSkipLinking()) {
            builder.skipLinking(true);
        }

        if (extension.isIosSkipSigning()) {
            builder.iosSkipSigning(true);
        } else {
            if (extension.getIosSignIdentity() != null) {
                String iosSignIdentity = extension.getIosSignIdentity();

                getLogger().debug("Using explicit iOS Signing identity: " + iosSignIdentity);
                builder.iosSignIdentity(SigningIdentity.find(SigningIdentity.list(), iosSignIdentity));
            }

            if (extension.getIosProvisioningProfile() != null) {
                String iosProvisioningProfile = extension.getIosProvisioningProfile();

                getLogger().debug("Using explicit iOS provisioning profile: " + iosProvisioningProfile);
                builder.iosProvisioningProfile(ProvisioningProfile.find(ProvisioningProfile.list(),
                        iosProvisioningProfile));
            }
        }

        if (extension.isDumpIntermediates())
            builder.dumpIntermediates(true);

        if (extension.isEnableBitcode())
            builder.enableBitcode(true);

        builder.clearClasspathEntries();

        // configure the runtime classpath
        Set<File> classpathEntries = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME).getFiles();
        classpathEntries.add(new File(project.getBuildDir(), "classes/main"));
        classpathEntries.add(new File(project.getBuildDir(), "classes/java/main"));
        classpathEntries.add(new File(project.getBuildDir(), "classes/groovy/main"));
        classpathEntries.add(new File(project.getBuildDir(), "classes/scala/main"));
        classpathEntries.add(new File(project.getBuildDir(), "classes/kotlin/main"));
        classpathEntries.add(new File(project.getBuildDir(), "resources/main"));

        if (project.hasProperty("output.classesDir")) {
            classpathEntries.add((File) project.property("output.classesDir"));
        }

        for (File classpathEntry : classpathEntries) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Including classpath element for RoboVM app: " + classpathEntry.getAbsolutePath());
            }

            builder.addClasspathEntry(classpathEntry);
        }

        return builder;
    }

    @TaskAction
    abstract public void invoke();

    protected File unpack() throws GradleException {
        final Artifact artifact = resolveArtifact("com.mobidevelop.robovm:robovm-dist:tar.gz:nocompiler:"
                + RoboVMPlugin.getRoboVMVersion());
        final File distTarFile = artifact.getFile();
        final File unpackedDirectory = new File(distTarFile.getParent(), "unpacked");
        final File unpackedDistDirectory = new File(unpackedDirectory, "robovm-" + RoboVMPlugin.getRoboVMVersion());


        // for snapshot -- don't remove entire directory just override over existing files
        if (unpackedDirectory.exists() && !artifact.isSnapshot()) {
            getLogger().debug("Archive '" + distTarFile + "' was already unpacked in: " + unpackedDirectory);
        } else {
            getLogger().info("Extracting '" + distTarFile + "' to: " + unpackedDirectory);

            if (!unpackedDirectory.exists() && !unpackedDirectory.mkdirs()) {
                throw new GradleException("Unable to create base directory to unpack into: " + unpackedDirectory);
            }

            try {
                extractTarGz(distTarFile, unpackedDirectory);
            } catch (IOException e) {
                throw new GradleException("Couldn't extract distribution tar.gz", e);
            }

            if (!unpackedDistDirectory.exists()) {
                throw new GradleException("Unable to unpack archive");
            }
        }

        getAnt().invokeMethod("chmod", new HashMap<String, Object>() {
            {
                put("dir", new File(unpackedDistDirectory, "bin").getAbsoluteFile());
                put("perm", "+x");
                put("includes", "*");
            }
        });

        return unpackedDistDirectory;
    }

    protected Artifact resolveArtifact(String artifactLocator) throws GradleException {
        ArtifactRequest request = new ArtifactRequest();
        DefaultArtifact artifact = new DefaultArtifact(artifactLocator);
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);

        getLogger().debug("Resolving artifact " + artifact + " from " + remoteRepositories);

        ArtifactResult result;

        try {
            result = repositorySystem.resolveArtifact(repositorySystemSession, request);
        } catch (ArtifactResolutionException e) {
            throw new GradleException(e.getMessage(), e);
        }

        getLogger().debug(
                "Resolved artifact " + artifact + " to " + result.getArtifact().getFile() + " from "
                        + result.getRepository());

        return result.getArtifact();
    }

    protected Logger getRoboVMLogger() {
        if (roboVMLogger == null) {
            roboVMLogger = new Logger() {
                @Override
                public void debug(String s, Object... objects) {
                    getLogger().debug(String.format(s, objects));
                }

                @Override
                public void info(String s, Object... objects) {
                    getLogger().info(String.format(s, objects));
                }

                @Override
                public void warn(String s, Object... objects) {
                    getLogger().warn(String.format(s, objects));
                }

                @Override
                public void error(String s, Object... objects) {
                    getLogger().error(String.format(s, objects));
                }
            };
        }

        return roboVMLogger;
    }

    private RepositorySystem createRepositorySystem() {
        MavenServiceLocator locator = new MavenServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.setService(WagonProvider.class, ManualWagonProvider.class);

        return locator.getService(RepositorySystem.class);
    }

    private RepositorySystemSession createRepositorySystemSession() {
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(localRepository));

        return session;
    }

    private List<RemoteRepository> createRemoteRepositories() {
        List<RemoteRepository> repositories = new ArrayList<>();
        repositories.add(new RemoteRepository("maven-central", "default", "https://repo1.maven.org/maven2/"));
        repositories.add(new RemoteRepository("oss.sonatype.org-snapshots", "default",
                "https://oss.sonatype.org/content/repositories/snapshots/"));

        return repositories;
    }

    private void extractTarGz(File archive, File destDir) throws IOException {
        TarArchiveInputStream in = null;
        try {
            boolean filesWereUpdated = false;
            in = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)));
            ArchiveEntry entry = null;
            while ((entry = in.getNextEntry()) != null) {
                File f = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    f.mkdirs();
                } else {
                    // skip extracting if file looks to be same as in archive (ts and size matches)
                    if (f.exists() && f.lastModified() == entry.getLastModifiedDate().getTime() && f.length() == entry.getSize()) {
                        continue;
                    }
                    f.getParentFile().mkdirs();
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(f);
                        IOUtils.copy(in, out);
                        IOUtils.closeQuietly(out);
                        out = null;
                        // set last modification time stamp as it was inside tar otherwise
                        // robovm will see new time stamp and will rebuild all classes that were inside jar
                        f.setLastModified(entry.getLastModifiedDate().getTime());
                    } finally {
                        if (out != null)
                            IOUtils.closeQuietly(out);
                    }

                    if (entry instanceof TarArchiveEntry) {
                        int mode = ((TarArchiveEntry) entry).getMode();
                        if ((mode & 00100) > 0) {
                            // Preserve execute permissions
                            f.setExecutable(true, (mode & 00001) == 0);
                        }
                    }

                    // mark that there was a change to SDK files
                    filesWereUpdated = true;
                }
            }

            if (filesWereUpdated) {
                getLogger().debug("Archive '" + archive + "' unpacked to: " + destDir);
                getLogger().info("Clearing ~/.robovm/cache folder due SDK files changed.");

                File cacheDir = new File(System.getProperty("user.home"), ".robovm/cache");
                try {
                    FileUtils.deleteDirectory(cacheDir);
                } catch (IOException ignored) {
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static class ManualWagonProvider implements WagonProvider {

        @Override
        public Wagon lookup(String roleHint) throws Exception {
            if ("http".equals(roleHint) || "https".equals(roleHint)) {
                return new HttpWagon();
            }

            return null;
        }

        @Override
        public void release(Wagon wagon) {
            // noop
        }
    }
}
