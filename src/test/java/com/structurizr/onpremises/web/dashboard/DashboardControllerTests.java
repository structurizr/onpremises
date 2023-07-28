package com.structurizr.onpremises.web.dashboard;

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

public class DashboardControllerTests extends ControllerTestsBase {

    private DashboardController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DashboardController();

        model = new ModelMap();
        Configuration.init();
    }

    @Test
    public void show_WhenAuthenticatedAndNoAdminUsersHaveBeenDefined() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        setUser("user@example.com");

        String result = controller.show("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("/workspace", model.getAttribute("urlPrefix"));
        assertEquals("dashboard", result);
        assertEquals(true, model.getAttribute("showAdminFeatures"));
    }

    @Test
    public void show_WhenAuthenticatedAndTheUserIsNotAnAdmin() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        Configuration.getInstance().setAdminUsersAndRoles("admin@exmaple.com");
        setUser("user@example.com");

        String result = controller.show("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("/workspace", model.getAttribute("urlPrefix"));
        assertEquals("dashboard", result);
        assertEquals(false, model.getAttribute("showAdminFeatures"));
    }

    @Test
    public void show_WhenAuthenticatedAndTheUserIsAnAdmin() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        Configuration.getInstance().setAdminUsersAndRoles("admin@exmaple.com");
        setUser("admin@example.com");

        String result = controller.show("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("/workspace", model.getAttribute("urlPrefix"));
        assertEquals("dashboard", result);
        assertEquals(false, model.getAttribute("showAdminFeatures"));
    }

}