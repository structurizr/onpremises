package com.structurizr.onpremises.web.error;

import com.structurizr.onpremises.web.AbstractController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class Http500Controller extends AbstractController {

    private static Log log = LogFactory.getLog(Http500Controller.class);

    @RequestMapping(value = "/500", method = RequestMethod.GET)
    public String showErrorPage(ModelMap model) {
        addCommonAttributes(model, "500", true);

        return "500";
    }

}
