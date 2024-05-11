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

import java.util.ArrayList;
import java.util.List;

import static com.structurizr.onpremises.util.WorkspaceValidationUtils.enrichWithRemoteDocument;

/**
 * Base class for all controllers underneath /share and /workspace (i.e. the workspace related controllers).
 */
public abstract class AbstractWorkspaceController extends AbstractController {

    private static final Log log = LogFactory.getLog(AbstractWorkspaceController.class);

    protected static final String URL_PREFIX = "urlPrefix";
    protected static final String URL_SUFFIX = "urlSuffix";

    protected final String showPublicView(String view, long workspaceId, String version, ModelMap model, boolean showHeaderAndFooter) {
        return showPublicView(view, workspaceId, version, model, showHeaderAndFooter, null);
    }

    protected final String showPublicView(String view, long workspaceId, String version, ModelMap model, boolean showHeaderAndFooter, List<String> scope) {
        version = HtmlUtils.filterHtml(version);

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

                if (version != null && version.trim().length() > 0) {
                    model.addAttribute(URL_SUFFIX, "?version=" + version);
                }

                return showView(view, workspaceMetaData, version, model, false, showHeaderAndFooter,scope);
            }
        }

        return show404Page(model);
    }
    protected final String showSharedView(String view, long workspaceId, String token, String version, ModelMap model, boolean showHeaderAndFooter) {
        return showSharedView(view, workspaceId, token, version, model, showHeaderAndFooter, null);
    }
    protected final String showSharedView(String view, long workspaceId, String token, String version, ModelMap model, boolean showHeaderAndFooter, List<String> scope) {
        token = HtmlUtils.filterHtml(token);
        version = HtmlUtils.filterHtml(version);

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

                if (version != null && version.trim().length() > 0) {
                    model.addAttribute(URL_SUFFIX, "?version=" + version);
                }

                return showView(view, workspaceMetaData, version, model, false, showHeaderAndFooter,scope);
            }
        }

        return show404Page(model);
    }
    protected final String showAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String version, ModelMap model, boolean showHeaderAndFooter, boolean editable) {
        return showAuthenticatedView(view, workspaceMetaData, version, model, showHeaderAndFooter, editable, null);
    }
    protected final String showAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String version, ModelMap model, boolean showHeaderAndFooter, boolean editable, List<String> scope) {
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
                return showView(view, workspaceMetaData, version, model, editable, showHeaderAndFooter,scope);
            } else if (workspaceMetaData.isReadUser(user)) {
                return showView(view, workspaceMetaData, version, model, false, showHeaderAndFooter,scope);
            }
        }

        return show404Page(model);
    }

    protected final String showView(String view, WorkspaceMetaData workspaceMetaData, String version, ModelMap model, boolean editable, boolean showHeaderAndFooter, List<String> scope) {
        try {
            if (editable) {
                workspaceMetaData.setEditable(true);

                if (workspaceMetaData.isPublicWorkspace() || workspaceMetaData.hasNoUsersConfigured()) {
                    model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId());
                }
            } else {
                workspaceMetaData.setEditable(false);
                String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), version);
                if (view.equals("documentation")) {
                    try {
                        json =enrichWithRemoteDocument(json, scope);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
    String toScope(String softwareSystem, String container, String component) {
        return toFullScope(softwareSystem, container, component).getFirst();
    }
    List<String> toFullScope(String softwareSystem, String container, String component) {
        List<String> scope = new ArrayList<>();
        if (softwareSystem != null && container != null && component != null) {
            scope.add(softwareSystem + "/" + container + "/" + component);
            scope.add(softwareSystem);
            scope.add(container);
            scope.add(component);
        } else if (softwareSystem != null && container != null) {
            scope.add(softwareSystem + "/" + container);
            scope.add(softwareSystem);
            scope.add(container);
        } else if (softwareSystem != null) {
            scope.add(softwareSystem);
            scope.add(softwareSystem);
        } else {
            scope.add("*");
        }
        return scope;
    }
}