package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Features;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class UsersControllerTests extends ControllerTestsBase {

    private UsersController controller;
    private ModelMap model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    public void setUp() {
        controller = new UsersController();
        model = new ModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
        Configuration.init();
        clearUser();
    }

    @Test
    public void showUsers_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showUsers(1, model);
        assertEquals("404", view);
    }

    @Test
    public void showUsers_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
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
        String view = controller.showUsers(1, model);
        assertEquals("404", view);
    }

    @Test
    public void showUsers_ReturnsTheUsersPage_WhenTheWorkspaceHasNoUsersConfigured()  {
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
        });

        setUser("user@example.com");
        String view = controller.showUsers(1, model);
        assertEquals("users", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
    }

    @Test
    public void showUsers_ReturnsTheUsersPage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("read1@example.com");
        workspaceMetaData.addReadUser("read2@example.com");
        workspaceMetaData.addWriteUser("write1@example.com");
        workspaceMetaData.addWriteUser("write2@example.com");
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

        setUser("write1@example.com");
        String view = controller.showUsers(1, model);
        assertEquals("users", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("read1@example.com\nread2@example.com\n", model.getAttribute("readUsers"));
        assertEquals("write1@example.com\nwrite2@example.com\n", model.getAttribute("writeUsers"));
    }

    @Test
    public void showUsers_ReturnsTheUsersPage_WhenTheUserHasWriteAccessButTheFeatureIsNotEnabled()  {
        Configuration.getInstance().setFeatureDisabled(Features.UI_WORKSPACE_USERS);
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("write@example.com");
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

        setUser("write@example.com");
        String view = controller.showUsers(1, model);
        assertEquals("users", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable()); // feature not available, so can't modify
    }

    @Test
    public void showUsers_ReturnsTheUsersPage_WhenTheUserHasReadAccess()  {
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
        String view = controller.showUsers(1, model);
        assertEquals("users", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable());
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
    }

    @Test
    public void updateUsers_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.updateUsers(1, "read", "write", model, redirectAttributes);
        assertEquals("404", view);
    }

    @Test
    public void updateUsers_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
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
        String view = controller.updateUsers(1, "read", "write", model, redirectAttributes);
        assertEquals("404", view);
    }

    @Test
    public void updateUsers_UpdatesTheUsers_WhenTheWorkspaceIsPublic()  {
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
        });

        setUser("user@example.com");
        String view = controller.updateUsers(1, "read", "write", model, redirectAttributes);
        assertEquals("redirect:/workspace/1/users", view);
        assertTrue(workspaceMetaData.getReadUsers().contains("read"));
        assertTrue(workspaceMetaData.getWriteUsers().contains("write"));
    }

    @Test
    public void updateUsers_ReturnsThe404Page_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        workspaceMetaData.addWriteUser("user2@example.com");
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
        String view = controller.updateUsers(1, "read", "write", model, redirectAttributes);
        assertEquals("404", view);
        assertEquals(1, workspaceMetaData.getReadUsers().size());
        assertTrue(workspaceMetaData.getReadUsers().contains("user1@example.com"));
        assertEquals(1, workspaceMetaData.getWriteUsers().size());
        assertTrue(workspaceMetaData.getWriteUsers().contains("user2@example.com"));
    }

    @Test
    public void updateUsers_UpdatesTheUsers_WhenTheUserHasWriteAccess()  {
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
        });

        setUser("user1@example.com");
        String view = controller.updateUsers(1, "read@example.com", "user1@example.com\nwrite@example.com", model, redirectAttributes);
        assertEquals("redirect:/workspace/1/users", view);
        assertEquals(1, workspaceMetaData.getReadUsers().size());
        assertTrue(workspaceMetaData.getReadUsers().contains("read@example.com"));
        assertEquals(2, workspaceMetaData.getWriteUsers().size());
        assertTrue(workspaceMetaData.getWriteUsers().contains("user1@example.com"));
        assertTrue(workspaceMetaData.getWriteUsers().contains("write@example.com"));
    }

    @Test
    public void updateUsers_ReturnsTheFeatureUnavailablePage_WhenTheUserHasWriteAccessButTheFeatureIsNotEnabled()  {
        Configuration.getInstance().setFeatureDisabled(Features.UI_WORKSPACE_USERS);
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.updateUsers(1, "read@example.com", "user1@example.com\nwrite@example.com", model, redirectAttributes);
        assertEquals("feature-not-available", view);
    }

    @Test
    public void updateUsers_UpdatesTheUsers_AndIncludesTheCurrentUserWhenTheyWouldOtherwiseBeLockedOut()  {
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
        });

        setUser("user@example.com");
        String view = controller.updateUsers(1, "read", "", model, redirectAttributes); // setting only a read-only user would lock the current user out
        assertEquals("redirect:/workspace/1/users", view);
        assertTrue(workspaceMetaData.getReadUsers().contains("read"));
        assertTrue(workspaceMetaData.getWriteUsers().contains("user@example.com"));
    }

}