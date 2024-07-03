package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ModelControllerTests extends ControllerTestsBase {

    private ModelController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new ModelController();
        model = new ModelMap();
        Configuration.init();
        clearUser();
    }

    @Test
    public void showPublicModel_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showPublicModel(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicModel_ReturnsThe404Page_WhenTheWorkspaceIsPrivate() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showPublicModel(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicModel_ReturnsTheModelPage_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showPublicModel(1, "version", model);
        assertEquals("model", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/share/1", model.getAttribute("urlPrefix"));
        assertEquals("view", model.getAttribute("view"));
    }

    @Test
    public void showSharedModel_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showSharedModel(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedModel_ReturnsThe404Page_WhenTheWorkspaceIsNotShared() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showSharedModel(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedModel_ReturnsThe404Page_WhenTheWorkspaceIsSharedAndTheTokenIsIncorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showSharedModel(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedModel_ReturnsTheModelPage_WhenTheWorkspaceIsSharedAndTheTokenIsCorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("token");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showSharedModel(1, "version", "token", model);
        assertEquals("model", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/share/1/token", model.getAttribute("urlPrefix"));
        assertEquals("view", model.getAttribute("view"));
    }

    @Test
    public void showAuthenticatedModel_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedModel(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedModel_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedModel(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedModel_ReturnsTheModelPage_WhenTheWorkspaceIsPublic()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedModel(1, "version", model);
        assertEquals("model", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("view", model.getAttribute("view"));
    }

    @Test
    public void showAuthenticatedModel_ReturnsTheModelPage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedModel(1, "version", model);
        assertEquals("model", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("view", model.getAttribute("view"));
    }

    @Test
    public void showAuthenticatedModel_ReturnsTheModelPage_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedModel(1, "version", model);
        assertEquals("model", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("view", model.getAttribute("view"));
    }

}