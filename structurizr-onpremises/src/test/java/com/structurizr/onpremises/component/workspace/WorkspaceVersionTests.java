package com.structurizr.onpremises.component.workspace;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceVersionTests {

    @Test
    void isValidBVersionIdentifier() {
        assertTrue(WorkspaceVersion.isValidBVersionIdentifier("1234567890")); // local
        assertTrue(WorkspaceVersion.isValidBVersionIdentifier("3sL4kqtJlcpXroDTDmJ+rmSpXd3dIbrHY+MTRCxf3vjVBH40Nr8X8gdRQBpUMLUo")); // aws
        assertTrue(WorkspaceVersion.isValidBVersionIdentifier("h_rKc7NTAmEyxFDB0p4CkCyg_y2uHmO.")); // aws
        assertTrue(WorkspaceVersion.isValidBVersionIdentifier("2024-09-07T16:34:45.7048862Z")); // azure

        assertFalse(WorkspaceVersion.isValidBVersionIdentifier("<script>"));
    }

    @Test
    void validateVersionIdentifier() {
        WorkspaceVersion.validateVersionIdentifier("1234567890");
        WorkspaceVersion.validateVersionIdentifier("3sL4kqtJlcpXroDTDmJ+rmSpXd3dIbrHY+MTRCxf3vjVBH40Nr8X8gdRQBpUMLUo");
        WorkspaceVersion.validateVersionIdentifier("2024-09-07T16:34:45.7048862Z");

        try {
            WorkspaceVersion.validateVersionIdentifier("<script>");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The version identifier \"<script>\" is invalid", e.getMessage());
        }
    }

}