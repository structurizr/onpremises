![Structurizr](https://static.structurizr.com/img/structurizr-banner.png)

# Structurizr on-premises installation

The Structurizr on-premises installation is a standalone version of Structurizr that can be run locally on your own infrastructure, and includes the majority of the features found in the cloud service. Please see the [products page](https://structurizr.com/products) for the differences in feature set.

The on-premises installation is a Java EE/Spring MVC web application, packaged as a .war file, for deployment into any compatible Java EE server, such as Apache Tomcat.
For ease of deployment, by default, all data is stored on the local file system. Optionally, data can be stored on Amazon S3, and search indexes on an Elasticsearch cluster.

- [Documentation](https://structurizr.com/share/18571/documentation) ([source](docs))
- [Issue tracker](https://github.com/structurizr/onpremises/issues)

## Building from source

- Note 1: The HTML, JS, CSS, JSP, etc files are in a separate [structurizr/ui](https://github.com/structurizr/ui) repo because they are shared with the cloud service and Structurizr Lite.
- Note 2: Building and running from source has only been tested with Java 17.

To build from source:

```
git clone https://github.com/structurizr/onpremises.git structurizr-onpremises
git clone https://github.com/structurizr/ui.git structurizr-ui
cd structurizr-onpremises
./ui.sh
./gradlew clean build
```

The integration tests use Testcontainers to start an Elasticsearch instance. If you don't have Docker installed, you can use
`./gradlew clean build -x integrationTest` to exclude the integration tests.


If successful, you will see a file named `structurizr-onpremises.war` in `structurizr-onpremises/build/libs`.
To then build a Docker image:

```
cd structurizr-onpremises
docker build . -t mytag
```

And to start a Docker container from this image (replace `/path/to/dataDirectory`):

```
docker run -it --rm -p 8080:8080 -v /path/to/dataDirectory:/usr/local/structurizr mytag
```