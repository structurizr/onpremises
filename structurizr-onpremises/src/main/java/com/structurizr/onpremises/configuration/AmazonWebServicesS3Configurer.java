package com.structurizr.onpremises.configuration;

import java.util.Properties;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

class AmazonWebServicesS3Configurer extends Configurer {

    AmazonWebServicesS3Configurer(Properties properties) {
        super(properties);
    }

    void apply() {
        setDefault(AWS_S3_ACCESS_KEY_ID, "");
        setDefault(AWS_S3_SECRET_ACCESS_KEY, "");
        setDefault(AWS_S3_REGION, "");
        setDefault(AWS_S3_BUCKET_NAME, "");
        setDefault(AWS_S3_ENDPOINT, "");
        setDefault(AWS_S3_PATH_STYLE_ACCESS, "false");
    }

}