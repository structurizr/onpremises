## Troubleshooting

Here are some common problems, and how to resolve them.
If your problem isn't listed here, please browse [the issues on the GitHub repo](https://github.com/structurizr/onpremises/issues).

### Installation

#### The home page loads, but doesn't look right

If you can see the home page, but it doesn't look right (e.g. the styles don't seem to be loading, images are oversized, etc),
you will likely need to explicitly set the `structurizr.url` property. See [Configuration - structurizr.properties](03-configuration.md#structurizrproperties) for details.

#### The on-premises installation cannot be accessed or is not found

The on-premises installation needs to be run as the root/default web application/context.
For Apache Tomcat, this means the web application named `ROOT`, running at `/`.
It is not possible to run the on-premises installation with an alternative context (e.g. `/structurizr-onpremises`).

### Diagrams

#### Embedded diagrams don't load

If you have installed the on-premises installation behind a reverse proxy,
be aware that some reverse proxies will add additional HTTP headers, which may override those generated/used by the on-premises installation,
causing issues such as the embedded diagram viewer/editor not working.
The following headers may need to be disabled in your reverse proxy if they are being set:

- [X-Frame-Options](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options)
- [Content-Security-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy)

### Reviews

#### Diagram reviews fail to be created

Some web servers (e.g. Apache Tomcat) restrict the quantity of data that can be sent in a HTTP POST request.
If you find that creating diagram reviews fails, you may need to change this configuration.
For Apache Tomcat, you can modify the `maxPostSize` parameter for your connector in the `server.xml` file (see [https://tomcat.apache.org/tomcat-9.0-doc/config/http.html](https://tomcat.apache.org/tomcat-9.0-doc/config/http.html)).

### SAML integration

The variation between identity providers and how organisations configure identity providers can make it difficult
to configure SAML integration, and even the smallest misconfiguration can cause errors, most of which you'll see in
the logs as a HTTP 405, `Request method 'POST' not supported` message. Some recommended steps to resolve this are:

1. Configure a non-secure (i.e. HTTP) `localhost` instance of the on-premises installation against your IdP to ascertain whether the problems you are seeing are related to your hosting environment (i.e. HTTPS, load balancers, reverse proxies, DNS, etc).
2. Debug the SAML handshake with one of the available browser plugins.
3. Enable debug on the on-premises installation to see the underlying error message (see [Logging](#logging)).

##### Max authentication age

By default, Spring Security checks that you've been authenticated with your IdP within the past 2 hours (7200 seconds).
If this value is too low, you can override it via a property named `structurizr.saml.maxAuthenticationAge` in your `structurizr.properties` file (the value is the number of seconds, e.g. 86400 seconds for 24 hours).

##### Force authentication

If you see intermittent HTTP 405 errors when trying to sign in (particularly after signing in already),
you can set Structurizr to force authentication by setting a property named `structurizr.saml.forceAuthentication`
in your `structurizr.properties` file (`true`, or `false` by default).

