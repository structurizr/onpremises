package com.structurizr.onpremises.web;

import com.structurizr.onpremises.web.home.HomePageController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractControllerTests extends ControllerTestsBase {

    private AbstractController controller;

    @Before
    public void setUp() {
        controller = new HomePageController(); // any controller will do
    }

    @Test
    public void isAuthenticated() {
        clearUser();
        assertFalse(controller.isAuthenticated());

        setUser("username");
        assertTrue(controller.isAuthenticated());

        clearUser();
        assertFalse(controller.isAuthenticated());
    }

}