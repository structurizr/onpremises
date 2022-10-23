## Overview

The Structurizr on-premises installation is a standalone version of Structurizr that can be run locally on your own infrastructure, and includes the majority of the features found in the cloud service. Please see the [products page](https://structurizr.com/products) for the differences in feature set.

The on-premises installation is a Java EE/Spring MVC web application, packaged as a .war file, for deployment into any compatible Java EE server, such as Apache Tomcat.
For ease of deployment, by default, all data is stored on the local file system.
Optionally, data can be stored on Amazon S3, and search indexes on an Elasticsearch cluster.