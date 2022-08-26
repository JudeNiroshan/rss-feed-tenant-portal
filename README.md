# rss-feed-tenant-portal

This MVC application is intended to use as a SaaS tenant controller. All the required components are packed into the /deploy/deployment.yaml file.

Login to the intended Red Hat Openshift cluster from your terminal and simply run

`oc apply -f deploy/deployment.yaml`

After successful execution, a new Openshift route will be available in `default` namespace.


### Steps to build the source code:

- `mvn clean install`
- `docker build -t quay.io/<username>/rss-feed-tenant-portal`
- `docker run -p 8080:8080 quay.io/<username>/rss-feed-tenant-portal`

