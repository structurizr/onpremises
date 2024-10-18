package com.structurizr.onpremises.util;

import com.structurizr.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class ConfigLookup {

    public static final String DATA_DIRECTORY_SYSTEM_PROPERTY_NAME = "structurizr.dataDirectory";
    private static final String DATA_DIRECTORY_ENVIRONMENT_VARIABLE_NAME = "STRUCTURIZR_DATA_DIRECTORY";
    private static final String DEFAULT_DATA_DIRECTORY_PATH = "/usr/local/structurizr";
    private static Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([\\w]*)\\}");

    public static String getDataDirectoryLocation() {
        return getConfigurationParameter(DATA_DIRECTORY_SYSTEM_PROPERTY_NAME, DATA_DIRECTORY_ENVIRONMENT_VARIABLE_NAME, null, DEFAULT_DATA_DIRECTORY_PATH);
    }

    static String getEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (StringUtils.isNullOrEmpty(value)) {
            return null;
        } else {
            return value;
        }
    }

    static String getSystemProperty(String name) {
        String value = System.getProperty(name);
        if (StringUtils.isNullOrEmpty(value)) {
            return null;
        } else {
            return value;
        }
    }

    static String getConfigurationParameter(String systemPropertyName, String environmentVariableName, String structurizrPropertyName, String defaultValue) {
        String value = getSystemProperty(systemPropertyName);
        if (value == null) {
            value = getEnvironmentVariable(environmentVariableName);

            if (value == null) {
                if (structurizrPropertyName != null) {
                    value = getConfigurationParameterFromStructurizrPropertiesFile(structurizrPropertyName, defaultValue);
                } else {
                    value = defaultValue;
                }
            }
        }

        return value;
    }

    public static String getConfigurationParameterFromStructurizrPropertiesFile(String structurizrPropertyName, String defaultValue) {
        String value = null;

        File file = new File(getDataDirectoryLocation(), StructurizrProperties.FILENAME);
        if (file.exists()) {
            try {
                Properties properties = new Properties();
                FileReader fileReader = new FileReader(file);
                properties.load(fileReader);

                if (properties.containsKey(structurizrPropertyName)) {
                    value = properties.getProperty(structurizrPropertyName);
                }
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // translate all ${...} within a string into a value, substituting any
        // environment variables along the way
        if (value != null) {
            value = getValueFromEnvironmentVariables(value);
        }

        if (value == null) {
            value = defaultValue;
        }

        if (value != null) {
            value = value.trim();
        }

        return value;
    }

    private static String getValueFromEnvironmentVariables(String original)
    {
        Matcher matcher = ENV_VAR_PATTERN.matcher(original);
        
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        while(matcher.find())
        {
            output.append(original, lastIndex, matcher.start())
                .append(System.getenv(matcher.group(1)));

            lastIndex = matcher.end();
        }
        
        if (lastIndex < original.length()) {
            output.append(original, lastIndex, original.length());
        }
        
        return output.toString();
    }
}