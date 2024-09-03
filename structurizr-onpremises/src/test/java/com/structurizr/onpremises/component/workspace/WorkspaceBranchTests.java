package com.structurizr.onpremises.component.workspace;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceBranchTests {

    @Test
    void isValidName() {
        assertTrue(WorkspaceBranch.isValidBranchName("main"));
        assertTrue(WorkspaceBranch.isValidBranchName("dev"));
        assertTrue(WorkspaceBranch.isValidBranchName("dev"));
        assertTrue(WorkspaceBranch.isValidBranchName("dev-0.0.1"));
        assertTrue(WorkspaceBranch.isValidBranchName("0.0.1"));

        assertFalse(WorkspaceBranch.isValidBranchName("-dev"));
        assertFalse(WorkspaceBranch.isValidBranchName("_dev"));
        assertFalse(WorkspaceBranch.isValidBranchName(".dev"));
    }

    @Test
    void validateBranchName() {
        WorkspaceBranch.validateBranchName("");
        WorkspaceBranch.validateBranchName("dev");

        try {
            WorkspaceBranch.validateBranchName(".dev");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The branch name \".dev\" is invalid", e.getMessage());
        }
    }

}