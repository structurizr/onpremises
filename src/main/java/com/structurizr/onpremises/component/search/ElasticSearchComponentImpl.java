package com.structurizr.onpremises.component.search;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Documentation;
import com.structurizr.documentation.Section;
import com.structurizr.model.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * A search component implementation that uses Elasticsearch.
 */
class ElasticSearchComponentImpl extends AbstractSearchComponentImpl {

    private static Log log = LogFactory.getLog(ElasticSearchComponentImpl.class);

    private static final String INDEX_NAME = "structurizr";
    private static final String DOCUMENT_TYPE = "document";
    private static final String WORKSPACE_KEY = "workspace";

    private static final int SNIPPET_LENGTH = 400;

    boolean async = true; // this is used for testing purposes only

    private final String host;
    private final int port;
    private final String protocol;
    private final String user;
    private final String password;
    private String indexName = INDEX_NAME;

    private RestClient restLowLevelClient;

    ElasticSearchComponentImpl(String host, int port, String protocol, String user, String password) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.user = user;
        this.password = password;
    }

    String getIndexName() {
        return indexName;
    }

    void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public void index(Workspace workspace) {
        try {
            delete(workspace.getId());

            Document document = new Document();
            document.setUrl("");
            document.setWorkspace(toString(workspace.getId()));
            document.setType(DocumentType.WORKSPACE);
            document.setName(workspace.getName());
            document.setDescription(workspace.getDescription());
            document.setContent(appendAll(workspace.getName(), workspace.getDescription()));

            sendIndexRequest(document);

            for (CustomView view : workspace.getViews().getCustomViews()) {
                index(workspace, view);
            }
            for (SystemLandscapeView view : workspace.getViews().getSystemLandscapeViews()) {
                index(workspace, view);
            }
            for (SystemContextView view : workspace.getViews().getSystemContextViews()) {
                index(workspace, view);
            }
            for (ContainerView view : workspace.getViews().getContainerViews()) {
                index(workspace, view);
            }
            for (ComponentView view : workspace.getViews().getComponentViews()) {
                index(workspace, view);
            }
            for (DynamicView view : workspace.getViews().getDynamicViews()) {
                index(workspace, view);
            }
            for (DeploymentView view : workspace.getViews().getDeploymentViews()) {
                index(workspace, view);
            }

            indexDocumentationAndDecisions(workspace, null, workspace.getDocumentation());

            for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
                indexDocumentationAndDecisions(workspace, softwareSystem, softwareSystem.getDocumentation());

                for (Container container : softwareSystem.getContainers()) {
                    indexDocumentationAndDecisions(workspace, container, container.getDocumentation());

                    for (Component component : container.getComponents()) {
                        indexDocumentationAndDecisions(workspace, component, component.getDocumentation());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while indexing", e);
        }
    }

    private void index(Workspace workspace, ModelView view) throws Exception {
        Document document = new Document();
        document.setUrl(DIAGRAMS_PATH + "#" + view.getKey());
        document.setWorkspace(toString(workspace.getId()));
        document.setType(DocumentType.DIAGRAM);
        document.setName(view.getName());
        document.setDescription(view.getDescription());

        StringBuilder content = new StringBuilder();

        if (!StringUtils.isNullOrEmpty(view.getTitle())) {
            content.append(view.getTitle());
            content.append(" ");
        } else if (!StringUtils.isNullOrEmpty(view.getName())) {
            content.append(view.getName());
            content.append(" ");
        }

        if (!StringUtils.isNullOrEmpty(view.getDescription())) {
            content.append(view.getDescription());
            content.append(" ");
        }

        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();

            if (element instanceof CustomElement || element instanceof Person || element instanceof SoftwareSystem) {
                content.append(indexElementBasics(element));
            }

            if (element instanceof Container) {
                content.append(indexElementBasics(element));
                String technology = ((Container)element).getTechnology();
                if (!StringUtils.isNullOrEmpty(technology)) {
                    content.append(technology);
                    content.append(" ");
                }
            }

            if (element instanceof Component) {
                content.append(indexElementBasics(element));
                String technology = ((Component)element).getTechnology();
                if (!StringUtils.isNullOrEmpty(technology)) {
                    content.append(technology);
                    content.append(" ");
                }
            }

            if (element instanceof DeploymentNode) {
                content.append(index((DeploymentNode)element));
                content.append(" ");
            }
        }

        for (RelationshipView relationshipView : view.getRelationships()) {
            Relationship relationship = relationshipView.getRelationship();
            if (!StringUtils.isNullOrEmpty(relationship.getDescription())) {
                content.append(relationship.getDescription());
                content.append(" ");
            }

            if (!StringUtils.isNullOrEmpty(relationship.getTechnology())) {
                content.append(relationship.getTechnology());
                content.append(" ");
            }
        }

        document.setContent(content.toString());

        sendIndexRequest(document);
    }

    private String indexElementBasics(Element element) {
        StringBuilder content = new StringBuilder();

        if (!StringUtils.isNullOrEmpty(element.getName())) {
            content.append(element.getName());
            content.append(" ");
        }
        if (!StringUtils.isNullOrEmpty(element.getDescription())) {
            content.append(element.getDescription());
            content.append(" ");
        }

        return content.toString();
    }

    private String index(DeploymentNode deploymentNode) {
        StringBuilder content = new StringBuilder();

        content.append(indexElementBasics(deploymentNode));
        String technology = deploymentNode.getTechnology();
        if (!StringUtils.isNullOrEmpty(technology)) {
            content.append(technology);
            content.append(" ");
        }

        for (DeploymentNode child : deploymentNode.getChildren()) {
            content.append(index(child));
        }

        for (InfrastructureNode infrastructureNode : deploymentNode.getInfrastructureNodes()) {
            content.append(indexElementBasics(infrastructureNode));
            String infrastructureNodeTechnology = infrastructureNode.getTechnology();
            if (!StringUtils.isNullOrEmpty(infrastructureNodeTechnology)) {
                content.append(infrastructureNodeTechnology);
                content.append(" ");
            }
        }

        for (SoftwareSystemInstance softwareSystemInstance : deploymentNode.getSoftwareSystemInstances()) {
            SoftwareSystem softwareSystem = softwareSystemInstance.getSoftwareSystem();
            content.append(indexElementBasics(softwareSystem));
        }

        for (ContainerInstance containerInstance : deploymentNode.getContainerInstances()) {
            Container container = containerInstance.getContainer();
            content.append(indexElementBasics(container));
            String containerInstanceTechnology = container.getTechnology();
            if (!StringUtils.isNullOrEmpty(containerInstanceTechnology)) {
                content.append(containerInstanceTechnology);
                content.append(" ");
            }
        }

        return content.toString();
    }

    private void indexDocumentationAndDecisions(Workspace workspace, Element element, Documentation documentation) throws Exception {
        if (documentation != null) {
            StringBuilder documentationContent = new StringBuilder();
            for (Section section : documentation.getSections()) {
                documentationContent.append(section.getContent());
                documentationContent.append(NEWLINE);
            }
            indexDocumentation(workspace, element, documentationContent.toString());

            for (Decision decision : documentation.getDecisions()) {
                indexDecision(workspace, element, decision);
            }
        }
    }

    private void indexDocumentation(Workspace workspace, Element element, String documentationContent) throws Exception {
        // split the entire documentation content up into sections, each of which is defined by a ## or == heading.
        String title = "";
        StringBuilder content = new StringBuilder();
        String[] lines = documentationContent.split(NEWLINE);
        int sectionNumber = 0;

        for (String line : lines) {
            if (line.startsWith(MARKDOWN_SECTION_HEADING) || line.startsWith(ASCIIDOC_SECTION_HEADING)) {
                indexDocumentationSection(title, content.toString(), sectionNumber, workspace, element);
                title = line.substring(MARKDOWN_SECTION_HEADING.length()-1).trim();
                content = new StringBuilder();
                sectionNumber++;
            } else {
                content.append(line);
                content.append(NEWLINE);
            }
        }

        if (content.length() > 0) {
            indexDocumentationSection(title, content.toString(), sectionNumber, workspace, element);
        }
    }

    private void indexDocumentationSection(String title, String content, int sectionNumber, Workspace workspace, Element element) throws Exception {
        Document document = new Document();

        document.setUrl(DOCUMENTATION_PATH + calculateUrlForSection(element, sectionNumber));
        document.setWorkspace(toString(workspace.getId()));
        document.setType(DocumentType.DOCUMENTATION);

        if (element == null) {
            if (!StringUtils.isNullOrEmpty(title)) {
                document.setName(workspace.getName() + " - " + title);
            } else {
                document.setName(workspace.getName());
            }
        } else {
            if (!StringUtils.isNullOrEmpty(title)) {
                document.setName(element.getName() + " - " + title);
            } else {
                document.setName(element.getName());
            }
        }

        String snippet = "";
        if (content != null) {
            if (content.length() > SNIPPET_LENGTH) {
                snippet = content.substring(0, SNIPPET_LENGTH) + "...";
            } else {
                snippet = content;
            }
        }
        document.setDescription(filterMarkup(snippet));
        document.setContent(appendAll(title, content));

        sendIndexRequest(document);
    }

    private void indexDecision(Workspace workspace, Element element, Decision decision) throws Exception {
        Document document = new Document();

        document.setUrl(DECISIONS_PATH + calculateUrlForDecision(element, decision));
        document.setWorkspace(toString(workspace.getId()));
        document.setType(DocumentType.DECISION);

        if (element == null) {
            document.setName(workspace.getName() + " - " + decision.getId() + ". " + decision.getTitle());
        } else {
            document.setName(element.getName() + " - " + decision.getId() + ". " + decision.getTitle());
        }

        document.setDescription(decision.getStatus());
        document.setContent(appendAll(decision.getTitle(), decision.getContent(), decision.getStatus()));

        sendIndexRequest(document);
    }

    @Override
    public void delete(long workspaceId) {
        try {
            String method = "POST";
            String url = "/" + getIndexName() + "/_delete_by_query";

            if (!async) {
                url +=  "?refresh=true";
            }

            Request request = new Request(method, url);
            request.addParameter("q", WORKSPACE_KEY + ":" + toString(workspaceId));

            restLowLevelClient.performRequest(request);
        } catch (Exception e) {
            log.error("There was an error while deleting workspace " + workspaceId + " from the search index.", e);
        }
    }

    private String appendAll(String... strings) {
        StringBuilder buf = new StringBuilder();

        for (String s : strings) {
            if (!StringUtils.isNullOrEmpty(s)) {
                buf.append(s);
                buf.append(" ");
            }
        }

        return buf.toString();
    }

    private String filterMarkup(String source) {
        source = source.replaceAll("#", "");
        source = source.replaceAll("=", "");

        return source;
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) {
        if (workspaceIds.isEmpty()) {
            throw new IllegalArgumentException("One or more workspace IDs must be provided.");
        }

        List<SearchResult> results = new ArrayList<>();

        try {
            String searchRequest = createSearchRequest(query, type, workspaceIds);

            Map<String, String> params = Collections.emptyMap();
            HttpEntity entity = new NStringEntity(searchRequest, ContentType.APPLICATION_JSON);

            Request request = new Request("POST", "/" + getIndexName() + "/_search");
            request.setEntity(entity);

            Response response = restLowLevelClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SearchResponse searchResponse = objectMapper.readValue(responseBody, SearchResponse.class);

            List<SearchHit> searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Document source = searchHit.getSource();
                SearchResult result = new SearchResult(
                        Long.parseLong(source.getWorkspace()),
                        source.getUrl(),
                        source.getName(),
                        source.getDescription(),
                        source.getType()
                );
                results.add(result);
            }

        } catch (Exception e) {
            log.error("Error while searching", e);
        }

        return results;
    }

    public void start() {
        if (StringUtils.isNullOrEmpty(user)) {
            restLowLevelClient = RestClient.builder(new HttpHost(host, port, protocol)).build();
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

            RestClientBuilder clientBuilder = RestClient.builder(
                    new HttpHost(host, port, protocol))
                    .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                        @Override
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                            return httpAsyncClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider);
                        }
                    });

            restLowLevelClient = clientBuilder.build();
        }
    }

    public void stop() {
        try {
            restLowLevelClient.close();
        } catch (Exception e) {
            log.error("Error closing connection", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private void sendIndexRequest(Document document) {
        try {
            String jsonString = new ObjectMapper().writeValueAsString(document);
            HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
            String method = "POST";
            String url = "/" + getIndexName() + "/" + DOCUMENT_TYPE;

            if (async) {
                ResponseListener responseListener = new ResponseListener() {
                    @Override

                    public void onSuccess(Response response) {
                        // do nothing
                    }

                    @Override
                    public void onFailure(Exception e) {
                        log.error("Error while indexing content for workspace", e);
                    }
                };

                try {
                    Request request = new Request(method, url);
                    request.setEntity(entity);

                    restLowLevelClient.performRequestAsync(request, responseListener);
                } catch (Exception e) {
                    log.error("Error while indexing");
                }
            } else {
                try {
                    Request request = new Request(method, url + "?refresh=true");
                    request.setEntity(entity);

                    Response response = restLowLevelClient.performRequest(request);
                    if (response.getStatusLine().getStatusCode() != 201) {
                        log.error(response.getStatusLine().getReasonPhrase());
                    }
                } catch (Exception e) {
                    log.error("Error while indexing", e);
                }
            }
        } catch (Exception e) {
            log.error("Error while indexing", e);
        }
    }

    private static final String TEMPLATE_WITHOUT_TYPE =
            "{\n" +
                    "    \"query\": {\n" +
                    "        \"bool\": {\n" +
                    "            \"must\": [\n" +
                    "                {\n" +
                    "                    \"match_phrase\": {\"content\": \"$PHRASE$\"}\n" +
                    "                },\n" +
                    "                {\"bool\": {\n" +
                    "                    \"should\": [\n" +
                    "                        $WORKSPACES$\n" +
                    "                    ],\n" +
                    "                   \"minimum_should_match\": 1\n" +
                    "                }}\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

    private static final String TEMPLATE_WITH_TYPE =
            "{\n" +
                    "    \"query\": {\n" +
                    "        \"bool\": {\n" +
                    "            \"must\": [\n" +
                    "                {\n" +
                    "                    \"match_phrase\": {\"content\": \"$PHRASE$\"}\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"term\": {\"type\": \"$TYPE$\"}\n" +
                    "                },\n" +
                    "                {\"bool\": {\n" +
                    "                    \"should\": [\n" +
                    "                        $WORKSPACES$\n" +
                    "                    ],\n" +
                    "                   \"minimum_should_match\": 1\n" +
                    "                }}\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

    private String createSearchRequest(String phrase, String type, Set<Long> workspaceIds) {
        String request;

        if (StringUtils.isNullOrEmpty(type)) {
            request = TEMPLATE_WITHOUT_TYPE;
        } else {
            request = TEMPLATE_WITH_TYPE;
            request = request.replace("$TYPE$", type.toLowerCase());
        }

        request = request.replace("$PHRASE$", phrase);

        StringBuilder buf = new StringBuilder();
        for (Long workspaceId : workspaceIds) {
            buf.append("{\"term\": {\"workspace\": \"" + toString(workspaceId) + "\"}},");
        }

        request = request.replace("$WORKSPACES$", buf.toString().substring(0, buf.length() - 1));

        return request;
    }

}