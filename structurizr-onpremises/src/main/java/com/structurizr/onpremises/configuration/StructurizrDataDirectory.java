package com.structurizr.onpremises.configuration;

import com.structurizr.util.StringUtils;

public final class StructurizrDataDirectory {

    public static final String DEFAULT_DATA_DIRECTORY_PATH = "/usr/local/structurizr";

    private static final String DATA_DIRECTORY_SYSTEM_PROPERTY = "structurizr.dataDirectory";
    private static final String DATA_DIRECTORY_ENVIRONMENT_VARIABLE = "STRUCTURIZR_DATA_DIRECTORY";

    public static String getLocation() {
        String value = getSystemPropertyIgnoreCase();
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }

        value = System.getenv(DATA_DIRECTORY_ENVIRONMENT_VARIABLE);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }

        return DEFAULT_DATA_DIRECTORY_PATH;
    }

    private static String getSystemPropertyIgnoreCase() {
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.equalsIgnoreCase(DATA_DIRECTORY_SYSTEM_PROPERTY)) {
                return System.getProperty(key);
            }
        }

        return null;
    }

}