package com.structurizr.onpremises.util;

public final class EarlyAccessBuildException extends RuntimeException {

    EarlyAccessBuildException(String feature) {
        super(feature + " is not available in this build - see https://docs.structurizr.com/onpremises/early-access for details of how to gain early access to new features");
    }

}