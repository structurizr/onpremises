## Troubleshooting

Here are some common problems, and how to resolve them.

#### The home page loads, but doesn't look right

If you can see the home page, but it doesn't look right (e.g. the styles don't seem to be loading, images are oversized, etc),
you will likely need to explicitly set the `structurizr.url` property. See [Configuration - structurizr.properties](03-configuration.md#structurizrproperties) for details.

#### The on-premises installation cannot be accessed or is not found

The on-premises installation needs to be run as the root/default web application/context.
For Apache Tomcat, this means the web application named `ROOT`, running at `/`.
It is not possible to run the on-premises installation with an alternative context (e.g. `/structurizr-onpremises`).

#### Embedded diagrams don't load

If you have installed the on-premises installation behind a reverse proxy,
be aware that some reverse proxies will add additional HTTP headers, which may override those generated/used by the on-premises installation,
causing issues such as the embedded diagram viewer/editor not working.
The following headers may need to be disabled in your reverse proxy if they are being set:

- [X-Frame-Options](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options)
- [Content-Security-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy)

#### Diagram reviews fail to be created

Some web servers (e.g. Apache Tomcat) restrict the quantity of data that can be sent in a HTTP POST request.
If you find that creating diagram reviews fails, you may need to change this configuration.
For Apache Tomcat, you can modify the `maxPostSize` parameter for your connector in the `server.xml` file (see [https://tomcat.apache.org/tomcat-9.0-doc/config/http.html](https://tomcat.apache.org/tomcat-9.0-doc/config/http.html)).
