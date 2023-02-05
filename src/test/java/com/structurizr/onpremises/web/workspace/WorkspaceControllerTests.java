package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.MockHttpServletRequest;
import com.structurizr.onpremises.web.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.ui.ModelMap;

import java.util.HashSet;

import static org.junit.Assert.*;

public class WorkspaceControllerTests {

    private WorkspaceController controller;
    private MockWorkspaceComponent workspaceComponent = new MockWorkspaceComponent();
    private MockHttpServletResponse response;
    private ModelMap model;
    private User user;

    @Before
    public void setUp() {
        controller = new WorkspaceController();
        controller.setWorkspaceComponent(workspaceComponent);
        response = new MockHttpServletResponse();
        model = new ModelMap();
        user = new User("help@structurizr.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        System.setProperty("structurizr.dataDirectory", ".");
        Configuration.init();
    }

    @Test
    public void test_viewDslEditor_ReturnsTheDslEditorPage_WhenTheWorkspaceIsPublic() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(), AuthenticationMethod.LOCAL);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);

        long workspaceId = workspaceComponent.createWorkspace(user);

        String view = controller.viewDslEditor(workspaceId, null, model);
        assertEquals("dsl-editor", view);
        assertTrue(((WorkspaceMetaData)model.get("workspace")).isEditable());
    }

    @Test
    public void test_viewDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheWorkspaceIsPublic() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(), AuthenticationMethod.LOCAL);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);

        long workspaceId = workspaceComponent.createWorkspace(user);

        String view = controller.viewDiagramEditor(workspaceId, null, model);
        assertEquals("diagram-editor", view);
        assertTrue(((WorkspaceMetaData)model.get("workspace")).isEditable());
    }

    @Test
    public void test_viewDslEditor_ReturnsAnErrorMessage_WhenTheWorkspaceIsNotPublicAndTheUserHasReadOnlyAccess() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(), null);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addReadUser("user");

        String view = controller.viewDslEditor(workspaceId, null, model);
        assertEquals("workspace-is-readonly", view);
    }

    @Test
    public void test_viewDiagramEditor_ReturnsAnErrorMessage_WhenTheWorkspaceIsNotPublicAndTheUserHasReadOnlyAccess() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(), null);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addReadUser("user");

        String view = controller.viewDiagramEditor(workspaceId, null, model);
        assertEquals("workspace-is-readonly", view);
    }

    @Test
    public void test_viewDiagramEditor_ReturnsTheDiagramsPage_WhenTheWorkspaceIsNotPublicButTheUserHasReadWriteAccess() throws Exception {
        controller = new WorkspaceController();
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addWriteUser("user");

        String view = controller.viewDiagramEditor(workspaceId, null, model);
        assertEquals("diagram-editor", view);
        assertTrue(((WorkspaceMetaData)model.get("workspace")).isEditable());
    }

    @Test
    public void test_viewDiagramEditor_ReturnsTheDiagramsPage_WhenTheWorkspaceIsNotPublicButTheUserRoleHasReadWriteAccess() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(Collections.singletonList("role_structurizr_writer")), AuthenticationMethod.LOCAL);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addWriteUser("role_structurizr_writer");

        String view = controller.viewDiagramEditor(workspaceId, null, model);
        assertEquals("diagram-editor", view);
        assertTrue(((WorkspaceMetaData)model.get("workspace")).isEditable());
    }

    @Test
    public void test_getImage_ReturnsThe404Page_WhenTheUserDoesNotHaveAccessToTheWorkspace() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(Collections.singletonList("role_structurizr_writer")), AuthenticationMethod.LOCAL);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addReadUser("someOtherUser");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Resource resource = controller.getImage(workspaceId, "SystemContext.png", null, response);

        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void test_getImage_ReturnsTheImage_WhenTheUserHasReadAccessToTheWorkspace() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(Collections.singletonList("role_structurizr_writer")), AuthenticationMethod.LOCAL);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addReadUser("user");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Resource resource = controller.getImage(workspaceId, "SystemContext.png", null, response);

        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void test_getImage_ReturnsTheImage_WhenTheUserHasWriteAccessToTheWorkspace() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(Collections.singletonList("role_structurizr_writer")), AuthenticationMethod.LOCAL);
//            }
//        };
        controller.setWorkspaceComponent(workspaceComponent);
        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addWriteUser("user");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Resource resource = controller.getImage(workspaceId, "SystemContext.png", null, response);

        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void test_getImage_ReturnsTheThumbnailNotFoundImage_WhenTheImageDoesNotExist() throws Exception {
        controller = new WorkspaceController();
//        {
//            @Override
//            protected User getUser() {
//                return new User("user", new HashSet<>(Collections.singletonList("role_structurizr_writer")), AuthenticationMethod.LOCAL);
//            }
//        };
        workspaceComponent = new MockWorkspaceComponent() {
            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                throw new WorkspaceComponentException("Image not found");
            }
        };
        controller.setWorkspaceComponent(workspaceComponent);

        long workspaceId = workspaceComponent.createWorkspace(user);
        workspaceComponent.getWorkspaceMetaData(workspaceId).addWriteUser("user");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Resource resource = controller.getImage(workspaceId, "SystemContext.png", request, response);

        assertNull(resource);
        assertEquals(404, response.getStatus());
    }

}