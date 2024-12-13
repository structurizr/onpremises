package com.structurizr.onpremises.configuration;

import java.util.Properties;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

public class AzureBlobStorageConfigurer extends Configurer {

    public AzureBlobStorageConfigurer(Properties properties) {
        super(properties);
    }

    public void apply() {
        setDefault(AZURE_BLOB_STORAGE_ACCOUNT_NAME, "");
        setDefault(AZURE_BLOB_STORAGE_ACCESS_KEY, "");
        setDefault(AZURE_BLOB_STORAGE_CONTAINER_NAME, "");
    }

}