package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;

import java.io.File;
import java.util.Collection;
import java.util.List;

interface WorkspaceDao {

    Collection<WorkspaceMetaData> getWorkspaces();

    WorkspaceMetaData getWorkspaceMetaData(long workspaceId);

    void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData);

    long createWorkspace(User user);

    boolean deleteWorkspace(long workspaceId);

    String getWorkspace(long workspaceId, String version);

    void putWorkspace(WorkspaceMetaData workspaceMetaData, String json);

    List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, int maxVersions);

    boolean putImage(long workspaceId, String filename, File file);

    List<Image> getImages(long workspaceId);

    InputStreamAndContentLength getImage(long workspaceId, String filename);

    boolean deleteImages(long workspaceId);

}