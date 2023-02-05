package com.structurizr.onpremises.web.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HmacContentTests {


    @Test
    public void toString_WhenThereAreNoStrings() {
        assertEquals("", new HmacContent().toString());
    }

    @Test
    public void toString_WhenThereAreSomeStrings() {
        assertEquals("String1\nString2\nString3\n", new HmacContent("String1", "String2", "String3").toString());
    }

}
