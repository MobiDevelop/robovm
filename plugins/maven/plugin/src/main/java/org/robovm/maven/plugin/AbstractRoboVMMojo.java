/*
 * Copyright (C) 2013 RoboVM AB.
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
package org.robovm.maven.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.robovm.compiler.AppCompiler;
import org.robovm.compiler.Version;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.config.Config.Home;
import org.robovm.compiler.config.Environment;
import org.robovm.compiler.config.OS;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.target.ios.ProvisioningProfile;
import org.robovm.compiler.target.ios.SigningIdentity;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@Execute(goal = "compile", phase = LifecyclePhase.COMPILE)
public abstract class AbstractRoboVMMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Component
    private ArchiverManager archiverManager;

    @Component
    private ArtifactResolver artifactResolver;

    @Parameter(defaultValue = "${localRepository}")
    private ArtifactRepository localRepository;

    /**
     * Base directory to extract RoboVM native distribution files into. The
     * robovm-dist bundle will be downloaded from Maven and extracted into this
     * directory. Note that each release of RoboVM is placed in a separate
     * sub-directory with the version number as suffix.
     * <p>
     * If not set, then the tar file is extracted into the local Maven
     * repository where the tar file is downloaded to.
     */
    @Parameter
    protected File home;

    /**
     * The path to a {@code robovm.properties} file which contains info for your app.
     */
    @Parameter(property = "robovm.propertiesFile")
    protected File propertiesFile;

    /**
     * The path to a {@code robovm.xml} file which configures the RoboVM compiler.
     */
    @Parameter(property = "robovm.configFile")
    protected File configFile;

    /**
     * The identity to sign the app as when building an iOS bundle for the app.
     * Default is to look for an identity starting with 'iPhone Developer' or
     * 'iOS Development'. Enclose in '/' to search by regexp, e.g. '/foo|bar/'.
     */
    @Parameter(property = "robovm.iosSignIdentity")
    protected String iosSignIdentity;

    /**
     * The provisioning profile to use when building for device.
     */
    @Parameter(property = "robovm.iosProvisioningProfile")
    protected String iosProvisioningProfile;

    /**
     * Whether the app should be signed or not. Unsigned apps can only be run on jailbroken
     * devices.
     */
    @Parameter(property = "robovm.iosSkipSigning")
    protected boolean iosSkipSigning = false;

    /**
     * The directory into which the RoboVM distributable for the project will be built.
     */
    @Parameter(property = "robovm.installDir", defaultValue = "${project.build.directory}/robovm")
    protected File installDir;

    /**
     * Overrides the arch used when running the app. One of x86, x86_64, thumbv7, arm64.
     * Will be ignored if the specified value isn't supported by the executed goal.
     */
    @Parameter(property = "robovm.arch")
    protected String arch;

    /**
     * Overrides the os used when running the app. One of macosx, linux, ios.
     * Will be ignored if the specified value isn't supported by the executed goal.
     */
    @Parameter(property = "robovm.os")
    protected String os;

    /**
     * If set to {@code true} the app will be launched in debug mode. The app
     * will suspend before the main method is called and will wait for a
     * debugger to connect. If set to {@code "clientmode"} then the application
     * will connect back to the local host to attach to already started
     * debugging server which is waiting for connection on <code>robovm.debugPort</code>.
     */
    @Parameter(property = "robovm.debug")
    protected String debug;

    /**
     * The port to listen for debugger connections on when launching in debug
     * mode using {@code debug=true}. If not set a default port will be used.
     * The port actually used will be written to the console before the app is
     * launched.
     */
    @Parameter(property = "robovm.debugPort")
    protected int debugPort = -1;

    /**
     * Whether the app should dump intermediate .ll/.s files
     */
    @Parameter(property = "robovm.dumpIntermediates")
    protected boolean dumpIntermediates = false;

    /**
     * Whether the app should be build with bitcode embedded
     */
    @Parameter(property = "robovm.enableBitcode")
    protected boolean enableBitcode = false;

    private Logger roboVMLogger;

    protected Config.Builder configure(Config.Builder builder) throws MojoExecutionException {
        builder.logger(getRoboVMLogger());

        // load config base file if it exists (and properties)

        if (propertiesFile != null) {
            if (!propertiesFile.exists()) {
                throw new MojoExecutionException(
                        "Invalid 'propertiesFile' specified for RoboVM compile: "
                                + propertiesFile);
            }
            try {
                getLog().debug(
                        "Including properties file in RoboVM compiler config: "
                                + propertiesFile.getAbsolutePath());
                builder.addProperties(propertiesFile);
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failed to add properties file to RoboVM config: "
                                + propertiesFile);
            }
        } else {
            try {
                builder.readProjectProperties(project.getBasedir(), false);
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failed to read RoboVM project properties file(s) in "
                                + project.getBasedir().getAbsolutePath(), e);
            }
        }

        if (configFile != null) {
            if (!configFile.exists()) {
                throw new MojoExecutionException(
                        "Invalid 'configFile' specified for RoboVM compile: "
                                + configFile);
            }
            try {
                getLog().debug(
                        "Loading config file for RoboVM compiler: "
                                + configFile.getAbsolutePath());
                builder.read(configFile);
            } catch (Exception e) {
                throw new MojoExecutionException(
                        "Failed to read RoboVM config file: " + configFile);
            }
        } else {
            try {
                builder.readProjectConfig(project.getBasedir(), false);
            } catch (Exception e) {
                throw new MojoExecutionException(
                        "Failed to read project RoboVM config file in "
                                + project.getBasedir().getAbsolutePath(), e);
            }
        }

        // Read embedded RoboVM <config> if there is one
        Plugin plugin = project.getPlugin("com.mobidevelop.robovm:robovm-maven-plugin");
        MavenProject p = project;
        while (p != null && plugin == null) {
            plugin = p.getPluginManagement().getPluginsAsMap().get("com.mobidevelop.robovm:robovm-maven-plugin");
            if (plugin == null) p = p.getParent();
        }
        if (plugin != null) {
            getLog().debug("Reading RoboVM plugin configuration from " + p.getFile().getAbsolutePath());
            Xpp3Dom configDom = (Xpp3Dom) plugin.getConfiguration();
            if (configDom != null && configDom.getChild("config") != null) {
                StringWriter sw = new StringWriter();
                XMLWriter xmlWriter = new PrettyPrintXMLWriter(sw, "UTF-8", null);
                Xpp3DomWriter.write(xmlWriter, configDom.getChild("config"));
                try {
                    builder.read(new StringReader(sw.toString()),
                            project.getBasedir());
                } catch (Exception e) {
                    throw new MojoExecutionException(
                            "Failed to read RoboVM config embedded in POM", e);
                }
            }
        }

        File tmpDir = new File(project.getBuild().getDirectory(), "robovm.tmp");
        try {
            FileUtils.deleteDirectory(tmpDir);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Failed to clean output dir " + tmpDir, e);
        }
        tmpDir.mkdirs();

        Home home = null;
        try {
            home = Home.find();
        } catch (Throwable ignored) {
        }
        if (home == null || !home.isDev()) {
            home = new Config.Home(unpackRoboVMDist());
        }
        builder.home(home)
                .tmpDir(tmpDir)
                .skipInstall(true)
                .installDir(installDir);
        if (home.isDev()) {
            builder.useDebugLibs(Boolean.getBoolean("robovm.useDebugLibs"));
            builder.dumpIntermediates(true);
        }

        if (debug != null && !debug.equals("false")) {
            builder.debug(true);
            if (debugPort != -1) {
                builder.addPluginArgument("debug:jdwpport=" + debugPort);
            }
            if ("clientmode".equals(debug)) {
                builder.addPluginArgument("debug:clientmode=true");
            }
        }

        if (iosSkipSigning) {
            builder.iosSkipSigning(true);
        } else {
            if (iosSignIdentity != null) {
                getLog().debug(
                        "Using explicit iOS Signing identity: " + iosSignIdentity);
                builder.iosSignIdentity(SigningIdentity.find(
                        SigningIdentity.list(), iosSignIdentity));
            }

            if (iosProvisioningProfile != null) {
                getLog().debug(
                        "Using explicit iOS provisioning profile: "
                                + iosProvisioningProfile);
                builder.iosProvisioningProfile(ProvisioningProfile.find(
                        ProvisioningProfile.list(), iosProvisioningProfile));
            }
        }

        if (dumpIntermediates) {
            builder.dumpIntermediates(true);
        }

        if (enableBitcode) {
            builder.enableBitcode(true);
        }

        builder.clearClasspathEntries();

        // configure the runtime classpath

        try {
            for (Object object : project.getRuntimeClasspathElements()) {
                String path = (String) object;
                if (getLog().isDebugEnabled()) {
                    getLog().debug(
                            "Including classpath element for RoboVM app: "
                                    + path);
                }
                builder.addClasspathEntry(new File(path));
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(
                    "Error resolving application classpath for RoboVM build", e);
        }

        return builder;
    }

    protected AppCompiler build(OS os, Arch arch, Environment env, String targetType)
            throws MojoExecutionException {

        getLog().info("Building RoboVM app for: " + os + " (" + arch + env.asLlvmSuffix("-") + ")");

        Config.Builder builder;
        builder = new Config.Builder();

        configure(builder).os(os).arch(arch).env(env).targetType(targetType);

        // execute the RoboVM build

        try {

            getLog().info(
                    "Compiling RoboVM app, this could take a while, especially the first time round");
            AppCompiler compiler = new AppCompiler(builder.build());
            compiler.build();

            return compiler;

        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Error building RoboVM executable for app", e);
        }
    }

    protected String getRoboVMVersion() {
        return Version.getVersion();
    }

    protected File unpackRoboVMDist() throws MojoExecutionException {

        Artifact distTarArtifact = resolveRoboVMDistArtifact();
        File distTarFile = distTarArtifact.getFile();
        File unpackBaseDir;
        if (home != null) {
            unpackBaseDir = home;
        } else {
            // by default unpack into the local repo directory
            unpackBaseDir = new File(distTarFile.getParent(), "unpacked");
        }
        if (unpackBaseDir.exists() && distTarArtifact.isSnapshot()) {
            getLog().debug("Deleting directory for unpacked snapshots: " + unpackBaseDir);
            try {
                FileUtils.deleteDirectory(unpackBaseDir);
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failed to delete " + unpackBaseDir, e);
            }
        }
        unpack(distTarFile, unpackBaseDir);
        File unpackedDir = new File(unpackBaseDir, "robovm-" + getRoboVMVersion());
        return unpackedDir;
    }

    protected Artifact resolveRoboVMDistArtifact() throws MojoExecutionException {

        MavenArtifactHandler handler = new MavenArtifactHandler("tar.gz");
        Artifact artifact = new DefaultArtifact("com.mobidevelop.robovm", "robovm-dist",
                getRoboVMVersion(), "", "tar.gz", "nocompiler", handler);
        return resolveArtifact(artifact);
    }

    protected Artifact resolveArtifact(Artifact artifact)
            throws MojoExecutionException {

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(artifact);
        request.setLocalRepository(localRepository);
        final List<ArtifactRepository> remoteRepositories = project
                .getRemoteArtifactRepositories();
        request.setRemoteRepositories(remoteRepositories);

        getLog().debug("Resolving artifact " + artifact);

        ArtifactResolutionResult result = artifactResolver.resolve(request);
        if (!result.isSuccess()) {
            throw new MojoExecutionException("Unable to resolve artifact: "
                    + artifact);
        }
        Collection<Artifact> resolvedArtifacts = result.getArtifacts();
        artifact = resolvedArtifacts.iterator().next();
        return artifact;
    }

    protected void unpack(File archive, File targetDirectory)
            throws MojoExecutionException {

        if (!targetDirectory.exists()) {

            getLog().info("Extracting '" + archive + "' to: " + targetDirectory);
            if (!targetDirectory.mkdirs()) {
                throw new MojoExecutionException(
                        "Unable to create base directory to unpack into: "
                                + targetDirectory);
            }

            try {
                UnArchiver unArchiver = archiverManager.getUnArchiver(archive);
                unArchiver.setSourceFile(archive);
                unArchiver.setDestDirectory(targetDirectory);
                unArchiver.extract();
            } catch (NoSuchArchiverException e) {
                throw new MojoExecutionException("Unable to unpack archive "
                        + archive + " to " + targetDirectory, e);
            }
            getLog().debug(
                    "Archive '" + archive + "' unpacked to: " + targetDirectory);

        } else {
            getLog().debug(
                    "Archive '" + archive + "' was already unpacked in: "
                            + targetDirectory);
        }
    }

    protected Logger getRoboVMLogger() {

        if (roboVMLogger == null) {
            roboVMLogger = new Logger() {
                public void debug(String s, Object... objects) {
                    getLog().debug(String.format(s, objects));
                }

                public void info(String s, Object... objects) {
                    getLog().info(String.format(s, objects));
                }

                public void warn(String s, Object... objects) {
                    getLog().warn(String.format(s, objects));
                }

                public void error(String s, Object... objects) {
                    getLog().error(String.format(s, objects));
                }
            };
        }
        return roboVMLogger;
    }
}
