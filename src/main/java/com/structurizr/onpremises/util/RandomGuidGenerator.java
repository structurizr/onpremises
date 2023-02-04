package com.structurizr.onpremises.util;

import java.util.UUID;

public final class RandomGuidGenerator {

    public String generate() {
        return UUID.randomUUID().toString();
    }

}