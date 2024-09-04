package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;

import java.io.File;
import java.util.Collection;
import java.util.List;

interface WorkspaceDao {

    List<Long> getWorkspaceIds();

    WorkspaceMetaData getWorkspaceMetaData(long workspaceId);

    void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData);

    long createWorkspace(User user);

    boolean deleteBranch(long workspaceId, String branch);

    boolean deleteWorkspace(long workspaceId);

    String getWorkspace(long workspaceId, String branch, String version);

    void putWorkspace(WorkspaceMetaData workspaceMetaData, String json, String branch);

    List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions);

    List<WorkspaceBranch> getWorkspaceBranches(long workspaceId);

    boolean putImage(long workspaceId, String filename, File file);

    List<Image> getImages(long workspaceId);

    InputStreamAndContentLength getImage(long workspaceId, String filename);

    boolean deleteImages(long workspaceId);

}