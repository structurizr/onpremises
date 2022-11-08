## HTTP sessions

There are two variants for HTTP session storage.

### Local server

By default, HTTP sessions are stored locally, in memory, on the server that created them. This works for a single server installation, but may not work for a high-availability installation,
particularly where multiple instances are deployed behind a load balancer that is delivering requests using a round-robin algorithm. If "sticky sessions" or "session pinning" is not an option, you can choose
to have HTTP session information stored in a Redis database instead.

### Redis database

To use Redis for HTTP session storage, assuming that you have a Redis installation and up running, make the following changes to your
`structurizr.properties` file, and restart your on-premises installation.

1. Add a property named `structurizr.session` with a value of `redis`.
2. Add properties named `structurizr.redis.host`, `structurizr.redis.port`, and `structurizr.redis.password` with values that reflect your Redis installation.

A side-effect of using Redis for session storage is that user sessions should survive restarts of the on-premises installation.