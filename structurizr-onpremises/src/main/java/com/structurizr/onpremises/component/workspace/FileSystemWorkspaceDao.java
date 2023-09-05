package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.util.DateUtils;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.structurizr.onpremises.util.DateUtils.UTC_TIME_ZONE;

/**
 * A workspace DAO implementation that uses the local file system.
 */
class FileSystemWorkspaceDao extends AbstractWorkspaceDao {

    private static final Log log = LogFactory.getLog(FileSystemWorkspaceDao.class);

    static final String WORKSPACE_JSON_FILENAME = "workspace.json";
    static final String WORKSPACE_VERSION_JSON_FILENAME = "workspace-%s.json";
    static final String WORKSPACE_PROPERTIES_FILENAME = "workspace.properties";
    static final String VERSION_TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    static final String WORKSPACE_VERSION_JSON_FILENAME_REGEX = "workspace-\\d{17}\\.json";
    static final String IMAGES_DIRECTORY_NAME = "images";
    static final String PNG_FILENAME_REGEX = ".*\\.png";

    private final File dataDirectory;

    FileSystemWorkspaceDao(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    private File getPathToWorkspace(long workspaceId) {
        File path = new File(dataDirectory, "" + workspaceId);
        if (!path.exists()) {
            try {
                Path directory = Files.createDirectories(path.toPath());
                if (!directory.toFile().exists()) {
                    log.error(path.getCanonicalFile().getAbsolutePath() + " could not be created.");
                }
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    @Override
    public List<Long> getWorkspaceIds() {
        File[] files = dataDirectory.listFiles();
        List<Long> workspaceIds = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file != null && file.isDirectory() && file.getName().matches("\\d*")) {
                    long id = Long.parseLong(file.getName());
                    workspaceIds.add(id);
                }
            }
        }

        Collections.sort(workspaceIds);
        return workspaceIds;
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        directory.delete();
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
        WorkspaceMetaData workspace = new WorkspaceMetaData(workspaceId);

        File workspacePropertiesFile = new File(getPathToWorkspace(workspaceId), WORKSPACE_PROPERTIES_FILENAME);
        if (workspacePropertiesFile.exists()) {
            try {
                FileReader fileReader = new FileReader(workspacePropertiesFile);
                Properties properties = new Properties();
                properties.load(fileReader);
                fileReader.close();

                workspace = WorkspaceMetaData.fromProperties(workspaceId, properties);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e);
            }
        } else {
            return null;
        }

        return workspace;
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
        try {
            File path = getPathToWorkspace(workspaceMetaData.getId());
            File workspacePropertiesFile = new File(path, WORKSPACE_PROPERTIES_FILENAME);

            Properties properties = workspaceMetaData.toProperties();

            FileWriter fileWriter = new FileWriter(workspacePropertiesFile);
            properties.store(fileWriter, null);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        File workspaceDirectory = getPathToWorkspace(workspaceId);
        deleteDirectory(workspaceDirectory);

        return !workspaceDirectory.exists();
    }

    @Override
    public String getWorkspace(long workspaceId, String version) {
        try {
            File path = getPathToWorkspace(workspaceId);
            File file;

            if (!StringUtils.isNullOrEmpty(version)) {
                file = new File(path, String.format(WORKSPACE_VERSION_JSON_FILENAME, version));
            } else {
                file = new File(path, WORKSPACE_JSON_FILENAME);
            }

            if (file.exists()) {
                return Files.readString(file.toPath());
            } else {
                if (!StringUtils.isNullOrEmpty(version)) {
                    throw new WorkspaceComponentException("Could not get workspace " + workspaceId + " with version " + version);
                } else {
                    throw new WorkspaceComponentException("Could not get workspace " + workspaceId);
                }
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Could not get workspace " + workspaceId, ioe);
        }
    }

    @Override
    public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {
        try {
            // write the latest version to workspace.json
            File path = getPathToWorkspace(workspaceMetaData.getId());
            File file = new File(path, WORKSPACE_JSON_FILENAME);
            Files.writeString(file.toPath(), json);

            try {
                // and write a versioned workspace.json file too
                SimpleDateFormat sdf = new SimpleDateFormat(VERSION_TIMESTAMP_FORMAT);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Files.writeString(new File(path, "workspace-" + sdf.format(workspaceMetaData.getLastModifiedDate()) + ".json").toPath(), json);
            } catch (Exception e) {
                log.error(e);
            }
        } catch (Exception e) {
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, int maxVersions) {
        List<WorkspaceVersion> versions = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(VERSION_TIMESTAMP_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

        try {
            File workspaceDirectory = getPathToWorkspace(workspaceId);
            if (workspaceDirectory.exists()) {
                File[] files = workspaceDirectory.listFiles((dir, name) -> name.matches(WORKSPACE_VERSION_JSON_FILENAME_REGEX));

                if (files != null) {
                    Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));

                    for (int i = 0; i < Math.min(maxVersions, files.length); i++) {
                        File file = files[i];
                        String versionId = file.getName().substring(file.getName().indexOf('-') + 1, file.getName().indexOf('.'));
                        Date versionDate = sdf.parse(versionId);
                        versions.add(new WorkspaceVersion(versionId, versionDate));
                    }
                }

                if (versions.size() > 0) {
                    versions.get(0).clearVersionId();
                }
            }
        } catch (Exception ioe) {
            log.error(ioe);
        }

        return versions;
    }

    public void removeOldWorkspaceVersions(int maxWorkspaceVersions) {
        try {
            Collection<Long> workspaceIds = getWorkspaceIds();

            for (Long workspaceId : workspaceIds) {
                File workspaceDirectory = getPathToWorkspace(workspaceId);
                File[] files = workspaceDirectory.listFiles((dir, name) -> name.matches(WORKSPACE_VERSION_JSON_FILENAME_REGEX));

                if (files != null) {
                    Arrays.sort(files, (a,b) -> b.getName().compareTo(a.getName()));

                    if (files.length > maxWorkspaceVersions) {
                        for (int i = maxWorkspaceVersions; i < files.length; i++) {
                            File file = files[i];
                            file.delete();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.error(t);
        }
    }

    @Scheduled(cron="0 0 * * * ?")
    public void removeOldWorkspaceVersions() {
        removeOldWorkspaceVersions(Configuration.getInstance().getMaxWorkspaceVersions());
    }

    private File getPathToWorkspaceImages(long workspaceId) {
        File path = new File(getPathToWorkspace(workspaceId), IMAGES_DIRECTORY_NAME);
        if (!path.exists()) {
            try {
                Files.createDirectories(path.toPath());
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    @Override
    public boolean putImage(long workspaceId, String filename, File file) {
        try {
            File imagesDirectory = getPathToWorkspaceImages(workspaceId);
            File destination = new File(imagesDirectory, filename);
            Files.move(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        List<Image> images = new LinkedList<>();
        File imagesDirectory = getPathToWorkspaceImages(workspaceId);

        File[] files = imagesDirectory.listFiles((dir, name) -> name.matches(PNG_FILENAME_REGEX));

        if (files != null) {
            for (File file : files) {
                images.add(new Image(file.getName(), file.length(), new Date(file.lastModified())));
            }
        }

        return images;
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String filename) {
        try {
            File imagesDirectory = getPathToWorkspaceImages(workspaceId);
            File file = new File(imagesDirectory, filename);
            if (file.exists()) {
                return new InputStreamAndContentLength(new FileInputStream(file), file.length());
            }
        } catch (Exception e) {
            String message = "Could not get " + filename + " for workspace " + workspaceId;
            log.warn(e.getMessage() + " - " + message);
        }

        return null;
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        File imagesDirectory = getPathToWorkspaceImages(workspaceId);
        File[] files = imagesDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        return imagesDirectory.delete();
    }

}