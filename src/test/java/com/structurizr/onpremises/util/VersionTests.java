package com.structurizr.onpremises.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class VersionTests {

    @Test
    public void testVersionInformationIsLoadable() {
        Version version = new Version();
        assertNotNull(version.getBuildNumber());
        assertNotNull(version.getBuildTimestamp());
        assertNotNull(version.getGitCommit());
   }

}