/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.structurizr.onpremises.authentication.saml;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import com.structurizr.onpremises.util.StructurizrProperties;
import com.structurizr.onpremises.web.security.CsrfSecurityRequestMatcher;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.utils.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@org.springframework.context.annotation.Configuration
@EnableWebSecurity
class Configuration {

	private static final Log log = LogFactory.getLog(Configuration.class);

	private static final String DEFAULT_REGISTRATION_ID = "structurizr";

	@Bean
	SecurityFilterChain app(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests((authorize) -> authorize
						.anyRequest().permitAll()
				)
				.csrf((csrf) -> csrf
						.requireCsrfProtectionMatcher(new CsrfSecurityRequestMatcher())
				)
				.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.saml2Login(Customizer.withDefaults())
				.saml2Logout(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
		String registrationId = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_REGISTRATION_ID, DEFAULT_REGISTRATION_ID);
		String entityId = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_ENTITY_ID);
		String metadata = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_METADATA);
		String signingCertificate = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_SIGNING_CERTIFICATE);
		String privateKey = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_SIGNING_PRIVATE_KEY);

		String attributeNameForUsername = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_ATTRIBUTE_USERNAME, StructurizrProperties.DEFAULT_SAML_ATTRIBUTE_USERNAME);
		String attributeNameForRole = com.structurizr.onpremises.util.Configuration.getInstance().getProperty(StructurizrProperties.SAML_ATTRIBUTE_ROLE, StructurizrProperties.DEFAULT_SAML_ATTRIBUTE_ROLE);
		
		log.debug("Configuring SAML authentication...");
		log.debug(StructurizrProperties.SAML_REGISTRATION_ID + ": " + registrationId);
		log.debug(StructurizrProperties.SAML_ENTITY_ID + ": " + entityId);
		log.debug(StructurizrProperties.SAML_METADATA + ": " + metadata);
		log.debug(StructurizrProperties.SAML_SIGNING_CERTIFICATE + ": " + signingCertificate);
		log.debug(StructurizrProperties.SAML_SIGNING_PRIVATE_KEY + ": " + privateKey);
		log.debug(StructurizrProperties.SAML_ATTRIBUTE_USERNAME + ": " + attributeNameForUsername);
		log.debug(StructurizrProperties.SAML_ATTRIBUTE_ROLE + ": " + attributeNameForRole);

		if (StringUtils.isNullOrEmpty(metadata)) {
			String message = "A property named " + StructurizrProperties.SAML_METADATA + " is missing from your structurizr.properties file";
			log.fatal(message);
			throw new RuntimeException(message);
		}

		RelyingPartyRegistration.Builder builder =
				RelyingPartyRegistrations
				.fromMetadataLocation(metadata)
				.registrationId(registrationId);

		if (!StringUtils.isNullOrEmpty(entityId)) {
			builder.entityId(entityId);
		}

		if (!StringUtils.isNullOrEmpty(signingCertificate) && !StringUtils.isNullOrEmpty(privateKey)) {
			builder.signingX509Credentials((c) -> c.add(Saml2X509Credential.signing(privateKey(privateKey), relyingPartyCertificate(signingCertificate))));
		}

		RelyingPartyRegistration relyingPartyRegistration = builder.build();

		return new InMemoryRelyingPartyRegistrationRepository(relyingPartyRegistration);
	}

	X509Certificate relyingPartyCertificate(String signingCertificate) {
		Resource resource = new FileSystemResource(new File(com.structurizr.onpremises.util.Configuration.getInstance().getDataDirectory(), signingCertificate));
		try (InputStream is = resource.getInputStream()) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
		} catch (Exception ex) {
			throw new UnsupportedOperationException(ex);
		}
	}

	public RSAPrivateKey privateKey(String privateKey) {
		File file = new File(com.structurizr.onpremises.util.Configuration.getInstance().getDataDirectory(), privateKey);
		try {
			String key = Files.readString(file.toPath(), Charset.defaultCharset());

			String privateKeyPEM = key
					.replace("-----BEGIN PRIVATE KEY-----", "")
					.replaceAll(System.lineSeparator(), "")
					.replace("-----END PRIVATE KEY-----", "");

			byte[] encoded = Base64.decodeBase64(privateKeyPEM);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}