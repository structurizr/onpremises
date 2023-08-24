package com.structurizr.onpremises.web.api;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Md5Digest {

    private static final String ALGORITHM = "MD5";

    String generate(String content) throws NoSuchAlgorithmException {
        if (content == null) {
            content = "";
        }

        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        return DatatypeConverter.printHexBinary(digest.digest(content.getBytes(StandardCharsets.UTF_8))).toLowerCase();
    }

}