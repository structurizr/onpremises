package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

abstract class AbstractWorkspaceDao implements WorkspaceDao {

    private static final Log log = LogFactory.getLog(WorkspaceDao.class);

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
                if (user != null) {
                    workspaceMetaData.setOwner(user.getUsername());
                }
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