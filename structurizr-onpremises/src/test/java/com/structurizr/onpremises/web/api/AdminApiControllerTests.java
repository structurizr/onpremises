package com.structurizr.onpremises.web.api;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AdminApiControllerTests {

    private AdminApiController controller;

    @BeforeEach
    public void setUp() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Configuration.init();
        Configuration.getInstance().setApiKey(encoder.encode("1234567890"));

        controller = new AdminApiController(encoder);
    }

    @Test
    public void getWorkspaces_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        try {
            controller.getWorkspaces(null);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }

        try {
            controller.getWorkspaces("");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }

        try {
            controller.getWorkspaces(" ");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    public void getWorkspaces_ReturnsAnApiError_WhenNoApiKeyIsConfigured() {
        try {
            Configuration.getInstance().setApiKey(null);
            controller.getWorkspaces("1234567890");
            fail();
        } catch (ApiException e) {
            assertEquals("The API key is not configured for this installation - please refer to the documentation", e.getMessage());
        }
    }

    @Test
    public void getWorkspaces_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() {
        try {
            controller.getWorkspaces("0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    public void getWorkspaces() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public Collection<WorkspaceMetaData> getWorkspaces() {
                WorkspaceMetaData wmd1 = new WorkspaceMetaData(1);
                wmd1.setName("Workspace 1");
                wmd1.setApiKey("key1");
                wmd1.setApiSecret("secret1");

                WorkspaceMetaData wmd2 = new WorkspaceMetaData(2);
                wmd2.setName("Workspace 2");
                wmd2.setApiKey("key2");
                wmd2.setApiSecret("secret2");

                return List.of(wmd1, wmd2);
            }
        });

        WorkspacesApiResponse response = controller.getWorkspaces("1234567890");
        assertEquals(2, response.getWorkspaces().size());

        WorkspaceApiResponse war1 = response.getWorkspaces().get(0);
        assertEquals("Workspace 1", war1.getName());
        assertEquals("key1", war1.getApiKey());
        assertEquals("secret1", war1.getApiSecret());

        WorkspaceApiResponse war2 = response.getWorkspaces().get(1);
        assertEquals("Workspace 2", war2.getName());
        assertEquals("key2", war2.getApiKey());
        assertEquals("secret2", war2.getApiSecret());
    }

    @Test
    public void createWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        try {
            controller.createWorkspace(null);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }

        try {
            controller.createWorkspace("");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }

        try {
            controller.createWorkspace(" ");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    public void createWorkspace_ReturnsAnApiError_WhenNoApiKeyIsConfigured() {
        try {
            Configuration.getInstance().setApiKey(null);
            controller.createWorkspace("1234567890");
            fail();
        } catch (ApiException e) {
            assertEquals("The API key is not configured for this installation - please refer to the documentation", e.getMessage());
        }
    }

    @Test
    public void createWorkspace_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() {
        try {
            controller.createWorkspace("0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    public void createWorkspace() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 123456;
            }

            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(workspaceId);
                wmd.setName("Workspace " + workspaceId);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");
                return wmd;
            }
        });

        WorkspaceApiResponse response = controller.createWorkspace("1234567890");
        assertEquals(123456, response.getId());
        assertEquals("Workspace 123456", response.getName());
        assertEquals("key", response.getApiKey());
        assertEquals("secret", response.getApiSecret());
    }

    @Test
    public void deleteWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        try {
            controller.deleteWorkspace(null, 1);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }

        try {
            controller.deleteWorkspace("", 1);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }

        try {
            controller.deleteWorkspace(" ", 1);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    public void deleteWorkspace_ReturnsAnApiError_WhenNoApiKeyIsConfigured() {
        try {
            Configuration.getInstance().setApiKey(null);
            controller.deleteWorkspace("1234567890", 1);
            fail();
        } catch (ApiException e) {
            assertEquals("The API key is not configured for this installation - please refer to the documentation", e.getMessage());
        }
    }

    @Test
    public void deleteWorkspace_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() {
        try {
            controller.deleteWorkspace("0987654321", 1);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    public void deleteWorkspace_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.deleteWorkspace("1234567890", -1);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    public void deleteWorkspace() {
        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                buf.append(workspaceId);
                return true;
            }
        });

        controller.deleteWorkspace("1234567890", 1);
        assertEquals("1", buf.toString());
    }

}