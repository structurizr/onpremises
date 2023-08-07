## Data storage

### Amazon Web Services S3

The default configuration uses the local file system for data storage.
This is sufficient for a single server installation, as well as for a multi-server installation where you can share a file system between server nodes.
Alternatively, Amazon Web Services S3 (or an S3 compatible service) can be used for data storage.

![](embed:Deployment-Example4)

The basic steps to configure S3 are:

- Create a bucket under your AWS account (folders named `workspaces` and `reviews` will be created in this bucket).
- Create a new programmatic access user in AWS, with the following permissions: `AmazonS3FullAccess`.
- Modify your `structurizr.properties` file to configure AWS S3 integration as follows:

| Property name | Property value |
| ------------- | -------------- |
| `structurizr.data` | `aws-s3` |
| `aws-s3.accessKeyId` | Your AWS API key ID. |
| `aws-s3.secretAccessKey` | Your AWS API secret access key. |
| `aws-s3.region` | Your AWS region (e.g. `us-east-1`). |
| `aws-s3.bucketName` | Your S3 bucket name. |
| `aws-s3.endpoint` | Your custom S3-compatible endpoint. |
| `aws-s3.pathStyleAccess` | Enables aws sdk path style access. |

Alternatively, you can leave the `aws-s3.accessKeyId` and `aws-s3.secretAccessKey` parameters unset,
and the on-premises installation will use the "Default Credential Provider Chain" to search your environment for the credentials, as described at
[Working with AWS Credentials - Using the Default Credential Provider Chain](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html).

You will need to enable bucket versioning if you'd like to use [Structurizr's workspace versioning feature](https://structurizr.com/help/workspace-versioning).

### Elasticsearch

The default configuration uses Apache Lucene for search functionality, storing search indexes on the local file system.
This is sufficient for a single server installation, but Apache Lucene's search indexes are not designed for concurrent access from multiple processes.
To run the Structurizr on-premises installation across multiple servers, for high availability, you'll need to use Elasticsearch instead.
You can use a local Elasticsearch installation, or a service provided by a cloud provider.
Version 6.8.10 and above is recommended.

![](embed:Deployment-Example2)

Modify your `structurizr.properties` file to configure Elasticsearch integration as follows:

| Property name | Property value |
| ------------- | -------------- |
| `structurizr.search` | `elasticsearch` |
| `elasticsearch.protocol` | The protocol used to communicate with Elasticsearch: `http` (default) or `https`. |
| `elasticsearch.host` | Your Elasticsearch hostname/IP address (default: `localhost`) |
| `elasticsearch.port` | Your Elasticsearch port number (default: `9200`). |
| `elasticsearch.username` | The username used to connect to your Elasticsearch instance, if applicable. |
| `elasticsearch.password` | The password used to connect to your Elasticsearch instance, if applicable. |
