package com.structurizr.onpremises.web.home;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HomePageControllerTests extends ControllerTestsBase {

    private HomePageController controller;
    private ModelMap model;

    @Before
    public void setUp() {
        controller = new HomePageController();

        model = new ModelMap();
    }

    @Test
    public void showHomePage_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        clearUser();

        String result = controller.showHomePage("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("/share", model.getAttribute("urlPrefix"));
        assertEquals("home", result);
    }

    @Test
    public void showHomePage_WhenAuthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        setUser("user");

        String result = controller.showHomePage("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("/workspace", model.getAttribute("urlPrefix"));
        assertEquals("home", result);
    }

}