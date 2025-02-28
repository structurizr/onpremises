package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockSearchComponent;
import com.structurizr.onpremises.web.MockWorkspaceComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteWorkspaceControllerTests extends ControllerTestsBase {

    private DeleteWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DeleteWorkspaceController();
        model = new ModelMap();
        Configuration.init();
        setUser("user@example.com");
    }

    @Test
    public void deleteWorkspace_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    public void deleteWorkspace_RedirectsToTheDashboard_WhenUserIsNotAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(properties);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                fail();
                return true;
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
    }

    @Test
    public void deleteWorkspace_DeletesTheWorkspace_WhenNoAdminUsersAreDefined() {
        Configuration.init();

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                buf.append("1 ");
                return true;
            }
        });

        controller.setSearchComponent(new MockSearchComponent() {
            @Override
            public void delete(long workspaceId) {
                buf.append("2");
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
        assertEquals("1 2", buf.toString());
    }

    @Test
    public void deleteWorkspace_DeletesTheWorkspace_WhenTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "user@example.com");
        Configuration.init(properties);

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                buf.append("1 ");
                return true;
            }
        });

        controller.setSearchComponent(new MockSearchComponent() {
            @Override
            public void delete(long workspaceId) {
                buf.append("2");
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
        assertEquals("1 2", buf.toString());
    }

}