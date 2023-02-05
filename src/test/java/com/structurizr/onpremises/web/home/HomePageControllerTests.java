package com.structurizr.onpremises.web.home;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

public class HomePageControllerTests extends ControllerTestsBase {

    private HomePageController controller;
    private MockWorkspaceComponent workspaceComponent;
    private ModelMap model;

    @Before
    public void setUp() {
        controller = new HomePageController();

        model = new ModelMap();
    }

    @Test
    public void showHomePage_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace
        workspace1.addWriteUser("user");
        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // open workspace

        workspaceComponent = new com.structurizr.onpremises.web.MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                return Arrays.asList(
                        workspace1, workspace2
                );
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);
        clearUser();

        String result = controller.showHomePage("", model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertFalse(((Collection)model.getAttribute("workspaces")).contains(workspace1)); // private workspace
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace2)); // open workspace
        assertEquals("/share", model.getAttribute("urlPrefix"));
        assertEquals("home", result);
    }

    @Test
    public void showHomePage_WhenAuthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace
        workspace1.addWriteUser("user");
        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // open workspace

        workspaceComponent = new com.structurizr.onpremises.web.MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                return Arrays.asList(
                        workspace1, workspace2
                );
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);
        setUser("user");

        String result = controller.showHomePage("", model);

        assertEquals(2, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1)); // private workspace
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace2)); // open workspace
        assertEquals("/workspace", model.getAttribute("urlPrefix"));
        assertEquals("home", result);
    }

}