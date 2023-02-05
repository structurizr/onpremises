package com.structurizr.onpremises.web.user;

import com.structurizr.onpremises.web.AbstractController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UserProfileController extends AbstractController {

    private static final String VIEW = "user-profile";

    @RequestMapping(value = "/user/profile", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showUserProfilePage(ModelMap model) {
        model.addAttribute("user", getUser());

        addCommonAttributes(model, "User Profile", true);

        return VIEW;
    }

}