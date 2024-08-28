/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.integration.jee;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.*;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.exteps.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Artifact {

    private static final Logger LOG = LoggerFactory.getLogger(Artifact.class);
    private static final String JBOSS_DEPLOYMENT_STRUCTURE_XML_NAME = "jboss-deployment-structure.xml";
    private static final String SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME = "ServiceFrameworkConfiguration.properties";
    private static final String BEANS_XML_NAME = "beans.xml";
    private static final String TEST_JAR_NAME = "test.jar";
    public static final String EPS_WATCHED_FOLDER_PROPERTY_NAME = "com.ericsson.oss.services.eps.module.deployment.folder.path";
    public static final String CONFIGURATION_FILE_NAME = "EpsConfiguration.properties";

    public static final String COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR = "com.ericsson.oss.services.eps:eps-jee-war:war";
    public static final String COM_ERICSSON_OSS_ITPF_SERVICES_EPS_DIST_JAR = "com.ericsson.oss.services.eps:eps-jboss-module-dist:jar";
    public static final String SERVICE_FRAMEWORK_DIST_JAR = "com.ericsson.oss.itpf.sdk:service-framework-dist:jar";
    public static final String PERSISTENCE_FILE_NAME = "persistence.xml";

    public static File resolveArtifactWithoutDependencies(
            final String artifactCoordinates) {
        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve(artifactCoordinates).withoutTransitivity().asFile();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates "
                    + artifactCoordinates + " was not resolved");
        }

        if (artifacts.length != 1) {

            throw new IllegalStateException(
                    "Resolved more then one artifact with coordinates "
                            + artifactCoordinates);
        }
        LOG.debug("Found file artifact {}", artifacts[0]);

        return artifacts[0];
    }

    public static File[] resolveArtifactWithDependencies(
            final String artifactCoordinates) {

        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve(artifactCoordinates).withTransitivity().asFile();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates "
                    + artifactCoordinates + " was not resolved");
        }

        LOG.debug("Found file artifact {}", (Object[]) artifacts);

        return artifacts;
    }

    public static Properties loadProperties() {

        final Properties prop = new Properties();

        final InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("projectProperties.properties");
        try {
            prop.load(inputStream);
        } catch (final IOException e) {
            LOG.error("Cannot load properties: ", e);
        }

        return prop;
    }

    public static HazelcastInstance createHazelcastInstance() {
        final Config cfg = new Config();
        cfg.setClassLoader(Thread.currentThread().getContextClassLoader());
        final HazelcastInstance hzInstance = Hazelcast
                .newHazelcastInstance(cfg);
        return hzInstance;
    }

    public static boolean copyFlowFile(final String testFile,
            final InputStream inputStream, final String folderName)
            throws IOException {
        final int indexOfSlash = testFile.lastIndexOf("/");
        String simpleFileName = testFile;
        if (indexOfSlash != -1) {
            simpleFileName = testFile.substring(indexOfSlash + 1,
                    testFile.length());
            LOG.debug("Simple file name is {}", simpleFileName);
        }
        simpleFileName = simpleFileName.replace(".xml", "");

        if (inputStream == null) {
            throw new IllegalArgumentException("Was not able to find "
                    + testFile + " in classpath!");
        }
        final File watchedFolder = new File(folderName);
        if (!watchedFolder.exists() || !watchedFolder.isDirectory()) {
            throw new IllegalStateException(folderName
                    + " is not valid folder!");
        }
        final Random random = new Random();
        final String finalFileNameRoot = simpleFileName + "_eps_module_"
                + random.nextInt(1000) + "_" + System.currentTimeMillis();
        final String tmpfileName = finalFileNameRoot + ".tmp";
        final String xmlFileName = finalFileNameRoot + ".xml";
        LOG.debug("Trying to create file {} in folder {}", tmpfileName,
                folderName);
        final File tmpDestinationFile = new File(watchedFolder, tmpfileName);
        final File xmlDestinationFile = new File(watchedFolder, xmlFileName);
        final Path tmpDestinationPath = tmpDestinationFile.toPath();
        final Path xmlDestinationPath = xmlDestinationFile.toPath();
        try {
            final long bytesWritten = Files.copy(inputStream,
                    tmpDestinationPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.debug(
                    "Successfully wrote {} bytes into temporary file. Now renaming to {}",
                    bytesWritten, xmlFileName);
            Files.move(tmpDestinationPath, xmlDestinationPath,
                    REPLACE_EXISTING, ATOMIC_MOVE);
            LOG.debug("Moved file to {}", xmlFileName);
            return bytesWritten > 1;
        } catch (final IOException ie) {
            LOG.error("Failed to rename file to {}", xmlFileName);
            throw ie;
        } finally {
            inputStream.close();
        }
    }

    public static boolean wait(final int milliseconds) {
        LOG.debug("Waiting " + milliseconds + " ms");
        try {
            Thread.sleep(milliseconds);
            return true;
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean waitForModuleDeploy(final int moduleCount,
            final int retry) throws InterruptedException {

        LOG.info(
                "\n*\n*\n* Artifact::waitForModuleDeploy: ModuleCount {} Retry: {}\n*\n*",
                moduleCount, retry);

        ModuleManager modulesManager = null;
        try {
            modulesManager = LoadingUtil
                    .loadSingletonInstance(ModuleManager.class);
        } catch (final Exception ex) {
            LOG.error("Cannot load ModuleManager", ex);
            return false;
        }

        boolean isModuleDeployed = false;
        int iterations = 0;
        while (!isModuleDeployed && (iterations < retry)) {

            Thread.sleep(5000);
            final int count = modulesManager.getDeployedModulesCount();
            LOG.info("\n*\n*\n* Artifact::ModuleManager: {} Count: {}\n*\n*",
                    modulesManager, count);

            iterations++;

            if (count == 1) {
                isModuleDeployed = true;
                break;
            }
        }

        return isModuleDeployed;
    }

    public static void deployFlowFile(final String flowFile,
            final String configurationFileName) throws IOException {
        final Properties properties = new Properties();
        InputStream propertiesIs = null;
        InputStream flowIs = null;
        boolean copied = false;
        try {
            LOG.debug("Loading properties from {}", configurationFileName);
            propertiesIs = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath",
                        configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }

            final String folderName = properties
                    .getProperty(Artifact.EPS_WATCHED_FOLDER_PROPERTY_NAME);
            flowIs = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(flowFile);
            copied = Artifact.copyFlowFile(flowFile, flowIs, folderName);
        } catch (final IOException ie) {
            LOG.error(
                    "loadProperties IOexception on loading file {} from classpath",
                    ie);
            throw ie;
        } finally {
            Assert.assertNotNull("Configuration file stream is null ",
                    propertiesIs);
            flowIs.close();
            propertiesIs.close();
            Assert.assertTrue("File has not been copied ", copied);
        }
    }

    public static List<File> getCsvFiles() {

        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.endsWith(".csv")) {
                    return true;
                }
                return false;
            }
        };
        return Arrays
                .asList(new File(
                        EPSConfigurationLoader
                                .getConfigurationProperty(EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME))
                        .listFiles(filter));
    }

    public static void createCsvOutputLocation(
            final String configurationFileName) {
        LOG.debug("Loading properties from {}", configurationFileName);
        final Properties properties = new Properties();
        InputStream propertiesIs = null;

        try {
            final ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            propertiesIs = classLoader
                    .getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath",
                        configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }
        } catch (final IOException ie) {
            LOG.error(
                    "loadProperties IOexception on loading file {} from classpath",
                    configurationFileName);
            ie.printStackTrace();

        } finally {
            Assert.assertNotNull("Configuration file stream is null ",
                    propertiesIs);
            try {
                propertiesIs.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        final String csvLocationPath = properties
                .getProperty(EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME);

        Assert.assertNotNull("CSV files location directory is null ",
                csvLocationPath);

        LOG.debug("Trying to create directory {}", csvLocationPath);
        final File csvFolder = new File(csvLocationPath);
        if (!csvFolder.exists()) {
            Assert.assertTrue(csvFolder.mkdirs());
        }
    }

    public static void createModuleOutputLocation(
            final String configurationFileName) {
        LOG.debug("Loading properties from {}", configurationFileName);
        final Properties properties = new Properties();
        InputStream propertiesIs = null;

        try {
            final ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            propertiesIs = classLoader
                    .getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath",
                        configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }
        } catch (final IOException ie) {
            LOG.error(
                    "loadProperties IOexception on loading file {} from classpath",
                    configurationFileName);
            ie.printStackTrace();

        } finally {
            Assert.assertNotNull("Configuration file stream is null ",
                    propertiesIs);
            try {
                propertiesIs.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        final String moduleDeploymentFolder = properties
                .getProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);

        Assert.assertNotNull(
                "Module Deployment files location directory is null ",
                moduleDeploymentFolder);

        LOG.debug("Trying to create directory {}", moduleDeploymentFolder);
        final File moduleFolder = new File(moduleDeploymentFolder);
        if (!moduleFolder.exists()) {
            Assert.assertTrue(moduleFolder.mkdirs());
        }
    }

    static EnterpriseArchive buildEar(final String earName,
            final String epsWarCoordinates, final String flowFileName,
            final String epsConfigFileName, final String manifestFile,
            final String jbossDeploymentFile, final Class<?> testClass,
            final List<Class<?>> handlerClasses,
            final List<Class<?>> ejbJarClasses) {
        LOG.info("Deploying: " + earName);

        final File archiveFile = resolveArtifactWithoutDependencies(epsWarCoordinates);

        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact "
                    + epsWarCoordinates);
        }

        final WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class,
                archiveFile);
        war.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);

        final JavaArchive jarLib = ShrinkWrap
                .create(JavaArchive.class, TEST_JAR_NAME).addClass(testClass)
                .addClass(CommonExtensionTest.class);
        jarLib.addClass(Artifact.class).addPackage(
                HazelcastInputListener.class.getPackage());
        if ((handlerClasses != null) && !handlerClasses.isEmpty()) {
            jarLib.addClasses(handlerClasses
                    .toArray(new Class<?>[handlerClasses.size()]));
        }
        jarLib.addAsResource(flowFileName, "flow.xml");
        jarLib.addAsResource(epsConfigFileName, CONFIGURATION_FILE_NAME);
        jarLib.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);
        jarLib.addAsManifestResource("persistence.xml", PERSISTENCE_FILE_NAME);

        final JavaArchive jarEjb = ShrinkWrap.create(JavaArchive.class,
                "test-ejb.jar");

        if ((ejbJarClasses != null) && !ejbJarClasses.isEmpty()) {
            jarEjb.addClasses(ejbJarClasses.toArray(new Class<?>[ejbJarClasses
                    .size()]));
        }

        jarEjb.addAsResource(
                "services/ServiceFrameworkConfiguration.properties",
                SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME);
        jarEjb.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);

        final EnterpriseArchive ear = ShrinkWrap.create(
                EnterpriseArchive.class, earName);

        ear.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.hazelcast:hazelcast:jar:?").withTransitivity()
                .asFile());

        ear.addAsManifestResource(manifestFile, "MANIFEST.MF");
        ear.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);
        ear.addAsModules(jarEjb);
        ear.addAsModules(war);
        ear.addAsLibraries(jarLib);
        if (jbossDeploymentFile != null) {
            ear.addAsManifestResource(jbossDeploymentFile,
                    JBOSS_DEPLOYMENT_STRUCTURE_XML_NAME);
        }

        return ear;
    }
}
