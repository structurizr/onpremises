package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class DiagramEditorControllerTests extends ControllerTestsBase {

    private DiagramEditorController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DiagramEditorController();
        model = new ModelMap();
        Configuration.init();
        clearUser();
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
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

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheWorkspaceIsPublic()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
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

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "version", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
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

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "version", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsAnErrorPage_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
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

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "version", model);
        assertEquals("workspace-is-readonly", view);
    }

}