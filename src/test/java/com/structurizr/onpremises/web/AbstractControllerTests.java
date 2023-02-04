package com.structurizr.onpremises.web;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.home.HomePageController;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static com.structurizr.onpremises.web.TestUtils.clearUser;
import static com.structurizr.onpremises.web.TestUtils.setUser;
import static org.junit.Assert.*;

public class AbstractControllerTests {

    private AbstractController controller;
    private MockWorkspaceComponent workspaceComponent;

    @Before
    public void setUp() {
        controller = new HomePageController(); // any controller will do
    }

    @Test
    public void getWorkspaces_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace
        workspace1.addWriteUser("user1");
        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // private workspace
        workspace2.addWriteUser("user2");
        WorkspaceMetaData workspace3 = new WorkspaceMetaData(3); // open workspace

        workspaceComponent = new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                return Arrays.asList(
                        workspace1, workspace2, workspace3
                );
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);
        clearUser();

        Collection<WorkspaceMetaData> workspaces = controller.getWorkspaces();
        assertEquals(1, workspaces.size());
        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace
        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 2)); // private workspace
        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 3)); // open workspace
    }

    @Test
    public void getWorkspace_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace
        workspace1.addWriteUser("user1");
        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // private workspace
        workspace2.addWriteUser("user2");
        WorkspaceMetaData workspace3 = new WorkspaceMetaData(3); // open workspace

        workspaceComponent = new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                return Arrays.asList(
                        workspace1, workspace2, workspace3
                );
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);
        clearUser();

        assertNull(controller.getWorkspace(1)); // private workspace
        assertNull(controller.getWorkspace(2)); // private workspace
        assertEquals(3, controller.getWorkspace(3).getId()); // open workspace
    }

    @Test
    public void getWorkspaces_WhenAuthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace, read/write access
        workspace1.addWriteUser("user1");
        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // private workspace, read-only access
        workspace2.addWriteUser("user2");
        workspace2.addReadUser("user1");
        WorkspaceMetaData workspace3 = new WorkspaceMetaData(3); // open workspace
        WorkspaceMetaData workspace4 = new WorkspaceMetaData(3); // private workspace, no access
        workspace4.addWriteUser("user4");

        workspaceComponent = new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                return Arrays.asList(
                        workspace1, workspace2, workspace3, workspace4
                );
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);
        setUser("user1");

        Collection<WorkspaceMetaData> workspaces = controller.getWorkspaces();
        assertEquals(3, workspaces.size());
        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace, read/write access
        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 2)); // private workspace, read-only access
        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 3)); // open workspace
        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 4)); // private workspace, no access
    }

    @Test
    public void getWorkspace_WhenAuthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace, read/write access
        workspace1.addWriteUser("user1");
        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // private workspace, read-only access
        workspace2.addWriteUser("user2");
        workspace2.addReadUser("user1");
        WorkspaceMetaData workspace3 = new WorkspaceMetaData(3); // open workspace
        WorkspaceMetaData workspace4 = new WorkspaceMetaData(3); // private workspace, no access
        workspace4.addWriteUser("user4");

        workspaceComponent = new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                return Arrays.asList(
                        workspace1, workspace2, workspace3, workspace4
                );
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);
        setUser("user1");

        assertEquals(1, controller.getWorkspace(1).getId()); // private workspace, read/write access
        assertEquals(2, controller.getWorkspace(2).getId()); // private workspace, read-only access
        assertEquals(3, controller.getWorkspace(3).getId()); // open workspace
        assertNull(controller.getWorkspace(4)); // private workspace, no access
    }

    @Test
    public void isAuthenticated() {
        clearUser();
        assertFalse(controller.isAuthenticated());

        setUser("username");
        assertTrue(controller.isAuthenticated());

        clearUser();
        assertFalse(controller.isAuthenticated());
    }

}