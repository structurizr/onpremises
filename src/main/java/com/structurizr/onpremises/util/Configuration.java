package com.structurizr.onpremises.util;

import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceComponent;
import com.structurizr.util.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Configuration extends ConfigLookup {

    private File dataDirectory;
    private String webUrl;
    private Set<String> adminUsersAndRoles = new HashSet<>();

    private String encryptionPassphrase;

    private boolean graphvizEnabled = false;
    private boolean dslEditorEnabled = false;
    private boolean safeMode = true;

    private static Configuration INSTANCE;

    public static void init() {
        INSTANCE = new Configuration();
    }

    private Configuration() {
        setDataDirectory(new File(getDataDirectoryLocation()));
        setEncryptionPassphrase(getConfigurationParameter("structurizr.encryption", "STRUCTURIZR_ENCRYPTION", StructurizrProperties.ENCRYPTION_PASSPHRASE_PROPERTY, null));
        setWebUrl(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.URL_PROPERTY, ""));
        setDslEditorEnabled(Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.DSL_EDITOR_PROPERTY, "false")));
        setSafeMode(Boolean.parseBoolean(getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.SAFE_MODE_PROPERTY, "true")));

        String commaSeparatedUsersAndRoles = getConfigurationParameterFromStructurizrPropertiesFile(StructurizrProperties.ADMIN_USERS_AND_ROLES_PROPERTY, "");
        if (!StringUtils.isNullOrEmpty(commaSeparatedUsersAndRoles)) {
            setAdminUsersAndRoles(commaSeparatedUsersAndRoles.split(","));
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("dot", "--version");
            Process process = processBuilder.start();
            process.waitFor();
            setGraphvizEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getEncryptionPassphrase() {
        return encryptionPassphrase;
    }

    void setEncryptionPassphrase(String encryptionPassphrase) {
        this.encryptionPassphrase = encryptionPassphrase;
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

    void setGraphvizEnabled(boolean graphvizEnabled) {
        this.graphvizEnabled = graphvizEnabled;
    }

    public boolean isSafeMode() {
        return safeMode;
    }

    public void setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
    }

    public boolean isDslEditorEnabled() {
        return dslEditorEnabled;
    }

    public void setDslEditorEnabled(boolean dslEditorEnabled) {
        this.dslEditorEnabled = dslEditorEnabled;
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

}