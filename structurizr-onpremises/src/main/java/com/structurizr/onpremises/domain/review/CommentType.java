package com.structurizr.onpremises.domain.review;

public enum CommentType {

    General,

    RiskLow,
    RiskMedium,
    RiskHigh,

    STRIDE_Spoofing,
    STRIDE_Tampering,
    STRIDE_Repudiation,
    STRIDE_InformationDisclosure,
    STRIDE_DenialOfService,
    STRIDE_ElevationOfPrivilege

}