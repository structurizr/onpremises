## FAQ

Here are answers to some frequently asked questions. If you don't see an answer you are looking for, please get in touch.

#### What are the minimum hardware requirements?

The on-premises installation will happily run on a laptop, supporting hundreds of concurrent users, so any small server with a few GB of RAM should be sufficient for most teams.

#### Can the on-premises version of Structurizr be installed on public cloud IaaS and PaaS services?

The on-premises version is designed to run inside a trusted network environment, but some of our customers have installed it on
environments such as the public Amazon Web Services and Microsoft Azure clouds.
Please ensure that you consider the security implications of doing this, and that you harden any servers and restrict access where appropriate.

#### Can the on-premises installation be clustered for high availability?

Yes, [Amazon Web Service S3](04-authentication.md#amazon-web-services-s3) and [Elasticsearch](04-authentication.md#elasticsearch) are supported.

#### Does the on-premises installation require an Internet connection?

No, the on-premises installation can run completely disconnected from the Internet, and doesn't make any requests to the Internet to check for updates.
You will only require Internet access from the server if you have workspaces that make use of an Internet hosted [theme](https://structurizr.com/help/themes) and have server-side (Graphviz) automatic layout enabled.

#### Does the on-premises installation automatically update itself?

There is no auto-update mechanism, so new versions of the Structurizr on-premises installation need to be applied manually.
The cloud service and on-premises installation share a common codebase, so any updates made to the cloud service are
immediately available in the on-premises installation. To update, pull the latest Docker image or download a new version of the
`.war` file, overwrite the existing version and restart your web/application server.
We recommend scripting your deployments, and ensuring that you can easily rollback if needed.

#### Can the on-premises installation run over HTTPS?

Yes, see [HTTPS](03-configuration.md#https) for details.
