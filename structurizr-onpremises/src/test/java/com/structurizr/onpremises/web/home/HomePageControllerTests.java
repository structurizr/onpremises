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
    public void show_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        clearUser();

        String result = controller.show("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("/share", model.getAttribute("urlPrefix"));
        assertEquals("home", result);
    }

    @Test
    public void show_WhenAuthenticated() {
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
        assertEquals("/share", model.getAttribute("urlPrefix"));
        assertEquals("home", result);
    }
    
}