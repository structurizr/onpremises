package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockHttpServletResponse;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ImageControllerTests extends ControllerTestsBase {

    private ImageController controller;
    private MockHttpServletResponse response;
    private static final InputStreamAndContentLength IMAGE = new InputStreamAndContentLength(new ByteArrayInputStream(new byte[1234]), 1234);

    @BeforeEach
    public void setUp() {
        controller = new ImageController();
        response = new MockHttpServletResponse();
        clearUser();
    }

    @Test
    public void getPublicImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        Resource resource = controller.getPublicImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }
    
    @Test
    public void getPublicImage_ReturnsThe404Page_WhenTheWorkspaceIsNotPublic() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        Resource resource = controller.getPublicImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getPublicImage_ReturnsA404_WhenTheImageDoesNotExist() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return null;
            }
        });

        Resource resource = controller.getPublicImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getPublicImage_ReturnsTheImage_WhenTheWorkspaceHasNoUsersConfigured() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return IMAGE;
            }
        });

        Resource resource = controller.getPublicImage(1, "thumbnail.png", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void getSharedImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        Resource resource = controller.getSharedImage(1, "thumbnail.png", "token", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getSharedImage_ReturnsThe404Page_WhenTheWorkspaceIsNotPublic() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        Resource resource = controller.getSharedImage(1, "thumbnail.png", "token", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getSharedImage_ReturnsThe404Page_WhenTheWorkspaceIsNotShared() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        Resource resource = controller.getSharedImage(1, "thumbnail.png", "token", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getSharedImage_ReturnsThe404Page_WhenTheSharingTokenIsIncorrect() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("0987654321");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        Resource resource = controller.getSharedImage(1, "thumbnail.png", "1234567890", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getSharedImage_ReturnsA404_WhenTheImageDoesNotExist() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return null;
            }
        });

        Resource resource = controller.getSharedImage(1, "thumbnail.png", "1234567890", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getSharedImage_ReturnsTheImage_WhenTheSharingTokenIsCorrect() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setSharingToken("1234567890");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return IMAGE;
            }
        });

        Resource resource = controller.getSharedImage(1, "thumbnail.png", "1234567890", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void getAuthenticatedImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getAuthenticatedImage_ReturnsA404_WhenTheUserDoesNotHaveAccessToTheWorkspace() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    public void getAuthenticatedImage_ReturnsTheImage_WhenTheUserHasReadAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return IMAGE;
            }
        });

        setUser("user@example.com");
        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void getAuthenticatedImage_ReturnsTheImage_WhenTheUserHasWriteAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return IMAGE;
            }
        });

        setUser("user@example.com");
        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void getAuthenticatedImage_ReturnsA404_WhenTheImageDoesNotExist() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

}
