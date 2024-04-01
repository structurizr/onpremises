package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EmbedControllerTests {

    private EmbedController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new EmbedController();
        model = new ModelMap();
    }

    @Test
    public void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.embedDiagrams(1, "version", "viewKey", "apiKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    public void embedDiagrams_ReturnsTheDiagramsPage_WhenTheWorkspaceHasNoUsersConfigured() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "version", "viewKey", "apiKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/share/1", model.get("urlPrefix"));
    }

    @Test
    public void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceIsPrivateAndTheApiKeyIsNotProvided() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(1);
                wmd.addWriteUser("user@example.com");
                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "version", "viewKey", "", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    public void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceIsPrivateAndTheApiKeyIsIncorrect() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(1);
                wmd.addWriteUser("user@example.com");
                wmd.setApiKey("0987654321");
                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "version", "viewKey", "1234567890", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    public void embedDiagrams_ReturnsTheDiagramsPage_WhenTheWorkspaceIsPrivateAndTheApiKeyIsCorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        workspaceMetaData.setApiKey("1234567890");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "version", "viewKey", "1234567890", false, "iframe", false, "perspective", model);
        assertEquals("diagrams", view);
        assertEquals("/workspace/1", model.get("urlPrefix"));
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
    }

    @Test
    public void embedDiagramsViaSharingToken_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "token", "version", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    public void embedDiagramsViaSharingToken_ReturnsThe404Page_WhenTheWorkspaceIsNotShareable() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "token", "version", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    public void embedDiagramsViaSharingToken_ReturnsThe404Page_WhenTheTokenIsIncorrect() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.setSharingToken("0987654321");
                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "1234567890", "version", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    public void embedDiagramsViaSharingToken_ReturnsTheDiagramsPage_WhenTheTokenIsCorrect() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "1234567890", "version", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("diagrams", view);
        assertEquals("/share/1/1234567890", model.get("urlPrefix"));
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
    }

}