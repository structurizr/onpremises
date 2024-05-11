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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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

    public static String enrichWithRemoteDocument(String json, List<String> scope) throws Exception {
        Workspace workspace = WorkspaceUtils.fromJson(json);
        HashMap<String,WikiItem> wikiItems = new HashMap<>();


        if(workspace.getProperties().get("wiki.document.id") != null) {
            wikiItems.put(workspace.getProperties().get("wiki.document.id"), new WikiItem(workspace));
        }
        for (Element element : workspace.getModel().getElements()) {
            try {
                WikiItem item = new WikiItem(element);
                wikiItems.put(item.getWikiDocumentId(), item);

            } catch (IllegalArgumentException ignored) {
            }
        }

        for (String documentId : wikiItems.keySet()) {
            WikiItem wikiItem = wikiItems.get(documentId);
            if (!wikiItem.inScope(scope)) {
                wikiItem.getDocumentation().clear();
                wikiItem.getDocumentation().addSection(new Section(Format.Markdown, "## Placeholder"));
                continue;
            }
            log.debug("Fetching remote document with ID: " + documentId+" for element: " + wikiItem.getName());
            try {
                String documentation = fetchRemoteDocument(documentId);
                if(documentation.length() <5) {
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
        HttpClient client =  HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = createRequest(BASE_URL + "documents.info", "{\"id\" : \""+documentId+"\"}");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse JSON response using Jackson
        JsonNode jsonNode = mapper.readTree(response.body());
        JsonNode parent = jsonNode.get("data");
        String intro = parent.get("text").asText();
        String parentId = parent.get("id").asText();

        request = createRequest(BASE_URL + "documents.list", "{\"parentDocumentId\" : \""+parentId+"\"}");
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        jsonNode = mapper.readTree(response.body());
        JsonNode children = jsonNode.get("data");
        if (children.isArray()) {
            for (JsonNode subDocument : children) {
                String part = subDocument.get("text").asText();
                if(!part.startsWith("## ")) {
                    part = "## "+subDocument.get("title").asText()+"\n"+part;
                }
                intro += "\n"+part;
            }
        }
       return cleanEmbeds(replaceAttachments(intro.replaceAll("\\\\", "")));

    }

    private static HttpRequest createRequest(String url, String body) {
        return HttpRequest.newBuilder(URI.create(url))
                .version(HttpClient.Version.HTTP_2)
                .header("Content-Type", "application/json")
                .header("Authorization", AUTHORIZATION_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
    private static String cleanEmbeds(String doc) {
        Pattern pattern = Pattern.compile("!\\[(.*?)]\\(__(embed.*?)__\\)");
        Matcher matcher = pattern.matcher(doc);
        StringBuilder sb = new StringBuilder();
        log.info("doc: " + doc);
        while (matcher.find()) {
            matcher.appendReplacement(sb, "![" + matcher.group(1) + "](" + matcher.group(2) + ")");
        }
        matcher.appendTail(sb);

        return sb.toString();//.replaceAll("\\\\\\s*$", "");
    }
    private static String replaceAttachments(String doc) {
        Pattern pattern = Pattern.compile("!\\[.*?]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(doc);
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            if (imageUrl.startsWith("/api/attachments.redirect")) {
                String id = imageUrl.substring("/api/attachments.redirect?id=".length());
                String jsonBody = String.format("{\"id\": \"%s\"}", id);

               HttpRequest request = createRequest(BASE_URL + "attachments.redirect", jsonBody);

                String base64Image = "data:image/png;base64,";
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String redirectUrl = response.headers().firstValue("Location").get();
                    HttpRequest redirectRequest = HttpRequest.newBuilder(URI.create(redirectUrl))
                            .version(HttpClient.Version.HTTP_2)
                            .header("Accept", "image/png")
                            .GET().build();
                    HttpResponse<InputStream> redirectResponse = client.send(redirectRequest, HttpResponse.BodyHandlers.ofInputStream());
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = redirectResponse.body().read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    byte[] imageBytes = outputStream.toByteArray();

                    // Convert the byte array to a base64-encoded string
                    base64Image+= Base64.getEncoder().encodeToString(imageBytes);

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                doc = doc.replace(matcher.group(1),  base64Image );
                matcher.reset(doc);
            }
        }
        return doc;
    }

}
