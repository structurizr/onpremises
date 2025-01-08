package com.structurizr.onpremises.web.dsl;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
import com.structurizr.dsl.Features;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.util.JsonUtils;
import com.structurizr.onpremises.util.WorkspaceValidationUtils;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.DslTemplate;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.validation.WorkspaceScopeValidationException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Controller
public class PublicDslController extends AbstractController {

    private static final int HTTP_OK_STATUS = 200;

    @RequestMapping(value = "/dsl", method = RequestMethod.GET)
    public String showDslDemoPage(
            @RequestParam(required = false, defaultValue = "") String src,
            @RequestParam(required = false, defaultValue = "") String view,
            ModelMap model) throws Exception {

        if (!StringUtils.isNullOrEmpty(src) && src.startsWith("https://")) {
            src = getContentFromUrl(src, model);
        }

        model.addAttribute("method", "get");

        return show(model, src, null, view);
    }

    @RequestMapping(value = "/dsl", method = RequestMethod.POST)
    public String postDsl(ModelMap model,
                       @RequestParam(required = true) String source,
                       @RequestParam(required = false) String json,
                       @RequestParam(required = false, defaultValue = "") String view) throws Exception {

        model.addAttribute("method", "post");

        return show(model, source, json, view);
    }

    public String show(ModelMap model, String source, String json, String view) throws Exception {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(0);
        workspaceMetaData.setName("Workspace");
        workspaceMetaData.setEditable(true);
        workspaceMetaData.setLastModifiedDate(new Date());

        if (StringUtils.isNullOrEmpty(source)) {
            source = DslTemplate.generate("Name", "Description");
        }

        view = HtmlUtils.filterHtml(view);

        Workspace workspace = null;

        try {
            workspace = fromDsl(source);
        } catch (StructurizrDslParserException e) {
            model.addAttribute("line", e.getLineNumber());
            model.addAttribute("errorMessage", e.getMessage());
        } catch (WorkspaceScopeValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("dslVersion", Class.forName(StructurizrDslParser.class.getCanonicalName()).getPackage().getImplementationVersion());
        model.addAttribute("source", source);
        model.addAttribute("view", view);
        model.addAttribute("workspace", workspaceMetaData);

        if (workspace != null) {
            workspace.setLastModifiedDate(new Date());

            if (!StringUtils.isNullOrEmpty(json)) {
                Workspace oldWorkspace = WorkspaceUtils.fromJson(json);

                try {
                    workspace.getViews().copyLayoutInformationFrom(oldWorkspace.getViews());
                } catch (Exception e) {
                    // ignore
                }
            }

            model.addAttribute("workspaceAsJson", JsonUtils.base64(WorkspaceUtils.toJson(workspace, false)));
        } else {
            if (!StringUtils.isNullOrEmpty(json)) {
                Workspace oldWorkspace = WorkspaceUtils.fromJson(json);
                model.addAttribute("workspaceAsJson", JsonUtils.base64(WorkspaceUtils.toJson(oldWorkspace, false)));
            } else {
                model.addAttribute("workspaceAsJson", JsonUtils.base64(WorkspaceUtils.toJson(new Workspace("Workspace", ""), false)));
            }
        }

        addCommonAttributes(model, "DSL", false);

        return "dsl-public";
    }

    private Workspace fromDsl(String dsl) throws StructurizrDslParserException, WorkspaceScopeValidationException {
        StructurizrDslParser parser = new StructurizrDslParser();
        parser.getFeatures().configure(Features.ARCHETYPES, Configuration.PREVIEW_FEATURES);
        parser.setRestricted(true);
        parser.parse(dsl);

        Workspace workspace = parser.getWorkspace();
        DslUtils.setDsl(workspace, dsl);

        // add default views if no views are explicitly defined
        if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
            workspace.getViews().createDefaultViews();
        }

        WorkspaceValidationUtils.validateWorkspaceScope(workspace);

        return workspace;
    }

    private String getContentFromUrl(String url, ModelMap model) {
        try {
            CloseableHttpClient httpClient = HttpClients.createSystem();
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getCode() == HTTP_OK_STATUS) {
                return EntityUtils.toString(response.getEntity());
            } else {
                model.addAttribute("errorMessage", EntityUtils.toString(response.getEntity()));
            }
            httpClient.close();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "";
    }

}