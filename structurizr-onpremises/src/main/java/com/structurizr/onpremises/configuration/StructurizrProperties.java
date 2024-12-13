package com.structurizr.onpremises.configuration;

public class StructurizrProperties {

    public static final String FILENAME = "structurizr.properties";

    public static final String DATA_DIRECTORY = "structurizr.dataDirectory";

    public static final String WORKSPACE_THREADS = "structurizr.workspace.threads";

    public static final String WORKSPACE_EVENT_LISTENER_PLUGIN = "structurizr.plugin.workspaceEventListener";

    public static final String URL = "structurizr.url";
    public static final String ENCRYPTION_PASSPHRASE = "structurizr.encryption";
    public static final String API_KEY = "structurizr.apiKey";
    public static final String ADMIN_USERS_AND_ROLES = "structurizr.admin";
    public static final String MAX_WORKSPACE_VERSIONS = "structurizr.maxWorkspaceVersions";
    public static final String DSL_EDITOR = "structurizr.dslEditor";
    public static final String INTERNET_CONNECTION = "structurizr.internetConnection";

    public static final String AUTHENTICATION_IMPLEMENTATION = "structurizr.authentication";
    public static final String AUTHENTICATION_VARIANT_FILE = "file";
    public static final String AUTHENTICATION_VARIANT_SAML = "saml";

    public static final String SAML_REGISTRATION_ID = "structurizr.saml.registrationId";
    public static final String SAML_ENTITY_ID = "structurizr.saml.entityId";
    public static final String SAML_METADATA = "structurizr.saml.metadata";
    public static final String SAML_SIGNING_CERTIFICATE = "structurizr.saml.signing.certificate";
    public static final String SAML_SIGNING_PRIVATE_KEY = "structurizr.saml.signing.privateKey";
    public static final String SAML_ATTRIBUTE_USERNAME = "structurizr.saml.attribute.username";
    public static final String SAML_ATTRIBUTE_ROLE = "structurizr.saml.attribute.role";

    public static final String SESSION_IMPLEMENTATION = "structurizr.session";
    public static final String SESSION_VARIANT_LOCAL = "local";
    public static final String SESSION_VARIANT_REDIS = "redis";

    public static final String DEFAULT_MAX_WORKSPACE_VERSIONS = "30";
    public static final String DEFAULT_NUMBER_OF_THREADS = "50";

    public static final String DATA_STORAGE_IMPLEMENTATION = "structurizr.data";
    public static final String DATA_STORAGE_VARIANT_FILE = "file";
    public static final String DATA_STORAGE_VARIANT_AMAZON_WEB_SERVICES_S3 = "aws-s3";
    public static final String AWS_S3_ACCESS_KEY_ID = "aws-s3.accessKeyId";
    public static final String AWS_S3_SECRET_ACCESS_KEY = "aws-s3.secretAccessKey";
    public static final String AWS_S3_REGION = "aws-s3.region";
    public static final String AWS_S3_BUCKET_NAME = "aws-s3.bucketName";
    public static final String AWS_S3_ENDPOINT = "aws-s3.endpoint";
    public static final String AWS_S3_PATH_STYLE_ACCESS = "aws-s3.pathStyleAccess";
    public static final String DATA_STORAGE_VARIANT_AZURE_BLOB_STORAGE = "azure-blob";
    public static final String AZURE_BLOB_STORAGE_ACCOUNT_NAME = "azure-blob.accountName";
    public static final String AZURE_BLOB_STORAGE_ACCESS_KEY = "azure-blob.accessKey";
    public static final String AZURE_BLOB_STORAGE_CONTAINER_NAME = "azure-blob.containerName";

    public static final String SEARCH_IMPLEMENTATION = "structurizr.search";
    public static final String SEARCH_VARIANT_NONE = "none";
    public static final String SEARCH_VARIANT_LUCENE = "lucene";

    public static final String SEARCH_VARIANT_ELASTICSEARCH = "elasticsearch";
    public static final String ELASTICSEARCH_ENDPOINT = "elasticsearch.endpoint";
    public static final String ELASTICSEARCH_PROTOCOL = "elasticsearch.protocol";
    public static final String ELASTICSEARCH_HOST = "elasticsearch.host";
    public static final String ELASTICSEARCH_PORT = "elasticsearch.port";
    public static final String ELASTICSEARCH_USERNAME = "elasticsearch.username";
    public static final String ELASTICSEARCH_PASSWORD = "elasticsearch.password";

    public static final String CACHE_IMPLEMENTATION = "structurizr.cache";
    public static final String CACHE_EXPIRY_IN_MINUTES = "structurizr.cache.expiry";
    public static final String DEFAULT_CACHE_EXPIRY_IN_MINUTES = "5";
    public static final String CACHE_VARIANT_NONE = "none";
    public static final String CACHE_VARIANT_LOCAL = "local";
    public static final String CACHE_VARIANT_REDIS = "redis";

    public static final String REDIS_ENDPOINT = "structurizr.redis.endpoint";
    public static final String REDIS_PROTOCOL = "structurizr.redis.protocol";
    public static final String REDIS_SSL = "structurizr.redis.ssl";
    public static final String REDIS_HOST = "structurizr.redis.host";
    public static final String REDIS_PORT = "structurizr.redis.port";
    public static final String REDIS_PASSWORD = "structurizr.redis.password";
    public static final String REDIS_DATABASE = "structurizr.redis.database";

}