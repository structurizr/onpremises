package com.structurizr.onpremises.util;

public class StructurizrProperties {

    public static final String FILENAME = "structurizr.properties";

    public static final String DATA_STORAGE_IMPLEMENTATION_PROPERTY = "structurizr.data";
    public static final String SEARCH_IMPLEMENTATION_PROPERTY = "structurizr.search";
    public static final String AUTHENTICATION_IMPLEMENTATION_PROPERTY = "structurizr.authentication";
    public static final String SESSION_IMPLEMENTATION_PROPERTY = "structurizr.session";

    public static final String CACHE_IMPLEMENTATION_PROPERTY = "structurizr.cache";
    public static final String CACHE_EXPIRY_IN_MINUTES_PROPERTY = "structurizr.cache.expiry";

    public static final String WORKSPACE_EVENT_LISTENER_PLUGIN_PROPERTY = "structurizr.plugin.workspaceEventListener";

    public static final String URL_PROPERTY = "structurizr.url";
    public static final String ENCRYPTION_PASSPHRASE_PROPERTY = "structurizr.encryption";
    public static final String API_KEY_PROPERTY = "structurizr.apiKey";
    public static final String ADMIN_USERS_AND_ROLES_PROPERTY = "structurizr.admin";
    public static final String MAX_WORKSPACE_VERSIONS_PROPERTY = "structurizr.maxWorkspaceVersions";
    public static final String DSL_EDITOR_PROPERTY = "structurizr.dslEditor";
    public static final String INTERNET_CONNECTION_PROPERTY = "structurizr.internetConnection";

    public static final String SAML_REGISTRATION_ID = "structurizr.saml.registrationId";
    public static final String SAML_ENTITY_ID = "structurizr.saml.entityId";
    public static final String SAML_METADATA = "structurizr.saml.metadata";
    public static final String SAML_SIGNING_CERTIFICATE = "structurizr.saml.signing.certificate";
    public static final String SAML_SIGNING_PRIVATE_KEY = "structurizr.saml.signing.privateKey";
    public static final String SAML_ATTRIBUTE_USERNAME = "structurizr.saml.attribute.username";
    public static final String SAML_ATTRIBUTE_ROLE = "structurizr.saml.attribute.role";

    public static final String DEFAULT_SAML_ATTRIBUTE_USERNAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
    public static final String DEFAULT_SAML_ATTRIBUTE_ROLE = "http://schemas.xmlsoap.org/claims/Group";

    public static final String DEFAULT_AUTHENTICATION_VARIANT = "file";
    public static final String DEFAULT_SESSION_VARIANT = "local";
    public static final String DEFAULT_MAX_WORKSPACE_VERSIONS = "30";

    public static final String CACHE_VARIANT_NONE = "none";
    public static final String CACHE_VARIANT_LOCAL = "local";
    public static final String CACHE_VARIANT_REDIS = "redis";

    public static final String REDIS_HOST = "structurizr.redis.host";
    public static final String REDIS_PORT = "structurizr.redis.port";
    public static final String REDIS_PASSWORD = "structurizr.redis.password";

    public static final String WIKI_URL = "structurizr.wiki.url";
    public static final String WIKI_TOKEN = "structurizr.wiki.token";

}