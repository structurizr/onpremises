package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.util.JsonUtils;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;

/**
 * Base class for all controllers underneath /share and /workspace (i.e. the workspace related controllers).
 */
public abstract class AbstractWorkspaceController extends AbstractController {

    private static final Log log = LogFactory.getLog(AbstractWorkspaceController.class);

    protected static final String URL_PREFIX = "urlPrefix";
    protected static final String URL_SUFFIX = "urlSuffix";

    protected final String showPublicView(String view, long workspaceId, ModelMap model, boolean showHeaderAndFooter) {
        WorkspaceMetaData workspaceMetaData = null;
        try {
            workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        if (workspaceMetaData != null) {
            if (workspaceMetaData.isOpen()) {
                String urlPrefix = "/share/" + workspaceId;
                model.addAttribute(URL_PREFIX, urlPrefix);
                model.addAttribute("thumbnailUrl", urlPrefix + "/images/");

                return showView(view, workspaceMetaData, null, null, model, false, showHeaderAndFooter);
            }
        }

        return show404Page(model);
    }

    protected final String showSharedView(String view, long workspaceId, String token, ModelMap model, boolean showHeaderAndFooter) {
        token = HtmlUtils.filterHtml(token);

        WorkspaceMetaData workspaceMetaData = null;
        try {
            workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        if (workspaceMetaData != null) {
            if (!StringUtils.isNullOrEmpty(token) && token.equals(workspaceMetaData.getSharingToken())) {
                String urlPrefix = "/share/" + workspaceId + "/" + token;
                model.addAttribute(URL_PREFIX, urlPrefix);
                model.addAttribute("thumbnailUrl", urlPrefix + "/images/");

                return showView(view, workspaceMetaData, null, null, model, false, showHeaderAndFooter);
            }
        }

        return show404Page(model);
    }

    protected final String showAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String branch, String version, ModelMap model, boolean showHeaderAndFooter, boolean editable) {
        version = HtmlUtils.filterHtml(version);

        User user = getUser();
        if (user == null) {
            // this should never happen, because private resources (e.g. /workspace/*) are protected by Spring Security,
            // but it doesn't hurt to double check...
            return show404Page(model);
        }

        if (workspaceMetaData != null) {
            String urlPrefix = "/workspace/" + workspaceMetaData.getId();
            model.addAttribute(URL_PREFIX, urlPrefix);
            model.addAttribute("thumbnailUrl", urlPrefix + "/images/");

            if (workspaceMetaData.isOpen()) {
                model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId());
            } else if (workspaceMetaData.isShareable()) {
                model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId() + "/" + workspaceMetaData.getSharingToken());
            }

            if (version != null && version.trim().length() > 0) {
                model.addAttribute(URL_SUFFIX, "?version=" + version);
            }

            if (workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(user)) {
                return showView(view, workspaceMetaData, branch, version, model, editable, showHeaderAndFooter);
            } else if (workspaceMetaData.isReadUser(user)) {
                return showView(view, workspaceMetaData, branch, version, model, false, showHeaderAndFooter);
            }
        }

        return show404Page(model);
    }

    protected final String showView(String view, WorkspaceMetaData workspaceMetaData, String branch, String version, ModelMap model, boolean editable, boolean showHeaderAndFooter) {
        try {
            if (editable) {
                workspaceMetaData.setEditable(true);

                if (workspaceMetaData.isPublicWorkspace() || workspaceMetaData.hasNoUsersConfigured()) {
                    model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId());
                }
            } else {
                workspaceMetaData.setEditable(false);
                String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), branch, version);
                json = json.replaceAll("[\\n\\r\\f]", "");
                model.addAttribute("workspaceAsJson", JsonUtils.base64(json));
                workspaceMetaData.setApiKey("");
                workspaceMetaData.setApiSecret("");
            }

            addCommonAttributes(model, workspaceMetaData.getName(), showHeaderAndFooter);

            workspaceMetaData.setInternalVersion(version);
            model.addAttribute("workspace", workspaceMetaData);
            model.addAttribute("showToolbar", true);
            model.addAttribute("embed", false);

            if (isAuthenticated()) {
                model.addAttribute("user", getUser());
            }

            return view;
        } catch (Exception e) {
            log.error(e);
            return "500";
        }
    }

}