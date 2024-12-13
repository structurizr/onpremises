package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.Messages;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.Features;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

@Controller
@PreAuthorize("isAuthenticated()")
public class UsersController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(UsersController.class);
    private static final String VIEW = "users";
    private static final String NEWLINE = "\n";

    @RequestMapping(value = "/workspace/{workspaceId}/users", method = RequestMethod.GET)
    public String showUsers(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        boolean editable = Configuration.getInstance().isFeatureEnabled(Features.UI_WORKSPACE_USERS);

        model.addAttribute("readUsers", toNewlineSeparatedString(workspaceMetaData.getReadUsers()));
        model.addAttribute("writeUsers", toNewlineSeparatedString(workspaceMetaData.getWriteUsers()));

        return showAuthenticatedView(VIEW, workspaceMetaData, null, null, model, true, editable);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/users", method = RequestMethod.POST)
    public String updateUsers(
            @PathVariable("workspaceId") long workspaceId,
            String readUsers,
            String writeUsers,
            ModelMap model,
            RedirectAttributes redirectAttributes
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (!Configuration.getInstance().isFeatureEnabled(Features.UI_WORKSPACE_USERS)) {
            return showFeatureNotAvailablePage(model);
        }

        User signedInUser = getUser();

        if (workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(signedInUser)) {
            readUsers = HtmlUtils.filterHtml(readUsers);
            writeUsers = HtmlUtils.filterHtml(writeUsers);

            if (readUsers != null) {
                workspaceMetaData.clearReadUsers();
                String[] users = readUsers.split(NEWLINE);
                for (String user : users) {
                    workspaceMetaData.addReadUser(user);
                }
            }

            if (writeUsers != null) {
                workspaceMetaData.clearWriteUsers();
                String[] users = writeUsers.split(NEWLINE);
                for (String user : users) {
                    workspaceMetaData.addWriteUser(user);
                }
            }

            // a safety check, to ensure that the currently signed in user doesn't remove themselves!
            if (!workspaceMetaData.hasNoUsersConfigured() && !workspaceMetaData.isWriteUser(signedInUser)) {
                workspaceMetaData.addWriteUser(signedInUser.getUsername());
            }

            try {
                workspaceComponent.putWorkspaceMetaData(workspaceMetaData);
            } catch (WorkspaceComponentException e) {
                Messages messages = new Messages();
                redirectAttributes.addFlashAttribute("messages", messages);
                messages.addErrorMessage("There was a problem updating the users for this workspace - please see the logs for more information.");
                log.error(e);
            }

            return "redirect:/workspace/" + workspaceId + "/users";
        } else {
            return show404Page(model);
        }
    }

    private String toNewlineSeparatedString(Set<String> usernames) {
        ArrayList<String> list = new ArrayList<>(usernames);
        Collections.sort(list);

        StringBuilder buf = new StringBuilder();
        for (String username : list) {
            buf.append(username);
            buf.append(NEWLINE);
        }

        return buf.toString();
    }

}