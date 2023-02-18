package com.structurizr.onpremises.web;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.importer.documentation.DefaultDocumentationImporter;
import com.structurizr.onpremises.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class ContextLoaderListener implements ServletContextListener {

    private static final String LOGS_DIRECTORY_NAME = "logs";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // push the Structurizr data directory into a system property, so it can be used to import Spring context files
        // (this can help keep, e.g., an installation's LDAP configuration separate to the application)
        File structurizrDataDirectory = new File(ConfigLookup.getDataDirectoryLocation());
        System.setProperty(ConfigLookup.DATA_DIRECTORY_SYSTEM_PROPERTY_NAME, structurizrDataDirectory.getAbsolutePath());

        try {
            Properties properties = new Properties();
            File propertiesFile = new File(structurizrDataDirectory, StructurizrProperties.FILENAME);
            if (propertiesFile.exists()) {
                properties.load(new FileReader(propertiesFile));
                System.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION_PROPERTY, properties.getProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION_PROPERTY, StructurizrProperties.DEFAULT_AUTHENTICATION_VARIANT));
                System.setProperty(StructurizrProperties.SESSION_IMPLEMENTATION_PROPERTY, properties.getProperty(StructurizrProperties.SESSION_IMPLEMENTATION_PROPERTY, StructurizrProperties.DEFAULT_SESSION_VARIANT));
            } else {
                System.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION_PROPERTY, StructurizrProperties.DEFAULT_AUTHENTICATION_VARIANT);
                System.setProperty(StructurizrProperties.SESSION_IMPLEMENTATION_PROPERTY, StructurizrProperties.DEFAULT_SESSION_VARIANT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();


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
        Configuration.init();

        try {
            log.info("***********************************************************************************");
            log.info("  _____ _                   _              _          ");
            log.info(" / ____| |                 | |            (_)         ");
            log.info("| (___ | |_ _ __ _   _  ___| |_ _   _ _ __ _ _____ __ ");
            log.info(" \\___ \\| __| '__| | | |/ __| __| | | | '__| |_  / '__|");
            log.info(" ____) | |_| |  | |_| | (__| |_| |_| | |  | |/ /| |   ");
            log.info("|_____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  |_/___|_|   ");
            log.info("                                                      ");
            log.info("Structurizr on-premises installation");
            log.info(" - build: " + new Version().getBuildNumber() + " (" + DateUtils.formatIsoDate(new Version().getBuildTimestamp()));

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

            log.info("Data directory: " + structurizrDataDirectory + " (r: " + structurizrDataDirectory.canRead() + "; w: " + structurizrDataDirectory.canWrite() + "; x: " + structurizrDataDirectory.canExecute() + ")");
            log.info("URL: " + Configuration.getInstance().getWebUrl());
            log.info("Memory: used=" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB; free=" + (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB; total=" + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB; max=" + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
            log.info("");
            log.info("Authentication: " + Configuration.getInstance().getAuthenticationVariant());
            log.info("Session: " + Configuration.getInstance().getSessionVariant());
            log.info("Data storage: " + Configuration.getInstance().getDataStorageImplementationName());
            log.info("Search: " + Configuration.getInstance().getSearchImplementationName());
            log.info("dot: " + Configuration.getInstance().isGraphvizEnabled());
            log.info("DSL editor: " + Configuration.getInstance().isDslEditorEnabled());

            log.info("***********************************************************************************");
            log.info("MIT License");
            log.info("");
            log.info("Copyright (c) 2023 Structurizr Limited");
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
        } catch (Exception e) {
            e.printStackTrace();
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