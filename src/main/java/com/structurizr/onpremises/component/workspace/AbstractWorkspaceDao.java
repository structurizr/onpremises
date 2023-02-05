package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public abstract class AbstractWorkspaceDao implements WorkspaceDao {

    private static final Log log = LogFactory.getLog(WorkspaceDao.class);

    protected abstract List<Long> getWorkspaceIds();

    @Override
    public final Collection<WorkspaceMetaData> getWorkspaces() {
        List<WorkspaceMetaData> workspaces = new ArrayList<>();
        Collection<Long> workspaceIds = getWorkspaceIds();

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
    public final long createWorkspace(User user) throws WorkspaceComponentException {
        try {
            long workspaceId;

            List<Long> workspaceIds = getWorkspaceIds();
            if (workspaceIds.size() == 0) {
                workspaceId = 1;
            } else {
                Collections.sort(workspaceIds);
                workspaceId = workspaceIds.get(workspaceIds.size()-1) + 1;
            }

            try {
                // create and write the workspace metadata
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspaceId);
                workspaceMetaData.setOwner(user.getUsername());
                workspaceMetaData.setApiKey(UUID.randomUUID().toString());
                workspaceMetaData.setApiSecret(UUID.randomUUID().toString());

                putWorkspaceMetaData(workspaceMetaData);
            } catch (Exception e) {
                log.error(e);
            }

            return workspaceId;
        } catch (Exception e) {
            throw new WorkspaceComponentException("Could not create workspace", e);
        }
    }

}