package com.structurizr.onpremises.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JsonUtils {

    public static String escapeJson(String json) {
        json = json.replace("\\", "\\\\");
        json = json.replace("'", "\\'");

        return json;
    }

    public static String base64(String json) {
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

}