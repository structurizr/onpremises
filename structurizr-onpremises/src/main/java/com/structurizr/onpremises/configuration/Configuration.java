package com.structurizr.onpremises.configuration;

import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.http.HttpClient;
import com.structurizr.onpremises.component.workspace.WorkspaceEventListener;
import com.structurizr.onpremises.util.Version;
import com.structurizr.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

public final class Configuration {

    public static final boolean PREVIEW_FEATURES = false;

    private static final String PLUGINS_DIRECTORY_NAME = "plugins";
    private static final String COMMA = ",";

    private final String versionSuffix;

    private String webUrl;
    private Set<String> adminUsersAndRoles = new HashSet<>();

    private boolean graphvizEnabled = false;

    private final Properties properties;
    private final com.structurizr.util.Features features = new com.structurizr.util.Features();

    private WorkspaceEventListener workspaceEventListener;

    private static Configuration INSTANCE;

    public static void init() {
        init(new Properties());
    }

    public static void init(Properties properties) {
        INSTANCE = new Configuration(properties);
    }

    public Properties getProperties() {
        return properties;
    }

    private Configuration(Properties props) {
        this.properties = props;

        new DefaultsConfigurer(properties).apply();
        setWebUrl(getProperty(URL));
        setAdminUsersAndRoles(getProperty(ADMIN_USERS_AND_ROLES).split(COMMA));

        // applicationContext.xml: <import resource="applicationContext-session-${structurizr.session}.xml" />
        System.setProperty(AUTHENTICATION_IMPLEMENTATION, getProperty(AUTHENTICATION_IMPLEMENTATION));
        // applicationContext-security.xml: <import resource="applicationContext-security-${structurizr.authentication}.xml" />
        System.setProperty(SESSION_IMPLEMENTATION, getProperty(SESSION_IMPLEMENTATION));

        // for backwards compatibility - don't configure any allowed URLs
        if (!Boolean.parseBoolean(getProperty(INTERNET_CONNECTION))) {
            properties.setProperty(URLS_ALLOWED, "");
        }

        configurePlugins();
        configureFeatures();

        if (getProperty(DATA_STORAGE_IMPLEMENTATION).equals(DATA_STORAGE_VARIANT_AMAZON_WEB_SERVICES_S3)) {
            new AmazonWebServicesS3Configurer(properties).apply();
        }

        if (getProperty(DATA_STORAGE_IMPLEMENTATION).equals(DATA_STORAGE_VARIANT_AZURE_BLOB_STORAGE)) {
            new AzureBlobStorageConfigurer(properties).apply();
        }

        if (getProperty(AUTHENTICATION_IMPLEMENTATION).equals(AUTHENTICATION_VARIANT_SAML)) {
            new SamlConfigurer(properties).apply();
        }

        if (getProperty(SEARCH_IMPLEMENTATION).equals(SEARCH_VARIANT_ELASTICSEARCH)) {
            new ElasticsearchConfigurer(properties).apply();
        }

        if (
            getProperty(SESSION_IMPLEMENTATION).equals(SESSION_VARIANT_REDIS) ||
            getProperty(CACHE_IMPLEMENTATION).equals(CACHE_VARIANT_REDIS)
        ) {
            new RedisConfigurer(properties).apply();
        }

        String buildNumber = new Version().getBuildNumber();
        if (StringUtils.isNullOrEmpty(buildNumber)) {
            versionSuffix = "";
        } else {
            versionSuffix = "-" + buildNumber;
        }
    }

    private void configurePlugins() {
        try {
            String workspaceEventListenerClassName = getProperty(WORKSPACE_EVENT_LISTENER_PLUGIN);
            if (!StringUtils.isNullOrEmpty(workspaceEventListenerClassName)) {
                Class clazz = loadClass(workspaceEventListenerClassName);
                workspaceEventListener = (WorkspaceEventListener)clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureFeatures() {
        features.configure(Features.UI_WORKSPACE_USERS, Boolean.parseBoolean(getProperty(Features.UI_WORKSPACE_USERS)));
        features.configure(Features.UI_WORKSPACE_SETTINGS, Boolean.parseBoolean(getProperty(Features.UI_WORKSPACE_SETTINGS)));
        features.configure(Features.UI_DSL_EDITOR, Boolean.parseBoolean(getProperty(Features.UI_DSL_EDITOR)));
        features.configure(Features.WORKSPACE_ARCHIVING, Boolean.parseBoolean(getProperty(Features.WORKSPACE_ARCHIVING)));
        features.configure(Features.WORKSPACE_BRANCHES, Boolean.parseBoolean(getProperty(Features.WORKSPACE_BRANCHES)));
        features.configure(Features.WORKSPACE_SCOPE_VALIDATION, getProperty(Features.WORKSPACE_SCOPE_VALIDATION).equalsIgnoreCase(Features.WORKSPACE_SCOPE_VALIDATION_STRICT));
        features.configure(Features.DIAGRAM_REVIEWS, Boolean.parseBoolean(getProperty(Features.DIAGRAM_REVIEWS)));
        features.configure(Features.DIAGRAM_ANONYMOUS_THUMBNAILS, Boolean.parseBoolean(getProperty(Features.DIAGRAM_ANONYMOUS_THUMBNAILS)));

        String search = getProperty(SEARCH_IMPLEMENTATION);
        features.configure(Features.WORKSPACE_SEARCH, search.equals(SEARCH_VARIANT_LUCENE) || search.equals(SEARCH_VARIANT_ELASTICSEARCH));

        // for backwards compatibility (older versions had structurizr.dslEditor=true)
        if (!isFeatureEnabled(Features.UI_DSL_EDITOR)) {
            features.configure(Features.UI_DSL_EDITOR, Boolean.parseBoolean(getProperty(DSL_EDITOR)));
        }

        properties.remove(DSL_EDITOR); // not needed after the feature has been configured
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getWebUrl() {
        return webUrl;
    }

    private void setWebUrl(String url) {
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
        return versionSuffix;
    }

    public Set<String> getAdminUsersAndRoles() {
        return new HashSet<>(adminUsersAndRoles);
    }

    private void setAdminUsersAndRoles(String... usersAndRoles) {
        adminUsersAndRoles = new HashSet<>();
        if (usersAndRoles != null) {
            for (String userOrRole : usersAndRoles) {
                if (!StringUtils.isNullOrEmpty(userOrRole)) {
                    adminUsersAndRoles.add(userOrRole.trim().toLowerCase());
                }
            }
        }
    }

    public String getApiUrl() {
        return webUrl + "/api";
    }

    public String getGraphvizUrl() {
        return webUrl + "/graphviz";
    }

    public String getThemesUrl() {
        return webUrl + "/static/themes/";
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

    public boolean isDslEditorEnabled() {
        return isFeatureEnabled(Features.UI_DSL_EDITOR);
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
    
    public File getDataDirectory() {
        return new File(getProperty(DATA_DIRECTORY));
    }

    public void setFeatureEnabled(String feature) {
        features.enable(feature);
    }

    public void setFeatureDisabled(String feature) {
        features.disable(feature);
    }

    public boolean isFeatureEnabled(String feature) {
        return features.isEnabled(feature);
    }

    private Class loadClass(String fqn) throws Exception {
        File pluginsDirectory = new File(getDataDirectory(), PLUGINS_DIRECTORY_NAME);
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

    public String getProperty(String structurizrPropertyName) {
        String value = null;

        if (properties.containsKey(structurizrPropertyName)) {
            value = properties.getProperty(structurizrPropertyName);
        }

        // translate ${...} into a value from the named environment variable
        // (this mirrors what Spring does via the property placeholders)
        if (value != null) {
            if (value.startsWith("${") && value.endsWith("}")) {
                String environmentVariableName = value.substring(2, value.length()-1);
                value = System.getenv(environmentVariableName);
            }
        }

        if (value != null) {
            value = value.trim();
        }

        return value;
    }

    public StructurizrDslParser createStructurizrDslParser() {
        StructurizrDslParser parser = new StructurizrDslParser();

        parser.getFeatures().configure(com.structurizr.dsl.Features.ENVIRONMENT, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.FILE_SYSTEM, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.PLUGINS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.SCRIPTS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.COMPONENT_FINDER, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DOCUMENTATION, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DECISIONS, false);

        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith("structurizr.feature.dsl.")) {
                parser.getFeatures().configure(name, Boolean.parseBoolean(getProperty(name)));
            }
        }

        configure(parser.getHttpClient());

        return parser;
    }

    public HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();
        configure(httpClient);

        return httpClient;
    }

    private void configure(HttpClient httpClient) {
        String urlsAllowed = getProperty(URLS_ALLOWED);
        if (!StringUtils.isNullOrEmpty(urlsAllowed)) {
            String[] regexes = urlsAllowed.split(",");
            for (String regex : regexes) {
                httpClient.allow(regex.trim());
            }
        }
    }

}