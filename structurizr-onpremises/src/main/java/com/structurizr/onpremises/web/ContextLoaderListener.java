package com.structurizr.onpremises.web;

import com.structurizr.Workspace;
import com.structurizr.autolayout.graphviz.GraphvizAutomaticLayout;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.importer.documentation.DefaultDocumentationImporter;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrDataDirectory;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.onpremises.util.DateUtils;
import com.structurizr.onpremises.util.Version;
import com.structurizr.util.StringUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

public class ContextLoaderListener implements ServletContextListener {

    private static final String LOGS_DIRECTORY_NAME = "logs";
    private static final String LOG4J_PROPERTIES_FILENAME = "log4j2.properties";

    private static final String PROPERTY_NAME_PREFIX = "structurizr.";
    private static final String ENVIRONMENT_VARIABLE_NAME_PREFIX = "structurizr_";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // push the Structurizr data directory into a system property, so it can be used to import Spring context files
        // (this can help keep, e.g., an installation's LDAP configuration separate to the application)
        File structurizrDataDirectory = new File(StructurizrDataDirectory.getLocation());

        Properties properties = loadProperties(new File(structurizrDataDirectory, StructurizrProperties.FILENAME));
        loadSystemProperties(properties);
        loadEnvironmentVariables(properties);

        properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());
        System.setProperty(StructurizrProperties.DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());

        // reconfigure the logging system
        LoggerContext loggerContext = (LoggerContext)LogManager.getContext(false);
        File log4jProperties = new File(structurizrDataDirectory, LOG4J_PROPERTIES_FILENAME);
        if (log4jProperties.exists()) {
            // if a log4j2.properties file exists inside the Structurizr data directory, use that
            loggerContext.setConfigLocation(log4jProperties.toURI());
            loggerContext.reconfigure();
        } else {
            // otherwise use the built-in version, which resides on the application classpath
            loggerContext.reconfigure();
        }

        File logDirectory = new File(structurizrDataDirectory, LOGS_DIRECTORY_NAME);
        if (!logDirectory.exists()) {
            try {
                Files.createDirectories(logDirectory.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

            private final Log log = LogFactory.getLog(UncaughtExceptionHandler.class);

            public void uncaughtException(Thread t, Throwable ex) {
                log.error("Uncaught exception in thread: " + t.getName(), ex);
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        Log log = LogFactory.getLog(ContextLoaderListener.class);
        Configuration.init(properties);

        try {
            log.info("***********************************************************************************");
            log.info("MIT License");
            log.info("");
            log.info("Copyright (c) 2024 Structurizr Limited");
            log.info("");
            log.info("Permission is hereby granted, free of charge, to any person obtaining a copy");
            log.info("of this software and associated documentation files (the \"Software\"), to deal");
            log.info("in the Software without restriction, including without limitation the rights");
            log.info("to use, copy, modify, merge, publish, distribute, sublicense, and/or sell");
            log.info("copies of the Software, and to permit persons to whom the Software is");
            log.info("furnished to do so, subject to the following conditions:");
            log.info("");
            log.info("The above copyright notice and this permission notice shall be included in all");
            log.info("copies or substantial portions of the Software.");
            log.info("");
            log.info("THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR");
            log.info("IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,");
            log.info("FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE");
            log.info("AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER");
            log.info("LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,");
            log.info("OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE");
            log.info("SOFTWARE.");
            log.info("***********************************************************************************");
            log.info("  _____ _                   _              _          ");
            log.info(" / ____| |                 | |            (_)         ");
            log.info("| (___ | |_ _ __ _   _  ___| |_ _   _ _ __ _ _____ __ ");
            log.info(" \\___ \\| __| '__| | | |/ __| __| | | | '__| |_  / '__|");
            log.info(" ____) | |_| |  | |_| | (__| |_| |_| | |  | |/ /| |   ");
            log.info("|_____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  |_/___|_|   ");
            log.info("                                                      ");
            log.info("Structurizr on-premises installation");
            log.info(" - build: " + new Version().getBuildNumber() + " (" + DateUtils.formatIsoDate(new Version().getBuildTimestamp()) + ")");

            try {
                log.info(" - structurizr-java: v" + Class.forName(Workspace.class.getCanonicalName()).getPackage().getImplementationVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                log.info(" - structurizr-dsl: v" + Class.forName(StructurizrDslParser.class.getCanonicalName()).getPackage().getImplementationVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                log.info(" - structurizr-import: v" + Class.forName(DefaultDocumentationImporter.class.getCanonicalName()).getPackage().getImplementationVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                log.info(" - structurizr-graphviz: v" + Class.forName(GraphvizAutomaticLayout.class.getCanonicalName()).getPackage().getImplementationVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.info("");
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("dot", "-V");
                Process process = processBuilder.start();
                int exitCode = process.waitFor();

                String input = new String(process.getInputStream().readAllBytes());
                String error = new String(process.getErrorStream().readAllBytes());
                Configuration.getInstance().setGraphvizEnabled(exitCode == 0);

                log.debug("Running: dot -V");
                log.debug("stdout: " + input);
                log.debug("stderr: " + error);
            } catch (Exception e) {
                log.error(e);
            }
            log.info("Graphviz (dot) available: " + Configuration.getInstance().isGraphvizEnabled());

            log.info("Memory: used=" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB; free=" + (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB; total=" + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB; max=" + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
            log.info("***********************************************************************************");

            logAllProperties(log, properties);
            log.info("***********************************************************************************");

            if (!Boolean.parseBoolean(Configuration.getInstance().getProperty(INTERNET_CONNECTION))) {
                log.warn(INTERNET_CONNECTION + " has been deprecated; use structurizr.urls.allowed instead");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // todo: https://github.com/structurizr/onpremises/issues/136
        System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
    }

    private void logAllProperties(Log log, Properties properties) {
        log.info("Configuration:");

        Set<String> propertiesToMask = Set.of(
                ENCRYPTION_PASSPHRASE, API_KEY, AWS_S3_SECRET_ACCESS_KEY, AZURE_BLOB_STORAGE_ACCESS_KEY, ELASTICSEARCH_PASSWORD, REDIS_PASSWORD
        );

        Set<String> propertyNames = new TreeSet<>(properties.stringPropertyNames());
        for (String name : propertyNames) {
            String value = properties.getProperty(name);
            if (propertiesToMask.contains(name)) {
                if (!StringUtils.isNullOrEmpty(value)) {
                    log.info(" - " + name + ": ********");
                } else {
                    log.info(" - " + name + ":");
                }
            } else {
                log.info(" - " + name + ": " + value);
            }
        }
    }

    private Properties loadProperties(File file) {
        Properties propertiesFromFile = new Properties();
        Properties properties = new Properties();
        try {
            if (file.exists()) {
                propertiesFromFile.load(new FileReader(file));

                for (String key : propertiesFromFile.stringPropertyNames()) {
                    String name = key.toLowerCase();
                    if (name.startsWith(PROPERTY_NAME_PREFIX)) {
                        properties.setProperty(name, propertiesFromFile.getProperty(key));
                    } else {
                        properties.setProperty(PROPERTY_NAME_PREFIX + name, propertiesFromFile.getProperty(key));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private void loadSystemProperties(Properties properties) {
        for (String name : System.getProperties().stringPropertyNames()) {
            String lowerCaseName = name.toLowerCase();
            if (lowerCaseName.startsWith(PROPERTY_NAME_PREFIX)) {
                properties.setProperty(lowerCaseName, System.getProperty(name));
            }
        }
    }

    private void loadEnvironmentVariables(Properties properties) {
        for (String name : System.getenv().keySet()) {
            String lowerCaseName = name.toLowerCase();
            if (lowerCaseName.startsWith(ENVIRONMENT_VARIABLE_NAME_PREFIX)) {
                lowerCaseName = lowerCaseName.replace('_', '.');
                properties.setProperty(lowerCaseName, System.getenv(name));
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Log log = LogFactory.getLog(Configuration.class);
        log.info("********************************************************");
        log.info(" Stopping Structurizr");
        log.info("********************************************************");
    }

}