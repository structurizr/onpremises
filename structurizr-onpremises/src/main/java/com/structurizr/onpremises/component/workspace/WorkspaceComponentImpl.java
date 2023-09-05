package com.structurizr.onpremises.component.workspace;

import com.structurizr.AbstractWorkspace;
import com.structurizr.Workspace;
import com.structurizr.configuration.Role;
import com.structurizr.configuration.Visibility;
import com.structurizr.configuration.WorkspaceConfiguration;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.encryption.EncryptionLocation;
import com.structurizr.encryption.EncryptionStrategy;
import com.structurizr.io.json.EncryptedJsonReader;
import com.structurizr.io.json.EncryptedJsonWriter;
import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.DateUtils;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkspaceComponentImpl implements WorkspaceComponent {

    private static final Log log = LogFactory.getLog(WorkspaceComponentImpl.class);
    private static final String ENCRYPTION_STRATEGY_STRING = "encryptionStrategy";
    private static final String CIPHERTEXT_STRING = "ciphertext";

    private final WorkspaceDao workspaceDao;
    private final String encryptionPassphrase;

    WorkspaceComponentImpl() {
        String dataStorageImplementationName = Configuration.getInstance().getDataStorageImplementationName();

        if (AMAZON_WEB_SERVICES_S3.equals(dataStorageImplementationName)) {
            String accessKeyId = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(AmazonWebServicesS3WorkspaceDao.ACCESS_KEY_ID_PROPERTY, "");
            String secretAccessKey = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(AmazonWebServicesS3WorkspaceDao.SECRET_ACCESS_KEY_PROPERTY, "");
            String region = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(AmazonWebServicesS3WorkspaceDao.REGION_PROPERTY, "");
            String bucketName = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(AmazonWebServicesS3WorkspaceDao.BUCKET_NAME_PROPERTY, "");
            String endpoint = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(AmazonWebServicesS3WorkspaceDao.ENDPOINT_PROPERTY, "");
            boolean pathAccessEnabled = Boolean.parseBoolean(Configuration.getConfigurationParameterFromStructurizrPropertiesFile(AmazonWebServicesS3WorkspaceDao.PATH_STYLE_ACCESS_PROPERTY, "false"));

            this.workspaceDao = new AmazonWebServicesS3WorkspaceDao(accessKeyId, secretAccessKey, region, bucketName, endpoint,pathAccessEnabled);
        } else {
            this.workspaceDao = new FileSystemWorkspaceDao(Configuration.getInstance().getDataDirectory());
        }

        encryptionPassphrase = Configuration.getInstance().getEncryptionPassphrase();
    }

    WorkspaceComponentImpl(WorkspaceDao workspaceDao, String encryptionPassphrase) {
        this.workspaceDao = workspaceDao;
        this.encryptionPassphrase = encryptionPassphrase;
    }


    @Override
    public List<Long> getWorkspaceIds() throws WorkspaceComponentException {
        return workspaceDao.getWorkspaceIds();
    }

    @Override
    public Collection<WorkspaceMetaData> getWorkspaces() throws WorkspaceComponentException {
        List<WorkspaceMetaData> workspaces = new ArrayList<>();
        Collection<Long> workspaceIds = workspaceDao.getWorkspaceIds();

        for (Long workspaceId : workspaceIds) {
            WorkspaceMetaData workspace = getWorkspaceMetaData(workspaceId);
            if (workspace != null) {
                workspaces.add(workspace);
            }
        }

        workspaces.sort(Comparator.comparing(wmd -> wmd.getName().toLowerCase()));

        return workspaces;
    }

    @Override
    public Collection<WorkspaceMetaData> getWorkspaces(User user) throws WorkspaceComponentException {
        Collection<WorkspaceMetaData> workspaces = new ArrayList<>();

        try {
            workspaces = getWorkspaces();
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        List<WorkspaceMetaData> filteredWorkspaces = new ArrayList<>();

        if (user == null) {
            // unauthenticated request
            for (WorkspaceMetaData workspace : workspaces) {
                if (workspace.isPublicWorkspace() || workspace.hasNoUsersConfigured()) {
                    // so anybody can see it
                    workspace.setUrlPrefix("/share");
                    filteredWorkspaces.add(workspace);
                }
            }
        } else {
            // authenticated request
            for (WorkspaceMetaData workspace : workspaces) {
                if (workspace.isWriteUser(user)) {
                    // the user has read-write access to the workspace
                    workspace.setUrlPrefix("/workspace");
                    filteredWorkspaces.add(workspace);
                } else if (workspace.isReadUser(user)) {
                    // the user has read-only access to the workspace
                    workspace.setUrlPrefix("/workspace");
                    filteredWorkspaces.add(workspace);
                } else if (workspace.hasNoUsersConfigured()) {
                    // the workspace has no users configured, so anybody can see it
                    workspace.setUrlPrefix("/workspace");
                    filteredWorkspaces.add(workspace);
                } else if (workspace.isPublicWorkspace()) {
                    // so anybody can see it
                    workspace.setUrlPrefix("/share");
                    filteredWorkspaces.add(workspace);
                }
            }
        }

        return filteredWorkspaces;
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) throws WorkspaceComponentException {
        return workspaceDao.getWorkspaceMetaData(workspaceId);
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) throws WorkspaceComponentException {
        workspaceDao.putWorkspaceMetaData(workspaceMetaData);
    }

    @Override
    public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
        String json = workspaceDao.getWorkspace(workspaceId, version);

        if (json.contains(ENCRYPTION_STRATEGY_STRING) && json.contains(CIPHERTEXT_STRING)) {
            EncryptedJsonReader encryptedJsonReader = new EncryptedJsonReader();
            StringReader stringReader = new StringReader(json);
            try {
                EncryptedWorkspace encryptedWorkspace = encryptedJsonReader.read(stringReader);

                if (encryptedWorkspace.getEncryptionStrategy().getLocation() == EncryptionLocation.Server) {
                    if (StringUtils.isNullOrEmpty(encryptionPassphrase)) {
                        log.warn("Workspace " + workspaceId + " seems to be encrypted, but a passphrase has not been set");
                    }

                    encryptedWorkspace.getEncryptionStrategy().setPassphrase(encryptionPassphrase);
                    json = encryptedWorkspace.getPlaintext();
                } else if (encryptedWorkspace.getEncryptionStrategy().getLocation() == EncryptionLocation.Client) {
                    // do nothing, we'll pass back the JSON as-is
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new WorkspaceComponentException("Could not get workspace " + workspaceId, e);
            }
        } else {
            // again, do nothing, the JSON was stored unencrypted
        }

        return json;
    }

    @Override
    public long createWorkspace(User user) throws WorkspaceComponentException {
        try {
            long workspaceId = workspaceDao.createWorkspace(user);

            Workspace workspace = new Workspace("Workspace " + workspaceId, "Description");
            String json = WorkspaceUtils.toJson(workspace, false);

            putWorkspace(workspaceId, json);

            return workspaceId;
        } catch (Exception e) {
            throw new WorkspaceComponentException("Could not create workspace", e);
        }
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
        return workspaceDao.deleteWorkspace(workspaceId);
    }

    @Override
    public void putWorkspace(long workspaceId, String json) throws WorkspaceComponentException {
        try {
            Long currentRevision;
            AbstractWorkspace workspaceToBeStored;
            String jsonToBeStored;
            WorkspaceConfiguration configuration;

            WorkspaceMetaData workspaceMetaData = getWorkspaceMetaData(workspaceId);
            if (workspaceMetaData == null) {
                workspaceMetaData = new WorkspaceMetaData(workspaceId);
            }

            if (Configuration.getInstance().getWorkspaceEventListener() != null) {
                WorkspaceEvent event = createWorkspaceEvent(workspaceMetaData, json);
                Configuration.getInstance().getWorkspaceEventListener().beforeSave(event);
                json = event.getJson();
            }

            if (json.contains(ENCRYPTION_STRATEGY_STRING) && json.contains(CIPHERTEXT_STRING)) {
                EncryptedJsonReader jsonReader = new EncryptedJsonReader();
                StringReader stringReader = new StringReader(json);
                EncryptedWorkspace encryptedWorkspace = jsonReader.read(stringReader);

                encryptedWorkspace.setId(workspaceId);
                encryptedWorkspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));

                if (encryptedWorkspace.getRevision() == null) {
                    currentRevision = workspaceMetaData.getRevision();
                    encryptedWorkspace.setRevision(currentRevision + 1);
                } else {
                    currentRevision = encryptedWorkspace.getRevision();
                    encryptedWorkspace.setRevision(currentRevision + 1);
                }

                // also remove the workspace configuration
                configuration = encryptedWorkspace.getConfiguration();
                encryptedWorkspace.clearConfiguration();

                // copy the last modified details from the workspace
                workspaceMetaData.setLastModifiedDate(encryptedWorkspace.getLastModifiedDate());
                workspaceMetaData.setLastModifiedAgent(encryptedWorkspace.getLastModifiedAgent());
                workspaceMetaData.setLastModifiedUser(encryptedWorkspace.getLastModifiedUser());

                // write it back as an encrypted workspace JSON
                EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
                StringWriter stringWriter = new StringWriter();
                encryptedJsonWriter.write(encryptedWorkspace, stringWriter);

                workspaceMetaData.setClientSideEncrypted(true);
                workspaceToBeStored = encryptedWorkspace;
                jsonToBeStored = stringWriter.toString();
            } else {
                Workspace workspace = WorkspaceUtils.fromJson(json);
                workspace.setId(workspaceId);
                workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));

                if (workspace.getRevision() == null) {
                    currentRevision = workspaceMetaData.getRevision();
                    workspace.setRevision(currentRevision + 1);
                } else {
                    currentRevision = workspace.getRevision();
                    workspace.setRevision(currentRevision + 1);
                }

                // also remove the configuration
                configuration = workspace.getConfiguration();
                workspace.clearConfiguration();

                // copy the last modified details from the workspace
                workspaceMetaData.setLastModifiedDate(workspace.getLastModifiedDate());
                workspaceMetaData.setLastModifiedAgent(workspace.getLastModifiedAgent());
                workspaceMetaData.setLastModifiedUser(workspace.getLastModifiedUser());

                workspaceMetaData.setClientSideEncrypted(false);
                workspaceToBeStored = workspace;

                if (!StringUtils.isNullOrEmpty(encryptionPassphrase)) {
                    EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy(128, 1000, encryptionPassphrase);
                    encryptionStrategy.setLocation(EncryptionLocation.Server);

                    EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, json, encryptionStrategy);
                    encryptedWorkspace.setLastModifiedDate(workspace.getLastModifiedDate());

                    EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
                    StringWriter stringWriter = new StringWriter();
                    encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
                    jsonToBeStored = stringWriter.toString();
                } else {
                    jsonToBeStored = WorkspaceUtils.toJson(workspace, false);
                }
            }

            // check the revision
            if (workspaceMetaData.getRevision() > currentRevision) {
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT);
                throw new WorkspaceComponentException("The workspace could not be saved because a newer version has been created by " + workspaceMetaData.getLastModifiedUser() + " at " + sdf.format(workspaceMetaData.getLastModifiedDate()) + ".");
            }

            workspaceMetaData.setSize(jsonToBeStored.length());

            // check the workspace lock
            if (workspaceMetaData.isLocked() && !workspaceMetaData.isLockedBy(workspaceToBeStored.getLastModifiedUser(), workspaceToBeStored.getLastModifiedAgent())) {
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT);
                throw new WorkspaceComponentException("The workspace could not be saved because the workspace was locked by " + workspaceMetaData.getLockedUser() + " at " + sdf.format(workspaceMetaData.getLockedDate()) + ".");
            }

            // use the DAO to write the workspace
            workspaceDao.putWorkspace(workspaceMetaData, jsonToBeStored);

            try {
                workspaceMetaData.setName(workspaceToBeStored.getName());
                workspaceMetaData.setDescription(workspaceToBeStored.getDescription());
                workspaceMetaData.setRevision(workspaceMetaData.getRevision() + 1);

                // configure users
                if (configuration != null) {
                    if (configuration.getVisibility() != null) {
                        workspaceMetaData.setPublicWorkspace(configuration.getVisibility() == Visibility.Public);
                    }

                    if (!configuration.getUsers().isEmpty()) {
                        workspaceMetaData.clearWriteUsers();
                        workspaceMetaData.clearReadUsers();

                        for (com.structurizr.configuration.User user : configuration.getUsers()) {
                            if (user.getRole() == Role.ReadWrite) {
                                workspaceMetaData.addWriteUser(user.getUsername());
                            } else {
                                workspaceMetaData.addReadUser(user.getUsername());
                            }
                        }
                    }
                }

                putWorkspaceMetaData(workspaceMetaData);
            } catch (Exception e) {
                log.error(e);
            }
        } catch (WorkspaceComponentException wce) {
            throw wce;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

    private WorkspaceEvent createWorkspaceEvent(WorkspaceMetaData workspaceMetaData, String workspaceAsJson) {
        return new WorkspaceEvent() {

            private String json = workspaceAsJson;

            @Override
            public WorkspaceProperties getWorkspaceProperties() {
                return workspaceMetaData.toWorkspaceProperties();
            }

            @Override
            public String getJson() {
                return json;
            }

            @Override
            public void setJson(String json) {
                this.json = json;
            }
        };
//
//            public WorkspaceProperties(WorkspaceMetaData workspaceMetaData) {
//            this.id = workspaceMetaData.getId();
//            this.name = workspaceMetaData.getName();
//            this.description = workspaceMetaData.getDescription();
//
//            this.users = new LinkedHashSet<>();
//            for (String user : workspaceMetaData.getReadUsers()) {
//                users.add(new com.structurizr.configuration.User(user, Role.ReadOnly));
//            }
//            for (String user : workspaceMetaData.getWriteUsers()) {
//                users.add(new com.structurizr.configuration.User(user, Role.ReadWrite));
//            }
//
//            if (workspaceMetaData.isPublicWorkspace()) {
//                this.visibility = Visibility.Public;
//            } else {
//                this.visibility = Visibility.Private;
//            }
//
//            this.lastModifiedDate = workspaceMetaData.getLastModifiedDate();
//        }
//
//        this.workspaceId = workspaceMetaData.getId();
//        this.workspaceProperties = new WorkspaceProperties(workspaceMetaData);
//        this.json = json;

    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, int maxVersions) throws WorkspaceComponentException {
        return workspaceDao.getWorkspaceVersions(workspaceId, maxVersions);
    }

    @Override
    public boolean lockWorkspace(long workspaceId, String username, String agent) throws WorkspaceComponentException {
        WorkspaceMetaData workspaceMetaData = getWorkspaceMetaData(workspaceId);
        if (!workspaceMetaData.isLocked() || workspaceMetaData.isLockedBy(username, agent)) {
            workspaceMetaData.addLock(username, agent);

            try {
                putWorkspaceMetaData(workspaceMetaData);
                return true;
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        }

        return false;
    }

    @Override
    public boolean unlockWorkspace(long workspaceId) throws WorkspaceComponentException {
        WorkspaceMetaData workspaceMetaData = getWorkspaceMetaData(workspaceId);
        workspaceMetaData.clearLock();

        try {
            putWorkspaceMetaData(workspaceMetaData);
            return true;
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return false;
    }

    @Override
    public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
        WorkspaceMetaData workspace = getWorkspaceMetaData(workspaceId);
        workspace.setPublicWorkspace(true);
        putWorkspaceMetaData(workspace);
    }

    @Override
    public void makeWorkspacePrivate(long workspaceId) throws WorkspaceComponentException {
        WorkspaceMetaData workspace = getWorkspaceMetaData(workspaceId);
        workspace.setPublicWorkspace(false);
        putWorkspaceMetaData(workspace);
    }

    @Override
    public void shareWorkspace(long workspaceId) throws WorkspaceComponentException {
        WorkspaceMetaData workspace = getWorkspaceMetaData(workspaceId);
        workspace.setSharingToken(UUID.randomUUID().toString());
        putWorkspaceMetaData(workspace);
    }

    @Override
    public void unshareWorkspace(long workspaceId) throws WorkspaceComponentException {
        WorkspaceMetaData workspace = getWorkspaceMetaData(workspaceId);
        workspace.setSharingToken("");
        putWorkspaceMetaData(workspace);
    }

    @Override
    public boolean putImage(long workspaceId, String filename, File file) throws WorkspaceComponentException {
        return workspaceDao.putImage(workspaceId, filename, file);
    }

    @Override
    public List<Image> getImages(long workspaceId) throws WorkspaceComponentException {
        return workspaceDao.getImages(workspaceId);
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
        return workspaceDao.getImage(workspaceId, diagramKey);
    }

    @Override
    public boolean deleteImages(long workspaceId) throws WorkspaceComponentException {
        return workspaceDao.deleteImages(workspaceId);
    }

}