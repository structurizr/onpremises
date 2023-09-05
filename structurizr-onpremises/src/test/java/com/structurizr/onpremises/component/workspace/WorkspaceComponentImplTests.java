package com.structurizr.onpremises.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.configuration.Role;
import com.structurizr.configuration.Visibility;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.encryption.EncryptionLocation;
import com.structurizr.encryption.EncryptionStrategy;
import com.structurizr.io.json.EncryptedJsonWriter;
import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.DateUtils;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceComponentImplTests {

    @BeforeEach
    public void setUp() {
        Configuration.init();
    }

    @Test
    public void getWorkspaces_WhenThereAreNoWorkspaces() {
        Collection<WorkspaceMetaData> workspaces = new ArrayList<>();

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>();
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        assertTrue(workspaceComponent.getWorkspaces().isEmpty());
    }

    @Test
    public void getWorkspaces_WhenUnauthenticated() {
        Map<Long, WorkspaceMetaData> workspaceMap = new HashMap<>();

        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace
        workspace1.addWriteUser("user1");
        workspaceMap.put(workspace1.getId(), workspace1);

        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // private workspace
        workspace2.addWriteUser("user2");
        workspaceMap.put(workspace2.getId(), workspace2);

        WorkspaceMetaData workspace3 = new WorkspaceMetaData(3); // open workspace
        workspaceMap.put(workspace3.getId(), workspace3);

        WorkspaceMetaData workspace4 = new WorkspaceMetaData(4); // public workspace
        workspace4.addWriteUser("user1");
        workspace4.setPublicWorkspace(true);
        workspaceMap.put(workspace4.getId(), workspace4);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>(workspaceMap.keySet());
            }

            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMap.get(workspaceId);
            }
        };

        WorkspaceComponentImpl workspaceComponent = new WorkspaceComponentImpl(dao, "");
        Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces(null);

        assertEquals(2, workspaces.size());

        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace

        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 2)); // private workspace

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 3)); // open workspace
        assertEquals("/share", workspaces.stream().filter(w -> w.getId() == 3).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 4)); // public workspace
        assertEquals("/share", workspaces.stream().filter(w -> w.getId() == 4).findFirst().get().getUrlPrefix());
    }

    @Test
    public void getWorkspaces_WhenAuthenticated() {
        Map<Long, WorkspaceMetaData> workspaceMap = new HashMap<>();

        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1); // private workspace, read/write access
        workspace1.addWriteUser("user1");
        workspaceMap.put(workspace1.getId(), workspace1);

        WorkspaceMetaData workspace2 = new WorkspaceMetaData(2); // private workspace, read-only access
        workspace2.addWriteUser("user2");
        workspace2.addReadUser("user1");
        workspaceMap.put(workspace2.getId(), workspace2);

        WorkspaceMetaData workspace3 = new WorkspaceMetaData(3); // open workspace
        workspaceMap.put(workspace3.getId(), workspace3);

        WorkspaceMetaData workspace4 = new WorkspaceMetaData(4); // private workspace, no access
        workspace4.addWriteUser("user4");
        workspaceMap.put(workspace4.getId(), workspace4);

        WorkspaceMetaData workspace5 = new WorkspaceMetaData(5); // public workspace, read/write access
        workspace5.addWriteUser("user1");
        workspace5.setPublicWorkspace(true);
        workspaceMap.put(workspace5.getId(), workspace5);

        WorkspaceMetaData workspace6 = new WorkspaceMetaData(6); // public workspace, no role-based access
        workspace6.addWriteUser("user6");
        workspace6.setPublicWorkspace(true);
        workspaceMap.put(workspace6.getId(), workspace6);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>(workspaceMap.keySet());
            }

            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMap.get(workspaceId);
            }
        };

        WorkspaceComponentImpl workspaceComponent = new WorkspaceComponentImpl(dao, "");
        User user = new User("user1", new HashSet<>(), AuthenticationMethod.LOCAL);
        Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces(user);

        assertEquals(5, workspaces.size());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace, read/write access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 1).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 2)); // private workspace, read-only access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 2).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 3)); // open workspace
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 3).findFirst().get().getUrlPrefix());

        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 4)); // private workspace, no access

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 5)); // public workspace, read/write access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 5).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 6)); // public workspace, no role-based access
        assertEquals("/share", workspaces.stream().filter(w -> w.getId() == 6).findFirst().get().getUrlPrefix());
    }

    @Test
    public void createWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        final StringBuffer jsonBuffer = new StringBuffer();
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public long createWorkspace(User user) {
                return 1;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {
                jsonBuffer.append(json);
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        long workspaceId = workspaceComponent.createWorkspace(null);

        assertEquals(1, workspaceId);
        assertEquals(String.format("{\"id\":1,\"name\":\"Workspace 1\",\"description\":\"Description\",\"revision\":1,\"lastModifiedDate\":\"%s\",\"model\":{},\"documentation\":{},\"views\":{\"configuration\":{\"branding\":{},\"styles\":{},\"terminology\":{}}}}", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate())), jsonBuffer.toString());
    }

    @Test
    public void deleteWorkspace() {
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public boolean deleteWorkspace(long workspaceId) {
                return true;
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        assertTrue(workspaceComponent.deleteWorkspace(1));
    }

    @Test
    public void getWorkspace_WhenServerSideEncryptionIsNotEnabled() {
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public String getWorkspace(long workspaceId, String version) {
                return "json";
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        String json = workspaceComponent.getWorkspace(1, "");
        assertEquals("json", json);
    }

    @Test
    public void getWorkspace_WhenServerSideEncryptionIsEnabled() {
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public String getWorkspace(long workspaceId, String version) {
                String json = "";

                try {
                    Workspace workspace = new Workspace("Name", "Description");
                    workspace.setId(1);
                    EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy("password");
                    encryptionStrategy.setLocation(EncryptionLocation.Server);
                    EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
                    EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
                    StringWriter stringWriter = new StringWriter();
                    encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
                    json = stringWriter.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return json;
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "password");
        String json = workspaceComponent.getWorkspace(1, "");
        assertEquals("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"model\":{},\"documentation\":{},\"views\":{\"configuration\":{\"branding\":{},\"styles\":{},\"terminology\":{}}}}", json);
    }

    @Test
    public void getWorkspace_WhenClientSideEncryptionIsEnabled() {
        try {
            Workspace workspace = new Workspace("Name", "Description");
            workspace.setId(1);
            EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy("password");
            encryptionStrategy.setLocation(EncryptionLocation.Client);
            EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
            EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
            StringWriter stringWriter = new StringWriter();
            encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
            final String json = stringWriter.toString();

            WorkspaceDao dao = new MockWorkspaceDao() {
                @Override
                public String getWorkspace(long workspaceId, String version) {
                    return json;
                }
            };

            WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
            assertEquals(json, workspaceComponent.getWorkspace(1, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putWorkspace_WhenServerSideEncryptionIsNotEnabled() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        final StringBuffer jsonBuffer = new StringBuffer();
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {
                jsonBuffer.append(json);
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);
        assertEquals(String.format("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"revision\":1,\"lastModifiedDate\":\"%s\",\"model\":{},\"documentation\":{},\"views\":{\"configuration\":{\"branding\":{},\"styles\":{},\"terminology\":{}}}}", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate())), jsonBuffer.toString());

        // and again, to increment the revision
        json = jsonBuffer.toString();
        jsonBuffer.setLength(0);
        workspaceComponent.putWorkspace(1, json);
        assertEquals(String.format("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"revision\":2,\"lastModifiedDate\":\"%s\",\"model\":{},\"documentation\":{},\"views\":{\"configuration\":{\"branding\":{},\"styles\":{},\"terminology\":{}}}}", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate())), jsonBuffer.toString());
    }

    @Test
    public void putWorkspace_WhenServerSideEncryptionIsEnabled() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        final StringBuffer jsonBuffer = new StringBuffer();
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {
                jsonBuffer.append(json);
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "password");
        workspaceComponent.putWorkspace(1, json);
        assertTrue(jsonBuffer.toString().startsWith(String.format("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"revision\":1,\"lastModifiedDate\":\"%s\",\"ciphertext\"", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));

        // and again, to increment the revision
        json = jsonBuffer.toString();
        jsonBuffer.setLength(0);
        workspaceComponent.putWorkspace(1, json);
        System.out.println(jsonBuffer);
        assertTrue(jsonBuffer.toString().startsWith(String.format("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"revision\":2,\"lastModifiedDate\":\"%s\",\"ciphertext\"", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));
    }

    @Test
    public void putWorkspace_WhenClientSideEncryptionIsEnabled() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy("passphrase");
        EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
        EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
        StringWriter stringWriter = new StringWriter();
        encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
        String json = stringWriter.toString();

        final StringBuffer jsonBuffer = new StringBuffer();
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json) {
                jsonBuffer.append(json);
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);
        assertTrue(jsonBuffer.toString().startsWith(String.format("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"revision\":1,\"lastModifiedDate\":\"%s\",\"ciphertext\"", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));
        assertTrue(jsonBuffer.toString().endsWith((json.substring(json.indexOf("ciphertext")))));

        // and again, to increment the revision
        json = jsonBuffer.toString();
        jsonBuffer.setLength(0);
        workspaceComponent.putWorkspace(1, json);
        assertTrue(jsonBuffer.toString().startsWith(String.format("{\"id\":1,\"name\":\"Name\",\"description\":\"Description\",\"revision\":2,\"lastModifiedDate\":\"%s\",\"ciphertext\"", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));
        assertTrue(jsonBuffer.toString().endsWith((json.substring(json.indexOf("ciphertext")))));
    }

    @Test
    public void test_putWorkspace_UpdatesTheVisibility_WhenTheVisibilityIsSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().setVisibility(Visibility.Public);

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setPublicWorkspace(false);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                wmd.setPublicWorkspace(workspaceMetaData.isPublicWorkspace());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);

        assertTrue(wmd.isPublicWorkspace());
    }

    @Test
    public void test_putWorkspace_DoesNotUpdateTheVisibility_WhenTheVisibilityIsNotSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setPublicWorkspace(false);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                wmd.setPublicWorkspace(workspaceMetaData.isPublicWorkspace());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);

        assertFalse(wmd.isPublicWorkspace());
    }

    @Test
    public void test_putWorkspace_UpdatesTheRoleBasedSecurity_WhenUsersAreDefined() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().addUser("user1@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user2@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user3@example.com", Role.ReadOnly);
        workspace.getConfiguration().addUser("user4@example.com", Role.ReadOnly);

        String json = WorkspaceUtils.toJson(workspace, false);

        final Set<String> readUsers = new HashSet<>();
        Set<String> writeUsers = new HashSet<>();
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                readUsers.addAll(workspaceMetaData.getReadUsers());
                writeUsers.addAll(workspaceMetaData.getWriteUsers());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);

        assertEquals(2, writeUsers.size());
        assertTrue(writeUsers.contains("user1@example.com"));
        assertTrue(writeUsers.contains("user2@example.com"));
        assertEquals(2, readUsers.size());
        assertTrue(readUsers.contains("user3@example.com"));
        assertTrue(readUsers.contains("user4@example.com"));
    }

    @Test
    public void putWorkspace_DoesNotUpdateTheRoleBasedSecurity_WhenUsersAreNotDefined() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().addUser("user1@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user2@example.com", Role.ReadOnly);

        String json = WorkspaceUtils.toJson(workspace, false);

        final Set<String> readUsers = new HashSet<>();
        Set<String> writeUsers = new HashSet<>();
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                readUsers.addAll(workspaceMetaData.getReadUsers());
                writeUsers.addAll(workspaceMetaData.getWriteUsers());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);

        assertEquals(1, writeUsers.size());
        assertTrue(writeUsers.contains("user1@example.com"));
        assertEquals(1, readUsers.size());
        assertTrue(readUsers.contains("user2@example.com"));

        // and update the workspace again, this time without users
        workspace = new Workspace("Name", "Description");
        json = WorkspaceUtils.toJson(workspace, false);
        workspaceComponent.putWorkspace(1, json);

        // check that existing users have not disappeared
        assertEquals(1, writeUsers.size());
        assertTrue(writeUsers.contains("user1@example.com"));
        assertEquals(1, readUsers.size());
        assertTrue(readUsers.contains("user2@example.com"));
    }

    @Test
    public void putWorkspace_WithWorkspaceEventListener() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }
        };

        StringBuilder buf = new StringBuilder();
        Configuration.getInstance().setWorkspaceEventListener(new WorkspaceEventListener() {
            @Override
            public void beforeSave(WorkspaceEvent event) {
                buf.append("beforeSave:" + event.getWorkspaceProperties().getId() + ":" + event.getJson());
            }
        });

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, json);
        assertEquals("beforeSave:1:" + json, buf.toString());
    }

    @Test
    public void lockWorkspace_LocksTheWorkspace_WhenItIsNotLocked() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        boolean locked = workspaceComponent.lockWorkspace(1, "user1", "agent");
        assertTrue(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    public void lockWorkspace_LocksTheWorkspace_WhenItIsAlreadyLockedByTheSameUser() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(1);
                wmd.setLockedUser("user1");
                wmd.setLockedAgent("agent");
                wmd.setLockedDate(new Date());

                return wmd;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        boolean locked = workspaceComponent.lockWorkspace(1, "user1", "agent");
        assertTrue(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    public void lockWorkspace_DoesNotLockTheWorkspace_WhenItIsAlreadyLocked() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(new Date());

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        boolean locked = workspaceComponent.lockWorkspace(1, "user2", "agent");
        assertFalse(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    public void lockWorkspace_LocksTheWorkspace_WhenThePreviousLockHasExpired() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(10));

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        boolean locked = workspaceComponent.lockWorkspace(1, "user2", "agent");
        assertTrue(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user2", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    public void unlockWorkspace_UnlocksTheWorkspace_WhenItIsAlreadyLocked() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(10));

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        boolean unlocked = workspaceComponent.unlockWorkspace(1);
        assertTrue(unlocked);

        assertFalse(workspaceMetaData.isLocked());
        assertNull(workspaceMetaData.getLockedUser());
        assertNull(workspaceMetaData.getLockedAgent());
        assertNull(workspaceMetaData.getLockedDate());
    }

    @Test
    public void putWorkspace_UpdatesTheWorkspace_WhenTheCorrectRevisionIsSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");

        final WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setRevision(1);
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return wmd;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                wmd.setRevision(workspaceMetaData.getRevision());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, WorkspaceUtils.toJson(workspace, false));
        assertEquals(2, wmd.getRevision());

        // and again, to increment the revision
        workspace.setRevision(2L);
        workspaceComponent.putWorkspace(1, WorkspaceUtils.toJson(workspace, false));
        assertEquals(3, wmd.getRevision());

        // and again, to increment the revision
        workspace.setRevision(3L);
        workspaceComponent.putWorkspace(1, WorkspaceUtils.toJson(workspace, false));
        assertEquals(4, wmd.getRevision());
    }

    @Test
    public void putWorkspace_ThrowsAnException_WhenTheIncorrectRevisionIsSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setLastModifiedUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setRevision(1);

        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setRevision(wmd.getRevision());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");
        workspaceComponent.putWorkspace(1, WorkspaceUtils.toJson(workspace, false));
        assertEquals(2, workspaceMetaData.getRevision());

        // and again, to increment the revision
        workspace.setRevision(2L);
        workspaceComponent.putWorkspace(1, WorkspaceUtils.toJson(workspace, false));
        assertEquals(3, workspaceMetaData.getRevision());

        try {
            // and repeat, but specifying revision 2 again
            workspace.setRevision(2L);
            workspaceComponent.putWorkspace(1, WorkspaceUtils.toJson(workspace, false));
            fail();
        } catch (WorkspaceComponentException wce) {
            System.out.println(wce.getMessage());
            assertTrue(wce.getMessage().startsWith("The workspace could not be saved because a newer version has been created by user@example.com at "));
            assertEquals(3, workspaceMetaData.getRevision());
        }
    }

    @Test
    public void shareWorkspace() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(workspaceId);
                wmd.setSharingToken("");

                return wmd;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setSharingToken(wmd.getSharingToken());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        workspaceComponent.shareWorkspace(1);
        assertEquals(36, workspaceMetaData.getSharingToken().length());
    }

    @Test
    public void unshareWorkspace() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        WorkspaceDao dao = new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(workspaceId);
                wmd.setSharingToken("1234567890");

                return wmd;
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData wmd) {
                workspaceMetaData.setSharingToken(wmd.getSharingToken());
            }
        };

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(dao, "");

        workspaceComponent.unshareWorkspace(1);
        assertEquals("", workspaceMetaData.getSharingToken());
    }

}