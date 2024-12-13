package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.Features;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class UnshareWorkspaceControllerTests extends ControllerTestsBase {

    private UnshareWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new UnshareWorkspaceController();
        model = new ModelMap();

        Configuration.init();
        Configuration.getInstance().setFeatureEnabled(Features.UI_WORKSPACE_SETTINGS);
    }

    @Test
    public void unshareWorkspace_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.unshareWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    public void unshareWorkspace_RedirectsToTheWorkspaceSettingsPage_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        workspaceMetaData.setSharingToken("1234567890");
        assertTrue(workspaceMetaData.isShareable());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void unshareWorkspace(long workspaceId) throws WorkspaceComponentException {
                fail();
            }
        });

        setUser("user2@example.com");
        String view = controller.unshareWorkspace(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isShareable());
    }

    @Test
    public void unshareWorkspace_UnsharesTheWorkspace_WhenTheWorkspaceHasNoUsersConfigured() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");
        assertTrue(workspaceMetaData.isShareable());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void unshareWorkspace(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setSharingToken("");
            }
        });

        setUser("user1@example.com");
        String view = controller.unshareWorkspace(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isShareable());
        assertEquals("", workspaceMetaData.getSharingToken());
    }

    @Test
    public void unshareWorkspace_UnsharesTheWorkspace_WhenTheUserHasAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        workspaceMetaData.setSharingToken("1234567890");
        assertTrue(workspaceMetaData.isShareable());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void unshareWorkspace(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setSharingToken("");
            }
        });

        setUser("user1@example.com");
        String view = controller.unshareWorkspace(1, model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isShareable());
        assertEquals("", workspaceMetaData.getSharingToken());
    }

    @Test
    public void unshareWorkspace_ReturnsTheFeatureNotAvailablePage_WhenTheUserHasAccessButTheFeatureIsNotEnabled() {
        Configuration.getInstance().setFeatureDisabled(Features.UI_WORKSPACE_SETTINGS);

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        workspaceMetaData.setSharingToken("1234567890");
        assertTrue(workspaceMetaData.isShareable());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void unshareWorkspace(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setSharingToken("");
            }
        });

        setUser("user1@example.com");
        String view = controller.unshareWorkspace(1, model);
        assertEquals("feature-not-available", view);
        assertTrue(workspaceMetaData.isShareable());
        assertEquals("1234567890", workspaceMetaData.getSharingToken());
    }

}