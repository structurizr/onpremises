package com.structurizr.onpremises.web.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HmacAuthorizationHeaderTests {

    private HmacAuthorizationHeader header;

    @Test
    public void format() {
        header = new HmacAuthorizationHeader("apiKey", "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
        assertEquals("apiKey:ZjdiYzgzZjQzMDUzODQyNGIxMzI5OGU2YWE2ZmIxNDNlZjRkNTlhMTQ5NDYxNzU5OTc0NzlkYmMyZDFhM2NkOA==", header.format());
    }

    @Test
    public void parse() {
        header = HmacAuthorizationHeader.parse("apiKey:ZjdiYzgzZjQzMDUzODQyNGIxMzI5OGU2YWE2ZmIxNDNlZjRkNTlhMTQ5NDYxNzU5OTc0NzlkYmMyZDFhM2NkOA==");
        assertEquals("apiKey", header.getApiKey());
        assertEquals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8", header.getHmac());
    }

    @Test
    public void parse_ThrowsAnException_WhenTheStringDoesNotContainAColonCharacter() {
        try {
            HmacAuthorizationHeader.parse("123");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("Invalid authorization header", iae.getMessage());
        }
    }

    @Test
    public void parse_ThrowsAnException_WhenTheStringContainsMoreThanOneColonCharacter() {
        try {
            HmacAuthorizationHeader.parse("1:2:3");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("Invalid authorization header", iae.getMessage());
        }
    }

}