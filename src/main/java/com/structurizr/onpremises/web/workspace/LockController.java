package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceLockResponse;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;

@Controller
@PreAuthorize("isAuthenticated()")
public class LockController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(LockController.class);

    @RequestMapping(value = "/workspace/{workspaceId}/lock", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public WorkspaceLockResponse lockWorkspace(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = true) String agent
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return new WorkspaceLockResponse(false, false);
        }

        User user = getUser();

        if (!workspaceMetaData.isLocked() || workspaceMetaData.isLockedBy(user.getUsername(), agent)) {
            try {
                if (workspaceComponent.lockWorkspace(workspaceId, user.getUsername(), agent)) {
                    return new WorkspaceLockResponse(true, true);
                }
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        }

        // not locked - refresh the metadata and try to figure out why
        workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData.isLocked() && !workspaceMetaData.isLockedBy(user.getUsername(), agent)) {
            // locked by somebody else
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT);

            return new WorkspaceLockResponse(false, true, "The workspace could not be locked; it was locked by " + workspaceMetaData.getLockedUser() + " using " + workspaceMetaData.getLockedAgent() + " at " + sdf.format(workspaceMetaData.getLockedDate()) + ".");
        } else {
            // other problem :-)
            return new WorkspaceLockResponse(false, workspaceMetaData.isLocked(), "The workspace could not be locked - please save your workspace and refresh the page.");
        }
    }

    @RequestMapping(value = "/workspace/{workspaceId}/unlock", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public WorkspaceLockResponse unlockWorkspace(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = true) String agent
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return new WorkspaceLockResponse(false, false);
        }

        User user = getUser();

        if (workspaceMetaData.isLockedBy(user.getUsername(), agent)) {
            workspaceComponent.unlockWorkspace(workspaceId);
            return new WorkspaceLockResponse(true, false);
        }

        return new WorkspaceLockResponse(false, workspaceMetaData.isLocked());
    }

    @RequestMapping(value = "/workspace/{workspaceId}/unlock", method = RequestMethod.GET)
    public String unlockWorkspace(@PathVariable("workspaceId") long workspaceId, ModelMap model) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        User user = getUser();
        if (workspaceMetaData.isOpen() || workspaceMetaData.isWriteUser(user)) {
            try {
                workspaceComponent.unlockWorkspace(workspaceId);
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        } else {
            return show404Page(model);
        }

        return "redirect:/workspace/" + workspaceId;
    }

}