package com.structurizr.onpremises.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.DateUtils;
import com.structurizr.onpremises.util.Features;
import com.structurizr.util.WorkspaceUtils;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import static com.structurizr.onpremises.util.DateUtils.UTC_TIME_ZONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceComponentTests {

    private static final File DATA_DIRECTORY = new File("./build/WorkspaceComponentTests");
    private WorkspaceComponent workspaceComponent;

    @BeforeEach
    public void setUp() {
        deleteDirectory(DATA_DIRECTORY);

        FileSystemWorkspaceDao dao = new FileSystemWorkspaceDao(DATA_DIRECTORY);
        workspaceComponent = new WorkspaceComponentImpl(dao, "");

        Configuration.init();
    }

    @Test
    public void test() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(FileSystemWorkspaceDao.VERSION_TIMESTAMP_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));
        
        User user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);
        long workspaceId = workspaceComponent.createWorkspace(user);
        assertEquals(1, workspaceId);

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(1);
        String jsonV1 = String.format("""
                {"configuration":{},"description":"Description","documentation":{},"id":1,"lastModifiedDate":"%s","model":{},"name":"Workspace 0001","revision":1,"views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()));
        assertEquals(jsonV1, workspaceComponent.getWorkspace(1, ""));

        Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(1, workspaces.size());
        assertEquals(workspaceId, workspaces.iterator().next().getId());

        List<WorkspaceVersion> workspaceVersions = workspaceComponent.getWorkspaceVersions(1, 10);
        assertEquals(1, workspaceVersions.size());
        WorkspaceVersion version1 = workspaceVersions.get(0); // keep this for later
        assertNull(workspaceVersions.get(0).getVersionId());
        assertEquals(DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()), DateUtils.formatIsoDate(workspaceVersions.get(0).getLastModifiedDate()));

        Thread.sleep(2 * 1000); // sleep for a couple of seconds, otherwise all workspace versions have the same timestamp

        Workspace workspace = new Workspace("Financial Risk System", "...");
        String json = WorkspaceUtils.toJson(workspace, false);
        workspaceComponent.putWorkspace(1, json);

        workspaceMetaData = workspaceComponent.getWorkspaceMetaData(1);
        String jsonV2 = String.format("""
                {"configuration":{},"description":"...","documentation":{},"id":1,"lastModifiedDate":"%s","model":{},"name":"Financial Risk System","revision":2,"views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()));
        assertEquals(jsonV2, workspaceComponent.getWorkspace(1, ""));

        workspaceVersions = workspaceComponent.getWorkspaceVersions(1, 10);
        assertEquals(2, workspaceVersions.size());
        assertNull(workspaceVersions.get(0).getVersionId());
        assertEquals(DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()), DateUtils.formatIsoDate(workspaceVersions.get(0).getLastModifiedDate()));

        assertEquals(sdf.format(version1.getLastModifiedDate()), workspaceVersions.get(1).getVersionId());
        assertEquals(DateUtils.formatIsoDate(version1.getLastModifiedDate()), DateUtils.formatIsoDate(workspaceVersions.get(1).getLastModifiedDate()));

        json = workspaceComponent.getWorkspace(1, sdf.format(version1.getLastModifiedDate()));
        assertEquals(jsonV1, json);

        try {
            workspaceComponent.getWorkspace(1, "1234567890"); // invalid workspace version
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not get workspace 1 with version 1234567890", e.getMessage());
        }

        boolean result = workspaceComponent.deleteWorkspace(1);
        assertTrue(result);

        try {
            assertNull(workspaceComponent.getWorkspaceMetaData(1));
            workspaceComponent.getWorkspace(1, "");
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not get workspace 1", e.getMessage());
        }
    }

    @Test
    public void deleteWorkspace() throws Exception {
        User user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);
        long workspaceId = workspaceComponent.createWorkspace(user);
        assertEquals(1, workspaceId);

        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_ARCHIVING);
        assertTrue(workspaceComponent.deleteWorkspace(1));
        assertFalse(new File(DATA_DIRECTORY, "1").exists());

        // create a new workspace - the ID should be recycled
        workspaceId = workspaceComponent.createWorkspace(user);
        assertEquals(1, workspaceId);

        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_ARCHIVING);
        assertTrue(workspaceComponent.deleteWorkspace(1));
        assertTrue(new File(DATA_DIRECTORY, "1").exists());
        assertNull(workspaceComponent.getWorkspaceMetaData(1));

        // with workspace archiving enabled, the workspace isn't deleted, so we get a new ID
        workspaceId = workspaceComponent.createWorkspace(user);
        assertEquals(2, workspaceId);
    }

    @AfterEach
    public void tearDown() {
        deleteDirectory(DATA_DIRECTORY);
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                file.delete();
            }
        }
    }

}