package com.structurizr.onpremises.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.structurizr.util.StringUtils;

public class AmazonS3ClientUtils {

    public static AmazonS3 create(String accessKeyId, String secretAccessKey, String region, String endpoint, boolean pathStyleAccessEnabled) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        if (!StringUtils.isNullOrEmpty(endpoint)) {
            builder.withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(endpoint, region));
        } else if (!StringUtils.isNullOrEmpty(region)) {
            builder.withRegion(region);
        }

        builder.withPathStyleAccessEnabled(pathStyleAccessEnabled);

        if (!StringUtils.isNullOrEmpty(accessKeyId) && !StringUtils.isNullOrEmpty(secretAccessKey)) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)));
        }

        return builder.build();
    }

}