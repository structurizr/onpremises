package com.structurizr.onpremises.configuration;

import java.util.Properties;

class SamlConfigurer extends Configurer {

    SamlConfigurer(Properties properties) {
        super(properties);
    }

    private static final String DEFAULT_REGISTRATION_ID = "structurizr";

    private static final String DEFAULT_SAML_ATTRIBUTE_USERNAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
    private static final String DEFAULT_SAML_ATTRIBUTE_ROLE = "http://schemas.xmlsoap.org/claims/Group";

    void apply() {
        setDefault(StructurizrProperties.SAML_REGISTRATION_ID, DEFAULT_REGISTRATION_ID);
        setDefault(StructurizrProperties.SAML_ENTITY_ID, "");
        setDefault(StructurizrProperties.SAML_METADATA, "");
        setDefault(StructurizrProperties.SAML_SIGNING_CERTIFICATE, "");
        setDefault(StructurizrProperties.SAML_SIGNING_PRIVATE_KEY, "");
        setDefault(StructurizrProperties.SAML_ATTRIBUTE_USERNAME, DEFAULT_SAML_ATTRIBUTE_USERNAME);
        setDefault(StructurizrProperties.SAML_ATTRIBUTE_ROLE, DEFAULT_SAML_ATTRIBUTE_ROLE);
    }


}