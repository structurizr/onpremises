package com.structurizr.onpremises.web.workspace.dsl;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DslControllerTests extends ControllerTestsBase {

    private DslController controller;
    private ModelMap model;
    private String workspaceJson = "";

    @BeforeEach
    public void setUp() {
        controller = new DslController();
        model = new ModelMap();
        Configuration.init();
        clearUser();

        try {
            Workspace workspace = new Workspace("Name", "Description");
            DslUtils.setDsl(workspace, """
            workspace "Name" "Description" {
                ...
            }""");
            workspaceJson = WorkspaceUtils.toJson(workspace, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void showPublicDsl_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showPublicDsl(1, model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicDsl_ReturnsThe404Page_WhenTheWorkspaceIsPrivate() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showPublicDsl(1, model);
        assertEquals("404", view);
    }

    @Test
    public void showPublicDsl_ReturnsTheDsl_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showPublicDsl(1, model);
        assertEquals("plaintext", view);
        assertEquals("""
            workspace "Name" "Description" {
                ...
            }""", model.getAttribute("text"));
    }

    @Test
    public void showSharedDsl_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showSharedDsl(1, "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedDsl_ReturnsThe404Page_WhenTheWorkspaceIsNotShared() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showSharedDsl(1, "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedDsl_ReturnsThe404Page_WhenTheWorkspaceIsSharedAndTheTokenIsIncorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showSharedDsl(1, "token", model);
        assertEquals("404", view);
    }

    @Test
    public void showSharedDsl_ReturnsTheDsl_WhenTheWorkspaceIsSharedAndTheTokenIsCorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("token");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return workspaceJson;
            }
        });

        String view = controller.showSharedDsl(1, "token", model);
        assertEquals("plaintext", view);
        assertEquals("""
            workspace "Name" "Description" {
                ...
            }""", model.getAttribute("text"));
    }

}