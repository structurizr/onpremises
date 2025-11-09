package com.structurizr.onpremises.component.workspace;

import com.amazonaws.util.StringInputStream;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

/**
 * A workspace DAO implementation that uses the Azure Blob Storage.
 */
public class AzureBlobStorageWorkspaceDao extends AbstractWorkspaceDao {

    private static final Log log = LogFactory.getLog(AzureBlobStorageWorkspaceDao.class);

    private static final String WORKSPACES_VIRTUAL_DIRECTORY = "workspaces/";
    private static final String WORKSPACE_PROPERTIES_FILENAME = "workspace.properties";
    private static final String WORKSPACE_CONTENT_FILENAME = "workspace.json";
    private static final String PNG_FILE_EXTENSION = ".png";

    private final String accountName;
    private final String containerName;
    private final String accessKey;

    private final BlobContainerClient blobContainerClient;

    AzureBlobStorageWorkspaceDao() {
        this.accountName = Configuration.getInstance().getProperty(AZURE_BLOB_STORAGE_ACCOUNT_NAME);
        this.accessKey = Configuration.getInstance().getProperty(AZURE_BLOB_STORAGE_ACCESS_KEY);
        this.containerName = Configuration.getInstance().getProperty(AZURE_BLOB_STORAGE_CONTAINER_NAME);

        this.blobContainerClient = createBlobContainerClient();
    }

    private BlobContainerClient createBlobContainerClient() {
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accessKey);
        
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net/", accountName))
                .credential(credential)
                .buildClient();

        return blobServiceClient.getBlobContainerClient(containerName);
    }

    @Override
    public List<Long> getWorkspaceIds() {
        List<Long> workspaceIds = new ArrayList<>();

        try {
            ListBlobsOptions options = new ListBlobsOptions();
            options.setPrefix(WORKSPACES_VIRTUAL_DIRECTORY);
            PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs(options, null);
            for (BlobItem blobItem : blobItems) {
                String name = blobItem.getName();
                if (name.matches(WORKSPACES_VIRTUAL_DIRECTORY + "\\d*/" + WORKSPACE_PROPERTIES_FILENAME)) {
                    long id = Long.parseLong(name.split("/")[1]);

                    if (id > 0) {
                        workspaceIds.add(id);
                    }
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        Collections.sort(workspaceIds);
        return workspaceIds;
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
        String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/" + WORKSPACE_PROPERTIES_FILENAME;

        BlockBlobClient blobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();
        String content = blobClient.downloadContent().toString();

        try {
            Properties properties = new Properties();
            properties.load(new StringInputStream(content));

            return WorkspaceMetaData.fromProperties(workspaceId, properties);
        } catch (Throwable t) {
            log.error(t.getMessage());
            return null;
        }
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
        try {
            String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceMetaData.getId() + "/" + WORKSPACE_PROPERTIES_FILENAME;

            Properties properties = workspaceMetaData.toProperties();
            StringWriter stringWriter = new StringWriter();
            properties.store(stringWriter, "");

            String content = stringWriter.toString();

            BlockBlobClient blobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            try (ByteArrayInputStream dataStream = new ByteArrayInputStream(contentBytes)) {
                blobClient.upload(dataStream, contentBytes.length, true);
            } catch (IOException e) {
                throw new WorkspaceComponentException("Error storing workspace metadata", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorkspaceComponentException("Error storing workspace metadata", e);
        }
    }

    @Override
    public boolean deleteBranch(long workspaceId, String branch) {
        return false;
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        try {
            ListBlobsOptions options = new ListBlobsOptions();
            options.setPrefix(WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/");
            PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs(options, null);
            for (BlobItem blobItem : blobItems) {
                BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
                blobClient.delete();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getWorkspace(long workspaceId, String branch, String version) {
        String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/" + WORKSPACE_CONTENT_FILENAME;
        BlockBlobClient blobClient = blobContainerClient.getBlobVersionClient(blobName, version).getBlockBlobClient();
        return blobClient.downloadContent().toString();
    }

    @Override
    public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json, String branch) {
        String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceMetaData.getId() + "/" + WORKSPACE_CONTENT_FILENAME;

        BlockBlobClient blobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(jsonBytes)) {
            blobClient.upload(dataStream, jsonBytes.length, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new WorkspaceComponentException("Error storing workspace", e);
        }
    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions) {
        List<WorkspaceVersion> versions = new ArrayList<>();

        String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/" + WORKSPACE_CONTENT_FILENAME;
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(blobName)
                .setDetails(new BlobListDetails().setRetrieveVersions(true));

        for (BlobItem blobItem : blobContainerClient.listBlobs(options, null)) {
            versions.add(new WorkspaceVersion(blobItem.getVersionId(), new Date(blobItem.getProperties().getLastModified().toInstant().toEpochMilli())));
        }

        if (!versions.isEmpty()) {
            versions.get(0).clearVersionId();
        }

        return versions;
    }

    @Override
    public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
        return List.of();
    }

    @Override
    public boolean putImage(long workspaceId, String filename, File file) {
        try {
            String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/" + filename;
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.uploadFromFile(file.getAbsolutePath(), true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        List<Image> images = new ArrayList<>();

        ListBlobsOptions options = new ListBlobsOptions();
        options.setPrefix(WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/");
        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs(options, null);
        for (BlobItem blobItem : blobItems) {
            String name = blobItem.getName();
            if (name.endsWith(PNG_FILE_EXTENSION)) {
                images.add(new Image(name.substring(name.lastIndexOf("/")+1), blobItem.getProperties().getContentLength(), new Date(blobItem.getProperties().getLastModified().toInstant().toEpochMilli())));
            }
        }

        return images;
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String filename) {
        String blobName = WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/" + filename;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (blobClient.exists()) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                blobClient.downloadStream(outputStream);

                return new InputStreamAndContentLength(new ByteArrayInputStream(outputStream.toByteArray()), outputStream.size());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        try {
            ListBlobsOptions options = new ListBlobsOptions();
            options.setPrefix(WORKSPACES_VIRTUAL_DIRECTORY + workspaceId + "/");
            PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs(options, null);
            for (BlobItem blobItem : blobItems) {
                String name = blobItem.getName();
                if (name.endsWith(PNG_FILE_EXTENSION)) {
                    BlobClient blobClient = blobContainerClient.getBlobClient(name);
                    blobClient.delete();
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
