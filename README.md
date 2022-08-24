# rss-feed-tenant-portal

Steps to run:

- `mvn clean install`
- `docker build -t quay.io/<username>/rss-feed-tenant-portal`
- `docker run -p 8080:8080 quay.io/<username>/rss-feed-tenant-portal`


**Note**

Currently the application is running with a in-memory database. It can be switched to MySQL database with few configuration changes.
