package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Responsible for managing workspace data.
 */
public interface WorkspaceComponent {

    String FILE = "file";
    String AMAZON_WEB_SERVICES_S3 = "aws-s3";

    List<Long> getWorkspaceIds() throws WorkspaceComponentException;

    Collection<WorkspaceMetaData> getWorkspaces() throws WorkspaceComponentException;

    Collection<WorkspaceMetaData> getWorkspaces(User user) throws WorkspaceComponentException;

    WorkspaceMetaData getWorkspaceMetaData(long workspaceId) throws WorkspaceComponentException;

    void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) throws WorkspaceComponentException;

    String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException;

    long createWorkspace(User user) throws WorkspaceComponentException;

    boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException;

    void putWorkspace(long workspaceId, String json) throws WorkspaceComponentException;

    List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, int maxVersions) throws WorkspaceComponentException;

    boolean lockWorkspace(long workspaceId, String username, String agent) throws WorkspaceComponentException;

    boolean unlockWorkspace(long workspaceId) throws WorkspaceComponentException;

    boolean putImage(long workspaceId, String filename, File file) throws WorkspaceComponentException;

    List<Image> getImages(long workspaceId) throws WorkspaceComponentException;

    InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException;

    boolean deleteImages(long workspaceId) throws WorkspaceComponentException;

    void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException;

    void makeWorkspacePrivate(long workspaceId) throws WorkspaceComponentException;

    void shareWorkspace(long workspaceId) throws WorkspaceComponentException;

    void unshareWorkspace(long workspaceId) throws WorkspaceComponentException;

}