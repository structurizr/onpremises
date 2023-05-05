package com.structurizr.onpremises.component.workspace;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A workspace DAO implementation that uses the Amazon Web Services S3.
 */
public class AmazonWebServicesS3WorkspaceDao extends AbstractWorkspaceDao {

    private static final Log log = LogFactory.getLog(AmazonWebServicesS3WorkspaceDao.class);

    static final String ACCESS_KEY_ID_PROPERTY = "aws-s3.accessKeyId";
    static final String SECRET_ACCESS_KEY_PROPERTY = "aws-s3.secretAccessKey";
    static final String REGION_PROPERTY = "aws-s3.region";
    static final String BUCKET_NAME_PROPERTY = "aws-s3.bucketName";
    static final String ENDPOINT_PROPERTY = "aws-s3.endpoint";

    private static final String WORKSPACE_PROPERTIES_FILENAME = "workspace.properties";
    private static final String WORKSPACE_CONTENT_FILENAME = "workspace.json";

    private static final String TYPE_TAG = "type";
    private static final String IMAGE_TYPE = "image";
    private static final String JSON_TYPE = "json";

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String region;
    private final String bucketName;
    private final String endpoint;

    private final AmazonS3 amazonS3;

    AmazonWebServicesS3WorkspaceDao(String accessKeyId, String secretAccessKey, String region, String bucketName,
            String endpoint) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.region = region;
        this.bucketName = bucketName;
        this.endpoint = endpoint;

        this.amazonS3 = createAmazonS3Client();
    }

    private AmazonS3 createAmazonS3Client() {
        if (!StringUtils.isNullOrEmpty(accessKeyId) && !StringUtils.isNullOrEmpty(secretAccessKey)) {
            log.debug("Creating AWS client with credentials from structurizr.properties file");
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
            if (!StringUtils.isNullOrEmpty(endpoint)) {
                return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(endpoint, region)).build();
            }
            return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
        } else {
            log.debug("Creating AWS client with default credentials provider chain");
            if (!StringUtils.isNullOrEmpty(region)) {
                return AmazonS3ClientBuilder.standard().withRegion(region).build();
            } else {
                return AmazonS3ClientBuilder.standard().build();
            }
        }
    }

    @Override
    public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {
        String objectKey = getBaseObjectName(workspaceMetaData.getId()) + WORKSPACE_CONTENT_FILENAME;

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(bytes);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(bytes.length);

        PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, inputStream, objectMetadata);
        putRequest.setTagging(new ObjectTagging(Collections.singletonList(new Tag(TYPE_TAG, JSON_TYPE))));
        putRequest.setMetadata(objectMetadata);

        amazonS3.putObject(putRequest);
    }

    @Override
    public String getWorkspace(long workspaceId, String version) {
        InputStream inputStream = null;
        try {
            String objectKey = getBaseObjectName(workspaceId) + WORKSPACE_CONTENT_FILENAME;
            GetObjectRequest getRequest = new GetObjectRequest(bucketName, objectKey);

            if (version != null && version.trim().length() > 0) {
                getRequest.withVersionId(version);
            }

            inputStream = amazonS3.getObject(getRequest).getObjectContent();

            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        try {
            String folderKey = getBaseObjectName(workspaceId);
            for (S3ObjectSummary file : amazonS3.listObjects(bucketName, folderKey).getObjectSummaries()) {
                amazonS3.deleteObject(bucketName, file.getKey());
            }

            amazonS3.deleteObject(bucketName, folderKey);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean putImage(long workspaceId, String filename, File file) {
        try {
            String objectKey = getBaseObjectName(workspaceId) + filename;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, file);
            putRequest.setTagging(new ObjectTagging(Collections.singletonList(new Tag(TYPE_TAG, IMAGE_TYPE))));
            putRequest.setMetadata(objectMetadata);

            amazonS3.putObject(putRequest);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        List<Image> images = new ArrayList<>();

        try {
            String folderKey = getBaseObjectName(workspaceId);
            for (S3ObjectSummary file : amazonS3.listObjects(bucketName, folderKey).getObjectSummaries()) {
                String name = file.getKey().substring(folderKey.length());
                System.out.println(name);
                if (name.endsWith(".png")) {
                    images.add(new Image(name.substring(name.lastIndexOf("/")+1), file.getSize(), file.getLastModified()));
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return images;
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String filename) {
        try {
            String objectKey = getBaseObjectName(workspaceId) + filename;

            GetObjectRequest getRequest = new GetObjectRequest(bucketName, objectKey);
            S3Object s3Object = amazonS3.getObject(getRequest);

            return new InputStreamAndContentLength(
                    s3Object.getObjectContent(),
                    s3Object.getObjectMetadata().getContentLength());
        } catch (AmazonS3Exception as3e) {
            if (as3e.getStatusCode() == 404) {
                // ignore this - the image doesn't exist
            } else {
                log.info(as3e.getMessage());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return null;
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        AmazonS3 s3 = amazonS3;

        try {
            String folderKey = getBaseObjectName(workspaceId);
            for (S3ObjectSummary file : s3.listObjects(bucketName, folderKey).getObjectSummaries()) {
                if (file.getKey().endsWith(".png")) {
                    s3.deleteObject(bucketName, file.getKey());
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, int maxVersions) {
        List<WorkspaceVersion> versions = new ArrayList<>();

        String objectKey = getBaseObjectName(workspaceId) + WORKSPACE_CONTENT_FILENAME;

        ListVersionsRequest request = new ListVersionsRequest()
                .withBucketName(bucketName)
                .withPrefix(objectKey)
                .withMaxResults(maxVersions);

        VersionListing versionListing = amazonS3.listVersions(request);
        for (S3VersionSummary versionSummary : versionListing.getVersionSummaries()) {
            if (versionSummary.getVersionId() != null && !versionSummary.getVersionId().equals("null") && versionSummary.getVersionId().trim().length() > 0) {
                WorkspaceVersion version = new WorkspaceVersion(versionSummary.getVersionId(), versionSummary.getLastModified());
                versions.add(version);
            }
        }

        if (versions.size() > 0) {
            versions.get(0).clearVersionId();
        }

        return versions;
    }

    @Override
    protected List<Long> getWorkspaceIds() {
        List<Long> workspaceIds = new ArrayList<>();

        try {
            String folderKey = getBaseObjectName();
            for (S3ObjectSummary file : amazonS3.listObjects(bucketName, folderKey).getObjectSummaries()) {
                String name = file.getKey().substring(folderKey.length());
                if (name.matches("/\\d*/" + WORKSPACE_PROPERTIES_FILENAME)) {
                    long id = Long.parseLong(name.substring(1, name.lastIndexOf('/')));

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
        InputStream inputStream = null;
        try {
            String objectKey = getBaseObjectName(workspaceId) + WORKSPACE_PROPERTIES_FILENAME;
            GetObjectRequest getRequest = new GetObjectRequest(bucketName, objectKey);

            inputStream = amazonS3.getObject(getRequest).getObjectContent();

            Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            return WorkspaceMetaData.fromProperties(workspaceId, properties);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
        try {
            String objectKey = getBaseObjectName(workspaceMetaData.getId()) + WORKSPACE_PROPERTIES_FILENAME;

            Properties properties = workspaceMetaData.toProperties();
            StringWriter stringWriter = new StringWriter();
            properties.store(stringWriter, "");

            byte[] bytes = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(bytes);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(bytes.length);

            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, inputStream, objectMetadata);
            putRequest.setTagging(new ObjectTagging(Collections.singletonList(new Tag(TYPE_TAG, JSON_TYPE))));
            putRequest.setMetadata(objectMetadata);

            amazonS3.putObject(putRequest);
        } catch (IOException e) {
            throw new WorkspaceComponentException("Error storing workspace metadata", e);
        }
    }

    private String getBaseObjectName() {
        return "workspaces";
    }

    private String getBaseObjectName(long workspaceId) {
        return getBaseObjectName() + "/" + workspaceId + "/";
    }

}