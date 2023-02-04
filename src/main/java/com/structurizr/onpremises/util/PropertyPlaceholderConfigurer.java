package com.structurizr.onpremises.util;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public final class PropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer {

    private static final String STRUCTURIZR_PROPERTIES_FILENAME = "structurizr.properties";
    private static final String STRUCTURIZR_DATA_DIRECTORY_PROPERTY_NAME = "structurizr.dataDirectory";

    public PropertyPlaceholderConfigurer() throws Exception {
        Properties properties = new Properties();
        File dataDirectory = Configuration.getInstance().getDataDirectory();
        File propertiesFile = new File(dataDirectory, STRUCTURIZR_PROPERTIES_FILENAME);
        if (propertiesFile.exists()) {
            properties.load(new FileReader(propertiesFile));
        }

        properties.setProperty(STRUCTURIZR_DATA_DIRECTORY_PROPERTY_NAME, dataDirectory.getAbsolutePath());

        setLocalOverride(true);
        setProperties(properties);
        setIgnoreUnresolvablePlaceholders(false);
    }

}