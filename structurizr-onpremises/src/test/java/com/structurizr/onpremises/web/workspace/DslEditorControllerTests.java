package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Features;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DslEditorControllerTests extends ControllerTestsBase {

    private DslEditorController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DslEditorController();
        model = new ModelMap();
        Configuration.init();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        clearUser();
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsAnErrorPage_WhenTheDslEditorHasBeenDisabled() {
        Configuration.getInstance().setFeatureDisabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("dsl-editor-disabled", view);
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsAnErrorPage_WhenTheWorkspaceIsClientSideEncrypted() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setClientSideEncrypted(true);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("workspace-is-client-side-encrypted", view);
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsTheDslEditorPage_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("dsl-editor", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr-onpremises/dsl-editor/"));
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsTheDslEditorPage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("dsl-editor", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr-onpremises/dsl-editor/"));
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsAnErrorPage_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "version", model);
        assertEquals("workspace-is-readonly", view);
    }

}