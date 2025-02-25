package com.structurizr.onpremises.util;

public final class EarlyAccessFeaturesNotAvailableException extends RuntimeException {

    public EarlyAccessFeaturesNotAvailableException(String feature) {
        super(feature + " is not available in this build - see https://docs.structurizr.com/onpremises for details of how to gain early access to new features");
    }

}