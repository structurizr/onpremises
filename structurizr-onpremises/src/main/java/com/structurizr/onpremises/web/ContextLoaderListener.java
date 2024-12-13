package com.structurizr.onpremises.web;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.autolayout.graphviz.GraphvizAutomaticLayout;
import com.structurizr.importer.documentation.DefaultDocumentationImporter;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.Features;
import com.structurizr.onpremises.configuration.StructurizrDataDirectory;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.onpremises.util.*;
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

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

public class ContextLoaderListener implements ServletContextListener {

    private static final String LOGS_DIRECTORY_NAME = "logs";
    private static final String LOG4J_PROPERTIES_FILENAME = "log4j2.properties";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // push the Structurizr data directory into a system property, so it can be used to import Spring context files
        // (this can help keep, e.g., an installation's LDAP configuration separate to the application)
        File structurizrDataDirectory = new File(StructurizrDataDirectory.getLocation());
        System.setProperty(StructurizrProperties.DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());

        Properties properties = new Properties();
        try {
            File propertiesFile = new File(structurizrDataDirectory, StructurizrProperties.FILENAME);
            if (propertiesFile.exists()) {
                properties.load(new FileReader(propertiesFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            logConfiguration(StructurizrProperties.DATA_DIRECTORY, structurizrDataDirectory + " (r: " + structurizrDataDirectory.canRead() + "; w: " + structurizrDataDirectory.canWrite() + "; x: " + structurizrDataDirectory.canExecute() + ")");
            logConfiguration(StructurizrProperties.ENCRYPTION_PASSPHRASE, !StringUtils.isNullOrEmpty(Configuration.getInstance().getProperty(StructurizrProperties.ENCRYPTION_PASSPHRASE)) ? "********" : "");
            logConfiguration(StructurizrProperties.URL, Configuration.getInstance().getWebUrl());
            logConfiguration(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION));
            logConfiguration(StructurizrProperties.API_KEY, !StringUtils.isNullOrEmpty(Configuration.getInstance().getProperty(StructurizrProperties.API_KEY)) ? "********" : "");

            String sessionImplementation = Configuration.getInstance().getProperty(SESSION_IMPLEMENTATION);
            if (sessionImplementation.equals(SESSION_VARIANT_REDIS)) {
                logConfiguration(SESSION_IMPLEMENTATION, sessionImplementation + " (" + Configuration.getInstance().getProperty(REDIS_ENDPOINT) + ")");
            } else {
                logConfiguration(StructurizrProperties.SESSION_IMPLEMENTATION, sessionImplementation);
            }

            logConfiguration(StructurizrProperties.DATA_STORAGE_IMPLEMENTATION, Configuration.getInstance().getProperty(DATA_STORAGE_IMPLEMENTATION));

            String cacheImplementation = Configuration.getInstance().getProperty(CACHE_IMPLEMENTATION);
            if (cacheImplementation.equals(CACHE_VARIANT_REDIS)) {
                logConfiguration(StructurizrProperties.CACHE_IMPLEMENTATION, cacheImplementation + " (" + Configuration.getInstance().getProperty(REDIS_ENDPOINT) + ")");
            } else {
                logConfiguration(StructurizrProperties.CACHE_IMPLEMENTATION, cacheImplementation);
            }

            logConfiguration(StructurizrProperties.SEARCH_IMPLEMENTATION, Configuration.getInstance().getProperty(SEARCH_IMPLEMENTATION));
            logConfiguration(StructurizrProperties.INTERNET_CONNECTION, Configuration.getInstance().hasInternetConnection());

            log.info("");
            logConfiguration(Features.UI_DSL_EDITOR, Configuration.getInstance().isFeatureEnabled(Features.UI_DSL_EDITOR));
            logConfiguration(Features.WORKSPACE_ARCHIVING, Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_ARCHIVING));
            logConfiguration(Features.WORKSPACE_BRANCHES, Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES));
            logConfiguration(Features.WORKSPACE_SCOPE_VALIDATION, (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_SCOPE_VALIDATION) ? "strict" : "relaxed"));

            if (Configuration.getInstance().getWorkspaceEventListener() != null) {
                log.info("");
                log.info("Workspace event listener: " + Configuration.getInstance().getWorkspaceEventListener().getClass().getName());
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
            log.info("Graphviz (dot): " + Configuration.getInstance().isGraphvizEnabled());

            log.info("Memory: used=" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB; free=" + (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB; total=" + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB; max=" + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
            log.info("***********************************************************************************");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logConfiguration(String name, String value) {
        Log log = LogFactory.getLog(ContextLoaderListener.class);
        log.info(name + ": " + (StringUtils.isNullOrEmpty(value) ? "(not set)" : value));
    }

    private void logConfiguration(String name, boolean value) {
        Log log = LogFactory.getLog(ContextLoaderListener.class);
        log.info(name + ": " + value);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Log log = LogFactory.getLog(Configuration.class);
        log.info("********************************************************");
        log.info(" Stopping Structurizr");
        log.info("********************************************************");
    }

}