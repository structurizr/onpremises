package com.structurizr.onpremises.web.home;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomePageControllerTests extends ControllerTestsBase {

    private HomePageController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new HomePageController();

        model = new ModelMap();
        Configuration.init();
    }

    @Test
    public void showUnauthenticatedHomePage_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        clearUser();

        String result = controller.showUnauthenticatedHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
    }

    @Test
    public void showUnauthenticatedHomePage_WhenAuthenticated() {
        setUser("user@example.com");

        String result = controller.showUnauthenticatedHomePage("", 1, 20, model);

        assertEquals("redirect:/dashboard", result);
    }

    @Test
    public void showAuthenticatedDashboard_WhenAuthenticatedAndNoAdminUsersHaveBeenDefined() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        setUser("user@example.com");

        String result = controller.showAuthenticatedDashboard("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("dashboard", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    public void showAuthenticatedDashboard_WhenAuthenticatedAndTheUserIsNotAnAdmin() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        Configuration.getInstance().setAdminUsersAndRoles("admin@exmaple.com");
        setUser("user@example.com");

        String result = controller.showAuthenticatedDashboard("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("dashboard", result);
        assertEquals(false, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    public void showAuthenticatedDashboard_WhenAuthenticatedAndTheUserIsAnAdmin() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        Configuration.getInstance().setAdminUsersAndRoles("admin@exmaple.com");
        setUser("admin@example.com");

        String result = controller.showAuthenticatedDashboard("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("dashboard", result);
        assertEquals(false, model.getAttribute("userCanCreateWorkspace"));
    }

}