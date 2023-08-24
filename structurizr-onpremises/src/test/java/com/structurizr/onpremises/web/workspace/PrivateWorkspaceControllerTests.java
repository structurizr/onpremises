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

public class PrivateWorkspaceControllerTests extends ControllerTestsBase {

    private PrivateWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new PrivateWorkspaceController();
        model = new ModelMap();

        Configuration.init();
        Configuration.getInstance().setFeatureEnabled(Features.UI_WORKSPACE_SETTINGS);
    }

    @Test
    public void makeWorkspacePrivate_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.makeWorkspacePrivate(1, model);
        assertEquals("404", view);
    }

    @Test
    public void makeWorkspacePrivate_RedirectsToTheWorkspaceSettingsPage_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        workspaceMetaData.setPublicWorkspace(true);
        assertTrue(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePrivate(long workspaceId) throws WorkspaceComponentException {
                fail();
            }
        });

        setUser("user2@example.com");
        String view = controller.makeWorkspacePrivate(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    public void makeWorkspacePrivate_MakesTheWorkspacePrivate_WhenTheWorkspaceHasNoUsersConfigured() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setPublicWorkspace(true);
        assertTrue(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePrivate(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(false);
            }
        });

        setUser("user1@example.com");
        String view = controller.makeWorkspacePrivate(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    public void makeWorkspacePrivate_MakesTheWorkspacePrivate_WhenTheUserHasAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        workspaceMetaData.setPublicWorkspace(true);
        assertTrue(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePrivate(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(false);
            }
        });

        setUser("user1@example.com");
        String view = controller.makeWorkspacePrivate(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    public void makeWorkspacePrivate_ReturnsTheFeatureNotAvailablePage_WhenTheUserHasAccessButTheFeatureIsNotEnabled() {
        Configuration.getInstance().setFeatureDisabled(Features.UI_WORKSPACE_SETTINGS);

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        workspaceMetaData.setPublicWorkspace(true);
        assertTrue(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePrivate(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(false);
            }
        });

        setUser("user1@example.com");
        String view = controller.makeWorkspacePrivate(1, model);
        assertEquals("feature-not-available", view);
        assertTrue(workspaceMetaData.isPublicWorkspace());
    }

}