package com.structurizr.onpremises.web.workspace;

import com.structurizr.Workspace;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.Shape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThemeControllerTests extends ControllerTestsBase {

    private ThemeController controller;
    private ModelMap model;
    private String workspaceJson = "";

    @BeforeEach
    public void setUp() {
        controller = new ThemeController();
        model = new ModelMap();
        Configuration.init();
        clearUser();

        try {
            Workspace workspace = new Workspace("Name", "Description");
            workspace.getViews().getConfiguration().getStyles().addElementStyle("Person").shape(Shape.Person);
            workspaceJson = WorkspaceUtils.toJson(workspace, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void showPublicTheme_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showPublicTheme(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicTheme_ReturnsThe404Page_WhenTheWorkspaceIsPrivate() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showPublicTheme(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicTheme_ReturnsTheThemePage_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showPublicTheme(1, "version", model);
        assertEquals("json", view);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Person",
                    "shape" : "Person"
                  } ]
                }""", model.getAttribute("json"));
    }

    @Test
    public void showSharedTheme_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showSharedTheme(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedTheme_ReturnsThe404Page_WhenTheWorkspaceIsNotShared() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showSharedTheme(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedTheme_ReturnsThe404Page_WhenTheWorkspaceIsSharedAndTheTokenIsIncorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showSharedTheme(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedTheme_ReturnsTheThemePage_WhenTheWorkspaceIsSharedAndTheTokenIsCorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("token");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showSharedTheme(1, "version", "token", model);
        assertEquals("json", view);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Person",
                    "shape" : "Person"
                  } ]
                }""", model.getAttribute("json"));
    }

    @Test
    public void showAuthenticatedTheme_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedTheme(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedTheme_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedTheme(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedTheme_ReturnsTheThemePage_WhenTheWorkspaceIsPublic()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedTheme(1, "version", model);
        assertEquals("json", view);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Person",
                    "shape" : "Person"
                  } ]
                }""", model.getAttribute("json"));
    }

    @Test
    public void showAuthenticatedTheme_ReturnsTheThemePage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedTheme(1, "version", model);
        assertEquals("json", view);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Person",
                    "shape" : "Person"
                  } ]
                }""", model.getAttribute("json"));
    }

    @Test
    public void showAuthenticatedTheme_ReturnsTheThemePage_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedTheme(1, "version", model);
        assertEquals("json", view);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Person",
                    "shape" : "Person"
                  } ]
                }""", model.getAttribute("json"));
    }

}