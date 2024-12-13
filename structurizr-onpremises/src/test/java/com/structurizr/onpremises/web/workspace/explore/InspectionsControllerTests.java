package com.structurizr.onpremises.web.workspace.explore;

import com.structurizr.Workspace;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class InspectionsControllerTests extends ControllerTestsBase {

    private InspectionsController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new InspectionsController();
        model = new ModelMap();
        Configuration.init();
        clearUser();
    }

    @Test
    public void showAuthenticatedInspectionsPage_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showInspections(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedInspections_ReturnsAnErrorPage_WhenTheWorkspaceIsClientSideEncrypted() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setClientSideEncrypted(true);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.showInspections(1, "main", "version", model);
        assertEquals("workspace-is-client-side-encrypted", view);
    }

    @Test
    public void showAuthenticatedInspections_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.showInspections(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedInspections_ReturnsTheInspectionsPage_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                try {
                    return WorkspaceUtils.toJson(new Workspace("Name", "Description"), false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e.getMessage());
                }
            }
        });

        setUser("user@example.com");
        String view = controller.showInspections(1, "main", "version", model);
        assertEquals("inspections", view);
        assertNotNull(model.getAttribute("violations"));
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable());
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
    }

    @Test
    public void showAuthenticatedInspections_ReturnsTheInspectionsPage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                try {
                    return WorkspaceUtils.toJson(new Workspace("Name", "Description"), false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e.getMessage());
                }
            }
        });

        setUser("user1@example.com");
        String view = controller.showInspections(1, "main", "version", model);
        assertEquals("inspections", view);
        assertNotNull(model.getAttribute("violations"));
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable());
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
    }

    @Test
    public void showAuthenticatedInspections_ReturnsTheInspectionsPage_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                try {
                    return WorkspaceUtils.toJson(new Workspace("Name", "Description"), false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e.getMessage());
                }
            }
        });

        setUser("user1@example.com");
        String view = controller.showInspections(1, "main", "version", model);
        assertEquals("inspections", view);
        assertNotNull(model.getAttribute("violations"));
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable());
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
    }

}