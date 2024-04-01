package com.structurizr.onpremises.web.workspace;

import com.structurizr.Workspace;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonControllerTests extends ControllerTestsBase {

    private JsonController controller;
    private ModelMap model;
    private String workspaceJson = "";

    @BeforeEach
    public void setUp() {
        controller = new JsonController();
        model = new ModelMap();
        Configuration.init();
        clearUser();

        try {
            Workspace workspace = new Workspace("Name", "Description");
            workspaceJson = WorkspaceUtils.toJson(workspace, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void showPublicJson_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showPublicJson(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicJson_ReturnsThe404Page_WhenTheWorkspaceIsPrivate() {
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

        String view = controller.showPublicJson(1, "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicJson_ReturnsTheJson_WhenTheWorkspaceHasNoUsersConfigured()  {
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

        String view = controller.showPublicJson(1, "version", model);
        assertEquals("json", view);
        assertEquals("""
                {"configuration":{},"description":"Description","documentation":{},"id":0,"model":{},"name":"Name","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", model.getAttribute("json"));
    }

    @Test
    public void showSharedJson_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showSharedJson(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedJson_ReturnsThe404Page_WhenTheWorkspaceIsNotShared() {
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

        String view = controller.showSharedJson(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedJson_ReturnsThe404Page_WhenTheWorkspaceIsSharedAndTheTokenIsIncorrect() {
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

        String view = controller.showSharedJson(1, "version", "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedJson_ReturnsTheJson_WhenTheWorkspaceIsSharedAndTheTokenIsCorrect() {
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

        String view = controller.showSharedJson(1, "version", "token", model);
        assertEquals("json", view);
        assertEquals("""
                {"configuration":{},"description":"Description","documentation":{},"id":0,"model":{},"name":"Name","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", model.getAttribute("json"));
    }

}