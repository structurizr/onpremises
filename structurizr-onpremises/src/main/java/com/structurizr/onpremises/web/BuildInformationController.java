package com.structurizr.onpremises.web;

import com.structurizr.onpremises.util.Version;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuildInformationController {

    @RequestMapping(value = "/help/build/number", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public String hello(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET");

        return new Version().getBuildNumber();
    }

}
