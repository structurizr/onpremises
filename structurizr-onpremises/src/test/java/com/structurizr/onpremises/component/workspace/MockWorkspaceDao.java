package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class MockWorkspaceDao implements WorkspaceDao {

    @Override
    public Collection<WorkspaceMetaData> getWorkspaces() {
        return null;
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
        return null;
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {

    }

    @Override
    public long createWorkspace(User user) {
        return 0;
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        return false;
    }

    @Override
    public String getWorkspace(long workspaceId, String version) {
        return null;
    }

    @Override
    public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {

    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, int maxVersions) {
        return null;
    }

    @Override
    public boolean putImage(long workspaceId, String filename, File file) {
        return false;
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        return null;
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String filename) {
        return null;
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        return false;
    }

}