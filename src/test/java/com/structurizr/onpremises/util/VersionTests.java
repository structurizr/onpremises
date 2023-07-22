package com.structurizr.onpremises.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VersionTests {

    @Test
    public void testVersionInformationIsLoadable() {
        Version version = new Version();
        assertNotNull(version.getBuildNumber());
        assertNotNull(version.getBuildTimestamp());
   }

}