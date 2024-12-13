package com.structurizr.onpremises.configuration;

import com.structurizr.util.StringUtils;

public final class StructurizrDataDirectory {

    private static final String DATA_DIRECTORY_ENVIRONMENT_VARIABLE_NAME = "STRUCTURIZR_DATA_DIRECTORY";
    private static final String DEFAULT_DATA_DIRECTORY_PATH = "/usr/local/structurizr";

    public static String getLocation() {
        String value = System.getProperty(StructurizrProperties.DATA_DIRECTORY);
        if (StringUtils.isNullOrEmpty(value)) {
            value = System.getenv(DATA_DIRECTORY_ENVIRONMENT_VARIABLE_NAME);

            if (StringUtils.isNullOrEmpty(value)) {
                value = DEFAULT_DATA_DIRECTORY_PATH;
            }
        }

        return value;
    }

}