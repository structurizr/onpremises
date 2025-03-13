package com.structurizr.onpremises.configuration;

import java.util.Properties;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

class DefaultsConfigurer extends Configurer {

    DefaultsConfigurer(Properties properties) {
        super(properties);
    }

    void apply() {
        setDefault(DATA_DIRECTORY, StructurizrDataDirectory.DEFAULT_DATA_DIRECTORY_PATH);
        setDefault(AUTHENTICATION_IMPLEMENTATION, AUTHENTICATION_VARIANT_FILE);
        setDefault(SESSION_IMPLEMENTATION, SESSION_VARIANT_LOCAL);
        setDefault(URL, "");
        setDefault(INTERNET_CONNECTION, "true");
        setDefault(API_KEY, "");
        setDefault(DATA_STORAGE_IMPLEMENTATION, DATA_STORAGE_VARIANT_FILE);
        setDefault(MAX_WORKSPACE_VERSIONS, DEFAULT_MAX_WORKSPACE_VERSIONS);
        setDefault(WORKSPACE_THREADS, DEFAULT_NUMBER_OF_THREADS);
        setDefault(SEARCH_IMPLEMENTATION, SEARCH_VARIANT_LUCENE);
        setDefault(CACHE_IMPLEMENTATION, CACHE_VARIANT_NONE);
        setDefault(CACHE_EXPIRY_IN_MINUTES, DEFAULT_CACHE_EXPIRY_IN_MINUTES);
        setDefault(ADMIN_USERS_AND_ROLES, "");
        setDefault(WORKSPACE_EVENT_LISTENER_PLUGIN, "");
        setDefault(DSL_EDITOR, "false"); // backwards compatibility

        setDefault(Features.UI_WORKSPACE_USERS, "true");
        setDefault(Features.UI_WORKSPACE_SETTINGS, "true");
        setDefault(Features.UI_DSL_EDITOR, "false");
        setDefault(Features.WORKSPACE_ARCHIVING, "false");
        setDefault(Features.WORKSPACE_BRANCHES, "false");
        setDefault(Features.WORKSPACE_SCOPE_VALIDATION, Features.WORKSPACE_SCOPE_VALIDATION_RELAXED);
        setDefault(Features.DIAGRAM_REVIEWS, "true");
        setDefault(Features.DIAGRAM_ANONYMOUS_THUMBNAILS, "false");
    }

}