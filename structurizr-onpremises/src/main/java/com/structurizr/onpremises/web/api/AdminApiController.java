package com.structurizr.onpremises.web.api;

import com.structurizr.onpremises.component.workspace.WorkspaceComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * An implementation of the Structurizr admin API.
 */
@RestController
//@Property(name = "Documentation", value = "https://docs.structurizr.com/onpremises/workspace-api")
public class AdminApiController extends AbstractController {

    private static final Log log = LogFactory.getLog(WorkspaceApiController.class);

    private WorkspaceComponent workspaceComponent;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AdminApiController(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Autowired
    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @RequestMapping(value = "/api/workspace", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody WorkspacesApiResponse getWorkspaces(
            @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey
    ) {

        authenticateRequest(apiKey);

        try {
            Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
            WorkspacesApiResponse workspacesApiResponse = new WorkspacesApiResponse();

            for (WorkspaceMetaData wmd : workspaces) {
                workspacesApiResponse.add(toWorkspaceApiResponse(wmd));
            }

            return workspacesApiResponse;
        } catch (Exception e) {
            log.error(e);
            throw new ApiException("Could not get workspaces");
        }
    }

    @RequestMapping(value = "/api/workspace", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public @ResponseBody WorkspaceApiResponse createWorkspace(
            @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey
    ) {

        authenticateRequest(apiKey);

        try {
            long workspaceId = workspaceComponent.createWorkspace(null);
            if (workspaceId > 0) {
                WorkspaceMetaData wmd = workspaceComponent.getWorkspaceMetaData(workspaceId);
                return toWorkspaceApiResponse(wmd);
            } else {
                throw new ApiException("Could not create workspace");
            }
        } catch (Exception e) {
            log.error(e);
            throw new ApiException("Could not create workspace: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse deleteWorkspace(
            @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey,
            @PathVariable("workspaceId") long workspaceId
    ) {

        authenticateRequest(apiKey);

        try {
            if (workspaceId > 0) {
                if (workspaceComponent.deleteWorkspace(workspaceId)) {
                    return new ApiResponse(true, "Workspace " + workspaceId + " deleted");
                } else {
                    throw new ApiException("Could not delete workspace");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw e;
            } else {
                log.error(e);
                throw new ApiException("Could not delete workspace: " + e.getMessage());
            }
        }
    }

    private void authenticateRequest(String apiKey) {
        if (StringUtils.isNullOrEmpty(apiKey)) {
            throw new HttpUnauthorizedException("Authorization header must be provided");
        }

        if (StringUtils.isNullOrEmpty(Configuration.getInstance().getProperty(StructurizrProperties.API_KEY))) {
            throw new ApiException("The API key is not configured for this installation - please refer to the documentation");
        }

        if (!bCryptPasswordEncoder.matches(apiKey, Configuration.getInstance().getProperty(StructurizrProperties.API_KEY))) {
            throw new HttpUnauthorizedException("Incorrect API key");
        }
    }

    private WorkspaceApiResponse toWorkspaceApiResponse(WorkspaceMetaData wmd) {
        WorkspaceApiResponse war = new WorkspaceApiResponse();
        war.setId(wmd.getId());
        war.setName(wmd.getName());
        war.setDescription(wmd.getDescription());
        war.setApiKey(wmd.getApiKey());
        war.setApiSecret(wmd.getApiSecret());
        war.setPrivateUrl(Configuration.getInstance().getWebUrl() + "/workspace/" + wmd.getId());
        if (wmd.isOpen()) {
            war.setPublicUrl(Configuration.getInstance().getWebUrl() + "/share/" + wmd.getId());
        }
        if (wmd.isShareable()) {
            war.setShareableUrl(Configuration.getInstance().getWebUrl() + "/share/" + wmd.getId() + "/" + wmd.getSharingToken());
        }

        return war;
    }

    @ExceptionHandler(HttpUnauthorizedException.class)
    @ResponseBody
    public ApiResponse handleCustomException(HttpUnauthorizedException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiResponse handleCustomException(ApiException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ApiResponse error(Throwable t, HttpServletResponse response) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        t.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ApiResponse(false, t.getMessage());
    }

}