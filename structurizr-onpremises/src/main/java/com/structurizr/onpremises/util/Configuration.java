package com.structurizr.onpremises.util;

import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceEventListener;
import com.structurizr.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Configuration extends ConfigLookup {

    private static final String PLUGINS_DIRECTORY_NAME = "plugins";

    private static final String WORKSPACE_SCOPE_STRICT = "strict";
    private static final String WORKSPACE_SCOPE_RELAXED = "relaxed";

    private File dataDirectory;
    private String webUrl;
    private Set<String> adminUsersAndRoles = new HashSet<>();

    private String encryptionPassphrase;
    private String apiKey;

    private boolean graphvizEnabled = false;
    private boolean internetConnection = true;

    private Properties properties = new Properties();
    private final Map<String,Boolean> features = new HashMap<>();

    private WorkspaceEventListener workspaceEventListener;

    private static Configuration INSTANCE;

    public static void init() {
        INSTANCE = new Configuration();
    }

    private Configuration() {
        setDataDirectory(new File(getDataDirectoryLocation()));
        setEncryptionPassphrase(getConfigurationParameter("structurizr.encryption", "STRUCTURIZR_ENCRYPTION", StructurizrProperties.ENCRYPTION_PASSPHRASE_PROPERTY, null));
        setWebUrl(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.URL_PROPERTY, ""));
        setInternetConnection(Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.INTERNET_CONNECTION_PROPERTY, "true")));
        setApiKey(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.API_KEY_PROPERTY, ""));

        String commaSeparatedUsersAndRoles = getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.ADMIN_USERS_AND_ROLES_PROPERTY, "");
        if (!StringUtils.isNullOrEmpty(commaSeparatedUsersAndRoles)) {
            setAdminUsersAndRoles(commaSeparatedUsersAndRoles.split(","));
        }

        try {
            String workspaceEventListenerClassName = getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.WORKSPACE_EVENT_LISTENER_PLUGIN_PROPERTY, "");
            if (!StringUtils.isNullOrEmpty(workspaceEventListenerClassName)) {
                Class clazz = loadClass(workspaceEventListenerClassName);
                workspaceEventListener = (WorkspaceEventListener)clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File propertiesFile = new File(dataDirectory, StructurizrProperties.FILENAME);
            if (propertiesFile.exists()) {
                properties.load(new FileReader(propertiesFile));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        features.put(Features.UI_WORKSPACE_USERS, Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(Features.UI_WORKSPACE_USERS, "true")));
        features.put(Features.UI_WORKSPACE_SETTINGS, Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(Features.UI_WORKSPACE_SETTINGS, "true")));
        features.put(Features.UI_DSL_EDITOR, Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(Features.UI_DSL_EDITOR, "false")));
        features.put(Features.WORKSPACE_ARCHIVING, Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(Features.WORKSPACE_ARCHIVING, "false")));
        features.put(Features.WORKSPACE_SCOPE_VALIDATION, getConfigurationParameterFromStructurizrPropertiesFile(Features.WORKSPACE_SCOPE_VALIDATION, "relaxed").equalsIgnoreCase("strict"));
        features.put(Features.DIAGRAM_REVIEWS, Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(Features.DIAGRAM_REVIEWS, "true")));

        // for backwards compatibility
        if (!isDslEditorEnabled()) {
            features.put(Features.UI_DSL_EDITOR, Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.DSL_EDITOR_PROPERTY, "false")));
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public String getEncryptionPassphrase() {
        return encryptionPassphrase;
    }

    void setEncryptionPassphrase(String encryptionPassphrase) {
        this.encryptionPassphrase = encryptionPassphrase;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String url) {
        if (url != null) {
            if (url.endsWith("/")) {
                this.webUrl = url.substring(0, url.length()-1);
            } else {
                this.webUrl = url;
            }
        }
    }

    public String getCdnUrl() {
        return webUrl + "/static";
    }

    public String getVersionSuffix() {
        return "";
    }

    public Set<String> getAdminUsersAndRoles() {
        return new HashSet<>(adminUsersAndRoles);
    }

    public void setAdminUsersAndRoles(String... usersAndRoles) {
        adminUsersAndRoles = new HashSet<>();
        if (usersAndRoles != null) {
            for (String userOrRole : usersAndRoles) {
                adminUsersAndRoles.add(userOrRole.trim().toLowerCase());
            }
        }
    }

    public String getApiUrl() {
        return webUrl + "/api";
    }

    public String getGraphvizUrl() {
        return webUrl + "/graphviz";
    }

    public boolean isCloud() {
        return false;
    }

    public String getType() {
        return "onpremises";
    }

    public String getProduct() {
        return "onpremises";
    }

    public boolean isGraphvizEnabled() {
        return graphvizEnabled;
    }

    public void setGraphvizEnabled(boolean graphvizEnabled) {
        this.graphvizEnabled = graphvizEnabled;
    }

    public boolean isSafeMode() {
        return false;
    }
    
    public boolean hasInternetConnection() {
        return internetConnection;
    }

    public void setInternetConnection(boolean internetConnection) {
        this.internetConnection = internetConnection;
    }

    public boolean isDslEditorEnabled() {
        return isFeatureEnabled(Features.UI_DSL_EDITOR);
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getAuthenticationVariant() {
        return getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION_PROPERTY, StructurizrProperties.DEFAULT_AUTHENTICATION_VARIANT);
    }

    public String getSessionVariant() {
        return getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.SESSION_IMPLEMENTATION_PROPERTY, StructurizrProperties.DEFAULT_SESSION_VARIANT);
    }

    public int getMaxWorkspaceVersions() {
        return Integer.parseInt(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.MAX_WORKSPACE_VERSIONS_PROPERTY, StructurizrProperties.DEFAULT_MAX_WORKSPACE_VERSIONS));
    }

    public String getDataStorageImplementationName() {
        String name = getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.DATA_STORAGE_IMPLEMENTATION_PROPERTY, "");

        if (WorkspaceComponent.AMAZON_WEB_SERVICES_S3.equalsIgnoreCase(name)) {
            return WorkspaceComponent.AMAZON_WEB_SERVICES_S3;
        } else {
            return WorkspaceComponent.FILE;
        }
    }

    public String getCacheImplementationName() {
        String name = getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.CACHE_IMPLEMENTATION_PROPERTY, StructurizrProperties.CACHE_VARIANT_NONE);

        if (StructurizrProperties.CACHE_VARIANT_LOCAL.equalsIgnoreCase(name)) {
            return StructurizrProperties.CACHE_VARIANT_LOCAL;
//        } else if (StructurizrProperties.CACHE_VARIANT_REDIS.equalsIgnoreCase(name)) {
//            return StructurizrProperties.CACHE_VARIANT_REDIS;
        } else {
            return StructurizrProperties.CACHE_VARIANT_NONE;
        }
    }

    public String getSearchImplementationName() {
        String name = getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.SEARCH_IMPLEMENTATION_PROPERTY, "");

        if (SearchComponent.ELASTICSEARCH.equalsIgnoreCase(name)) {
            return SearchComponent.ELASTICSEARCH;
        } else if (SearchComponent.NONE.equalsIgnoreCase(name)) {
            return SearchComponent.NONE;
        } else {
            return SearchComponent.LUCENE;
        }
    }

    public void setFeatureEnabled(String feature) {
        features.put(feature, true);
    }

    public void setFeatureDisabled(String feature) {
        features.put(feature, false);
    }

    public boolean isFeatureEnabled(String feature) {
        return features.getOrDefault(feature, true);
    }

    protected Class loadClass(String fqn) throws Exception {
        File pluginsDirectory = new File(dataDirectory, PLUGINS_DIRECTORY_NAME);
        URL[] urls = new URL[0];

        if (pluginsDirectory.exists()) {
            File[] jarFiles = pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                urls = new URL[jarFiles.length];
                for (int i = 0; i < jarFiles.length; i++) {
                    try {
                        urls[i] = jarFiles[i].toURI().toURL();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        URLClassLoader childClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
        return childClassLoader.loadClass(fqn);
    }

    public WorkspaceEventListener getWorkspaceEventListener() {
        return workspaceEventListener;
    }

    public void setWorkspaceEventListener(WorkspaceEventListener workspaceEventListener) {
        this.workspaceEventListener = workspaceEventListener;
    }

}