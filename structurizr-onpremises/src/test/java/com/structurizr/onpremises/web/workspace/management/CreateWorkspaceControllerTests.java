package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

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
        Configuration.init();
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
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(properties);
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
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(properties);
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});
        String result = controller.createWorkspace(model);

        assertEquals("404", result);
    }

}