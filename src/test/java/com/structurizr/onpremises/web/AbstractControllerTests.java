package com.structurizr.onpremises.web;

import com.structurizr.onpremises.web.home.HomePageController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractControllerTests extends ControllerTestsBase {

    private AbstractController controller;

    @BeforeEach
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