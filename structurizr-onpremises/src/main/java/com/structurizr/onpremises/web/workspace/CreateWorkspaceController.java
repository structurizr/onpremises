package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.AbstractController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CreateWorkspaceController extends AbstractController {

    private static final Log log = LogFactory.getLog(CreateWorkspaceController.class);

    @RequestMapping(value = "/workspace/create", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String createWorkspace(ModelMap model) {
        try {
            Configuration configuration = Configuration.getInstance();
            User user = getUser();
            if (user == null) {
                return show404Page(model); // this should never happen, as this page requires authentication
            }

            if (configuration.getAdminUsersAndRoles().isEmpty() || user.isUserOrRole(configuration.getAdminUsersAndRoles())) {
                long workspaceId = workspaceComponent.createWorkspace(user);
                return "redirect:/workspace/" + workspaceId;
            } else {
                return show404Page(model);
            }
        } catch (WorkspaceComponentException e) {
            e.printStackTrace();
            log.error(e);
        }

        return "redirect:/dashboard";
    }

}