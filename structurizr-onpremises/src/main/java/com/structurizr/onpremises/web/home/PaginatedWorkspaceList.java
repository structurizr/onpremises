package com.structurizr.onpremises.web.home;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;

import java.util.List;

class PaginatedWorkspaceList {

    static final int DEFAULT_PAGE_SIZE = 10;

    private final List<WorkspaceMetaData> workspaces;
    private final int pageNumber;
    private final int pageSize;
    private int start;
    private int end;

    PaginatedWorkspaceList(List<WorkspaceMetaData> workspaces, int pageNumber, int pageSize) {
        this.workspaces = workspaces;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;

        pageNumber = Math.abs(pageNumber);
        pageNumber = Math.max(1, pageNumber);
        pageSize = Math.abs(pageSize);
        pageSize = Math.min(pageSize, workspaces.size());
        if (pageSize == 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        start = (pageNumber - 1) * pageSize;
        start = Math.min(start, workspaces.size() - 1);

        end = start + pageSize;
        end = Math.min(end, workspaces.size());
    }

    int getPageNumber() {
        return pageNumber;
    }

    int getPageSize() {
        return pageSize;
    }

    boolean hasPreviousPage() {
        return start > 0;
    }

    boolean hasNextPage() {
        return end < workspaces.size();
    }

    List<WorkspaceMetaData> getWorkspaces() {
        return workspaces.subList(start, end);
    }

}