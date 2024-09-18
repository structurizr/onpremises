package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateWorkspaceControllerTests extends ControllerTestsBase {

    private CreateWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new CreateWorkspaceController();
        model = new ModelMap();
        clearUser();
        Configuration.init();
    }

    @Test
    public void createWorkspace_CreatesAWorkspace_WhenNoAdminUsersAreDefined() {
        Configuration.getInstance().setAdminUsersAndRoles();
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 1;
            }
        });
        String result = controller.createWorkspace(model);

        assertEquals("redirect:/workspace/1", result);
    }

    @Test
    public void createWorkspace_CreatesAWorkspace_WhenAnAdminUserIsDefined() {
        Configuration.getInstance().setAdminUsersAndRoles("admin@example.com");
        setUser("admin@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 1;
            }
        });
        String result = controller.createWorkspace(model);

        assertEquals("redirect:/workspace/1", result);
    }

    @Test
    public void createWorkspace_ReturnsThe404Page_WhenTheUserIsNotAnAdmin() {
        Configuration.getInstance().setAdminUsersAndRoles("admin@example.com");
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});
        String result = controller.createWorkspace(model);

        assertEquals("404", result);
    }

}