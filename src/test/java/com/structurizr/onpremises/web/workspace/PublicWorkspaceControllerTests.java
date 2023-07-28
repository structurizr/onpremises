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

public class PublicWorkspaceControllerTests extends ControllerTestsBase {

    private PublicWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new PublicWorkspaceController();
        model = new ModelMap();

        Configuration.init();
        Configuration.getInstance().setFeatureEnabled(Features.UI_WORKSPACE_SETTINGS);
    }

    @Test
    public void makeWorkspacePublic_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.makeWorkspacePublic(1, model);
        assertEquals("404", view);
    }

    @Test
    public void makeWorkspacePublic_RedirectsToTheWorkspaceSettingsPage_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                fail();
            }
        });

        setUser("user2@example.com");
        String view = controller.makeWorkspacePublic(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    public void makeWorkspacePublic_MakesTheWorkspacePublic_WhenTheWorkspaceHasNoUsersConfigured() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(true);
            }
        });

        setUser("user1@example.com");
        String view = controller.makeWorkspacePublic(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    public void makeWorkspacePublic_MakesTheWorkspacePublic_WhenTheUserHasAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(true);
            }
        });

        setUser("user1@example.com");
        String view = controller.makeWorkspacePublic(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    public void makeWorkspacePublic_ReturnsTheFeatureNotAvailablePage_WhenTheUserHasAccessButTheFeatureIsNotEnabled() {
        Configuration.getInstance().setFeatureDisabled(Features.UI_WORKSPACE_SETTINGS);

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(true);
            }
        });

        setUser("user1@example.com");
        String view = controller.makeWorkspacePublic(1, model);
        assertEquals("feature-not-available", view);
        assertFalse(workspaceMetaData.isPublicWorkspace());
    }

}