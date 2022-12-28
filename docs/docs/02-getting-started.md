## Getting started

Here's a brief guide to getting started with the Structurizr on-premises installation.

### Create the Structurizr data directory

The Structurizr on-premises installation needs to be given access to a directory, where all data will be stored.
We'll refer to this directory as the "Structurizr data directory".

You now have the choice of whether to use the [pre-built Docker image](#docker) or to deploy the [Java EE application](#java-ee) yourself.

### Docker

Assuming that you have Docker installed, to start the Structurizr on-premises installation, use the following command to pull the image from [Docker Hub](https://hub.docker.com/r/structurizr/onpremises).

```
docker pull structurizr/onpremises
```

Then use the following command to start the Docker container, replacing `PATH` with the path to your Structurizr data directory:

```
docker run -it --rm -p 8080:8080 -v PATH:/usr/local/structurizr structurizr/onpremises
```

For example, if your Structurizr data directory is located at `/Users/simon/structurizr`, the command would be:

```
docker run -it --rm -p 8080:8080 -v /Users/simon/structurizr:/usr/local/structurizr structurizr/onpremises
```

### Java EE

To use the Java EE version, you'll need:

- Java 11+ (required)
- A Java EE compatible web/application server (required, e.g. [Apache Tomcat 9.x](https://tomcat.apache.org/download-90.cgi) ... please note that Tomcat 10.x and other Jakarta EE servers are not supported)
- [Graphviz](https://graphviz.org/download/) (optional if you want to use automatic layout)

Here are some basic instructions that assume you are using a freshly downloaded version of Apache Tomcat.
In the instructions that follow (`TOMCAT_HOME` refers to the location of the Apache Tomcat installation).

#### Shutdown Apache Tomcat

Shutdown Apache Tomcat if it's running.

#### Delete the ROOT web application

Delete the following if they exist:

- `TOMCAT_HOME/webapps/ROOT.war`
- `TOMCAT_HOME/webapps/ROOT`

#### Download/copy the on-premises installation file

Download the [https://static.structurizr.com/download/structurizr-onpremises.war](https://static.structurizr.com/download/structurizr-onpremises.war),
move it to the `TOMCAT_HOME/webapps` directory,
and rename it to `ROOT.war` (the on-premises installation must be installed as the root web application).

#### Configuration

You then need to configure the Structurizr data directory location.
The easiest way to do this is to set an environment variable named `STRUCTURIZR_DATA_DIRECTORY`,
with a value of the full path to your Structurizr data directory. For example:

```
export STRUCTURIZR_DATA_DIRECTORY=/Users/simon/structurizr
```


#### Start Apache Tomcat

After starting Apache Tomcat (e.g. using the `TOMCAT_HOME/bin/startup.sh` or `TOMCAT_HOME\bin\startup.bat` script).

#### Building your own Docker image

There is a [Dockerfile](https://github.com/structurizr/onpremises/blob/main/Dockerfile) in the GitHub repo that can be used as a starting point if you'd like to build your own Docker image.

### Using the Structurizr on-premises installation

If deployment was successful, navigating to [http://localhost:8080](http://localhost:8080) should open the Structurizr on-premises installation.
You will see the end user license agreement, which you will need to accept, before the on-premises installation home page is displayed.
You can then sign in using the default credentials (`structurizr` and `password`).
