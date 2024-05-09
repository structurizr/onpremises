package com.structurizr.onpremises.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.fasterxml.jackson.databind.JsonNode;
import com.structurizr.model.Element;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentImpl;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.validation.WorkspaceScopeValidationException;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkspaceValidationUtils {
    private static final Log log = LogFactory.getLog(WorkspaceComponentImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "https://wiki.moarse.ru/api/";
    private static final String AUTHORIZATION_VALUE = "Bearer ol_api_8MjosoOPe7UMT3gmjJtxO7lJ9aObD0RtdxPJy9";

    public static void validateWorkspaceScope(Workspace workspace) throws WorkspaceScopeValidationException {
        // if workspace scope validation is enabled, reject workspaces without a defined scope
        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_SCOPE_VALIDATION)) {
            if (workspace.getConfiguration().getScope() == null) {
                throw new WorkspaceScopeValidationException("Strict workspace scope validation has been enabled for this on-premises installation. Unscoped workspaces are not permitted - see https://docs.structurizr.com/workspaces for more information.");
            }
        }

        // validate workspace scope
        WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);
    }

    public static String enrichWithRemoteDocument(String json) throws Exception {
        Workspace workspace = WorkspaceUtils.fromJson(json);
        HashMap<String,WikiItem> wikiItems = new HashMap<>();
        if(workspace.getProperties().get("wiki.document.id") != null) {
            wikiItems.put(workspace.getProperties().get("wiki.document.id"), new WikiItem(workspace));
        }
        for (Element element : workspace.getModel().getElements()) {
            if(element.getProperties().get("wiki.document.id") != null) {
                wikiItems.put(element.getProperties().get("wiki.document.id"), new WikiItem(element));
            }
        }

        for (String documentId : wikiItems.keySet()) {
            WikiItem wikiItem = wikiItems.get(documentId);
            log.debug("Fetching remote document with ID: " + documentId+" for element: " + wikiItem.getName());
            try {
                String documentation = fetchRemoteDocument(documentId);
                if(documentation == null || documentation.length() <5) {
                    throw new Exception("Could not fetch remote document");
                }
                wikiItem.getDocumentation().clear();
                wikiItem.getDocumentation().addSection(new Section(Format.Markdown, documentation));
            } catch (Exception e) {
                log.error("Error fetching remote document", e);
            }
        }
        return WorkspaceUtils.toJson(workspace, false);

    }

    public static String fetchRemoteDocument(String documentId) throws IOException, InterruptedException {
        // Define constants or configuration properties



        // Create an HTTP client with a reusable configuration
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = createRequest(BASE_URL + "documents.info", "{\"id\" : \""+documentId+"\"}");
        log.debug("Request 1:"+request.toString());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse JSON response using Jackson
        JsonNode jsonNode = mapper.readTree(response.body());
        log.debug("Response: {}"+ jsonNode);

        JsonNode parent = jsonNode.get("data");
        log.debug(parent.isObject()+" Intro: {}"+parent);
        String intro = parent.get("text").asText();
        String parentId = parent.get("id").asText();


        request = createRequest(BASE_URL + "documents.list", "{\"parentDocumentId\" : \""+parentId+"\"}");
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        jsonNode = mapper.readTree(response.body());
        JsonNode children = jsonNode.get("data");
        log.debug(children.isArray()+" Children: {}" + children);
        if (children.isArray()) {
            for (JsonNode subDocument : children) {
                log.debug("Child: {}"+ subDocument);
                intro += "\n## "+subDocument.get("title").asText()+"\n" + subDocument.get("text").asText().replaceAll("\\\\","");
            }
        }

       return replaceAttachements(intro);

    }

    private static HttpRequest createRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", AUTHORIZATION_VALUE)
                .method("POST", HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private static String replaceAttachements(String doc) {
        Pattern pattern = Pattern.compile("!\\[.*?]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(doc);
        HttpClient client = HttpClient.newHttpClient();
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            if (imageUrl.startsWith("/api/attachments.redirect")) {
                String id = imageUrl.substring("/api/attachments.redirect?id=".length());
                String jsonBody = String.format("{\"id\": \"%s\"}", id);

               HttpRequest request = createRequest(BASE_URL + "attachments.redirect", jsonBody);

                String base64Image = "data:image/png;base64,";
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println(response.body());
                    base64Image+=response.body();
//                    request = HttpRequest.newBuilder()
//                            .uri(URI.create(response.body().substring(15,-1)))
//                            .GET()
//                            .build();
                    //response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    //System.out.println(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                doc = doc.replace(matcher.group(), "![" + id + "](" + base64Image + ")");
                matcher.reset(doc);
            }
        }
        return doc;
    }

}
