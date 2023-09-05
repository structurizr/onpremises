package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceMetaDataTests {

    @Test
    public void isPublic() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertFalse(workspace.isPublicWorkspace());

        workspace.setPublicWorkspace(true);
        assertTrue(workspace.isPublicWorkspace());
    }

    @Test
    public void open() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertTrue(workspace.isOpen()); // workspaces are open by default (i.e. no configured users)

        workspace.addWriteUser("user1"); // adding a user makes the workspace private
        assertFalse(workspace.isOpen());

        workspace.setPublicWorkspace(true); // force the workspace to be public
        assertTrue(workspace.isOpen());
    }

    @Test
    public void sharingToken() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertEquals("", workspace.getSharingToken());
        assertFalse(workspace.isShareable());

        workspace.setSharingToken("12345678901234567890");
        assertEquals("12345678901234567890", workspace.getSharingToken());
        assertEquals("123456...", workspace.getSharingTokenTruncated());
        assertTrue(workspace.isShareable());
    }

    @Test
    public void hasNoUsersConfigured_ReturnsTrue_WhenTheWorkspaceHasNoConfiguredUsers() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertTrue(workspace.hasNoUsersConfigured());
    }

    @Test
    public void hasNoUsersConfigured_ReturnsFalse_WhenTheWorkspaceHasAReadOnlyUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("user");
        assertFalse(workspace.hasNoUsersConfigured());
    }

    @Test
    public void hasNoUsersConfigured_ReturnsFalse_WhenTheWorkspaceHasAReadWriteUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("user");
        assertFalse(workspace.hasNoUsersConfigured());
    }

    @Test
    public void isReadUser_ReturnsFalse_WhenTheUserIsNotAReadUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("simon");

        User user = new User("user", new HashSet<>(), null);
        assertFalse(workspace.isReadUser(user));
    }

    @Test
    public void isReadUser_ReturnsTrue_WhenTheUserIsAReadUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("user");

        User user = new User("user", new HashSet<>(), null);
        assertTrue(workspace.isReadUser(user));
    }

    @Test
    public void isReadUser_ReturnsFalse_WhenTheUserRoleIsNotAReadUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("role1");

        User user = new User("user", new HashSet<>(List.of("role2")), AuthenticationMethod.LOCAL);
        assertFalse(workspace.isReadUser(user));
    }

    @Test
    public void isReadUser_ReturnsTrue_WhenTheUserRoleIsAReadUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("role1");

        User user = new User("bob", new HashSet<>(List.of("role1", "role2")), AuthenticationMethod.LOCAL);
        assertTrue(workspace.isReadUser(user));
    }

    @Test
    public void isWriteUser_ReturnsFalse_WhenTheUserIsNotAWriteUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("simon");

        User user = new User("user", new HashSet<>(), null);
        assertFalse(workspace.isWriteUser(user));
    }

    @Test
    public void isWriteUser_ReturnsTrue_WhenTheUserIsAWriteUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("user");

        User user = new User("user", new HashSet<>(), null);
        assertTrue(workspace.isWriteUser(user));
    }

    @Test
    public void test_isWriteUser_ReturnsFalse_WhenTheUserRoleIsNotAWriteUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("role1");

        User user = new User("user", new HashSet<>(List.of("role2")), AuthenticationMethod.LOCAL);
        assertFalse(workspace.isWriteUser(user));
    }

    @Test
    public void test_isWriteUser_ReturnsTrue_WhenTheUserRoleIsAWriteUser() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("role1");

        User user = new User("user", new HashSet<>(List.of("role1", "role2")), AuthenticationMethod.LOCAL);
        assertTrue(workspace.isWriteUser(user));
    }

    @Test
    public void test_isLocked_ReturnsFalse_WhenTheWorkspaceIsNotLocked() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser(null);
        workspaceMetaData.setLockedDate(null);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    public void test_isLocked_ReturnsTrue_WhenTheWorkspaceIsLocked() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedDate(new Date());

        assertTrue(workspaceMetaData.isLocked());
    }

    @Test
    public void test_isLocked_ReturnsFalse_WhenTheWorkspaceWasLockedOverTwoMinutesAgo() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(3));

        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    public void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLocked() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser(null);
        workspaceMetaData.setLockedDate(null);
        assertFalse(workspaceMetaData.isLockedBy("user", "agent"));
    }

    @Test
    public void test_isLockedBy_ReturnsTrue_WhenTheWorkspaceIsLockedByTheSpecifiedUserAndAgent() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(new Date());

        assertTrue(workspaceMetaData.isLockedBy("user", "agent"));
    }

    @Test
    public void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLockedByTheSpecifiedUserAndAgent() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent1");
        workspaceMetaData.setLockedDate(new Date());

        assertFalse(workspaceMetaData.isLockedBy("user1", "agent2"));
    }

    @Test
    public void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLockedByTheSpecifiedUser() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent1");
        workspaceMetaData.setLockedDate(new Date());

        assertFalse(workspaceMetaData.isLockedBy("user2", "agent2"));
    }

    @Test
    public void fromProperties_and_toProperties() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(WorkspaceMetaData.NAME_PROPERTY, "Name");
        properties.setProperty(WorkspaceMetaData.DESCRIPTION_PROPERTY, "Description");
        properties.setProperty(WorkspaceMetaData.VERSION_PROPERTY, "v1.2.3");
        properties.setProperty(WorkspaceMetaData.CLIENT_SIDE_ENCRYPTED_PROPERTY, "false");
        properties.setProperty(WorkspaceMetaData.LAST_MODIFIED_USER_PROPERTY, "user1");
        properties.setProperty(WorkspaceMetaData.LAST_MODIFIED_AGENT_PROPERTY, "structurizr/dsl");
        properties.setProperty(WorkspaceMetaData.LAST_MODIFIED_DATE_PROPERTY, "2021-01-31T14:30:59Z");
        properties.setProperty(WorkspaceMetaData.REVISION_PROPERTY, "81");
        properties.setProperty(WorkspaceMetaData.API_KEY_PROPERTY, "1234567890");
        properties.setProperty(WorkspaceMetaData.API_SECRET_PROPERTY, "0987654321");
        properties.setProperty(WorkspaceMetaData.PUBLIC_PROPERTY, "true");
        properties.setProperty(WorkspaceMetaData.SHARING_TOKEN_PROPERTY, "12345678901234567890");
        properties.setProperty(WorkspaceMetaData.OWNER_PROPERTY, "user@example.com");
        properties.setProperty(WorkspaceMetaData.LOCKED_USER_PROPERTY, "user2@example.com");
        properties.setProperty(WorkspaceMetaData.LOCKED_AGENT_PROPERTY, "structurizr/web");
        properties.setProperty(WorkspaceMetaData.LOCKED_DATE_PROPERTY, "2022-01-31T14:30:59Z");
        properties.setProperty(WorkspaceMetaData.READ_USERS_AND_ROLES_PROPERTY, "user1,user2,user3");
        properties.setProperty(WorkspaceMetaData.WRITE_USERS_AND_ROLES_PROPERTY, "user4,user5,user6");
        properties.setProperty(WorkspaceMetaData.ARCHIVED_PROPERTY, "false");

        WorkspaceMetaData workspace = WorkspaceMetaData.fromProperties(123, properties);

        assertEquals("Name", workspace.getName());
        assertEquals("Description", workspace.getDescription());
        assertEquals("v1.2.3", workspace.getVersion());
        assertFalse(workspace.isClientEncrypted());
        assertEquals("user1", workspace.getLastModifiedUser());
        assertEquals("structurizr/dsl", workspace.getLastModifiedAgent());
        assertEquals(DateUtils.parseIsoDate("2021-01-31T14:30:59Z"), workspace.getLastModifiedDate());
        assertEquals(81, workspace.getRevision());
        assertEquals("1234567890", workspace.getApiKey());
        assertEquals("0987654321", workspace.getApiSecret());
        assertTrue(workspace.isPublicWorkspace());
        assertEquals("12345678901234567890", workspace.getSharingToken());
        assertEquals("user@example.com", workspace.getOwner());
        assertEquals("user2@example.com", workspace.getLockedUser());
        assertEquals("structurizr/web", workspace.getLockedAgent());
        assertEquals(DateUtils.parseIsoDate("2022-01-31T14:30:59Z"), workspace.getLockedDate());
        assertEquals(Set.of("user1", "user2", "user3"), workspace.getReadUsers());
        assertEquals(Set.of("user4", "user5", "user6"), workspace.getWriteUsers());
        assertFalse(workspace.isArchived());
        
        properties.clear();
        properties = workspace.toProperties();

        assertEquals("Name", properties.getProperty(WorkspaceMetaData.NAME_PROPERTY));
        assertEquals("Description", properties.getProperty(WorkspaceMetaData.DESCRIPTION_PROPERTY));
        assertEquals("v1.2.3", properties.getProperty(WorkspaceMetaData.VERSION_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetaData.CLIENT_SIDE_ENCRYPTED_PROPERTY));
        assertEquals("user1", properties.getProperty(WorkspaceMetaData.LAST_MODIFIED_USER_PROPERTY));
        assertEquals("structurizr/dsl", properties.getProperty(WorkspaceMetaData.LAST_MODIFIED_AGENT_PROPERTY));
        assertEquals("2021-01-31T14:30:59Z", properties.getProperty(WorkspaceMetaData.LAST_MODIFIED_DATE_PROPERTY));
        assertEquals("81", properties.getProperty(WorkspaceMetaData.REVISION_PROPERTY));
        assertEquals("1234567890", properties.getProperty(WorkspaceMetaData.API_KEY_PROPERTY));
        assertEquals("0987654321", properties.getProperty(WorkspaceMetaData.API_SECRET_PROPERTY));
        assertEquals("true", properties.getProperty(WorkspaceMetaData.PUBLIC_PROPERTY));
        assertEquals("12345678901234567890", properties.getProperty(WorkspaceMetaData.SHARING_TOKEN_PROPERTY));
        assertEquals("user@example.com", properties.getProperty(WorkspaceMetaData.OWNER_PROPERTY));
        assertEquals("user2@example.com", properties.getProperty(WorkspaceMetaData.LOCKED_USER_PROPERTY));
        assertEquals("structurizr/web", properties.getProperty(WorkspaceMetaData.LOCKED_AGENT_PROPERTY));
        assertEquals("2022-01-31T14:30:59Z", properties.getProperty(WorkspaceMetaData.LOCKED_DATE_PROPERTY));
        assertEquals("user1,user2,user3", properties.getProperty(WorkspaceMetaData.READ_USERS_AND_ROLES_PROPERTY));
        assertEquals("user4,user5,user6", properties.getProperty(WorkspaceMetaData.WRITE_USERS_AND_ROLES_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetaData.ARCHIVED_PROPERTY));
    }

    @Test
    public void fromProperties_DefaultsLastModifiedDateWhenNotSet() throws Exception {
        WorkspaceMetaData workspace = WorkspaceMetaData.fromProperties(123, new Properties());
        assertEquals(DateUtils.parseIsoDate("1970-01-01T00:00:00Z"), workspace.getLastModifiedDate());
    }

}