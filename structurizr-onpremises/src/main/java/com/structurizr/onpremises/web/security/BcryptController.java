package com.structurizr.onpremises.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BcryptController {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public BcryptController(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @RequestMapping(value = "/bcrypt/{plaintext}", method = RequestMethod.GET, produces = "text/plain")
    public String getWorkspace(@PathVariable("plaintext") String plaintext) {
        return bCryptPasswordEncoder.encode(plaintext) + "\n";
    }

}