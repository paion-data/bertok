Aristotle
=========

![Java Version Badge][Java Version Badge]
[![Apache License Badge]][Apache License, Version 2.0]

Aristotle is a [JSR 370] [JAX-RS] webservice of CRUD operations against a graph database. It supports Neo4J now.

Start Locally in Jetty
----------------------

Navigate to a dedicated directory; make sure port 8080 is not occupied and the following environment variables are set:

```console
export NEO4J_URI=
export NEO4J_USERNAME=
export NEO4J_PASSWORD=
export NEO4J_DATABASE=
```

Then start webservice with:

```bash
./jetty-start.sh
```

Press `Ctr-C` to stop the webservice and delete generated directories if needed when done.

Test
----

```console
mvn clean verify
```

Deployment
----------

This is a one-person project. Agility outplays team scaling, so deployment is manual and pretty much follows
[jetty-start.sh](./jetty-start.sh)

### Sending Logs to ELK Cloud

Simply add Logstash integration and install agent on the production server. The logs will be available on integration
dashboard.

### Gateway Registration

```bash
export GATEWAY_PUBLIC_IP=52.53.186.26

# vocabulary paged & count
curl -v -i -s -k -X POST https://api.paion-data.dev:8444/services \
  --data name=wilhelm-ws-languages \
  --data url="http://${GATEWAY_PUBLIC_IP}:8080/v1/neo4j/languages"
curl -i -k -X POST https://api.paion-data.dev:8444/services/wilhelm-ws-languages/routes \
  --data "paths[]=/wilhelm/languages" \
  --data name=wilhelm-ws-languages

# expand
curl -v -i -s -k -X POST https://api.paion-data.dev:8444/services \
  --data name=wilhelm-ws-expand \
  --data url="http://${GATEWAY_PUBLIC_IP}:8080/v1/neo4j/expand"
curl -i -k -X POST https://api.paion-data.dev:8444/services/wilhelm-ws-expand/routes \
  --data "paths[]=/wilhelm/expand" \
  --data name=wilhelm-ws-expand

# search
curl -v -i -s -k -X POST https://api.paion-data.dev:8444/services \
  --data name=wilhelm-ws-search \
  --data url="http://${GATEWAY_PUBLIC_IP}:8080/v1/neo4j/search"
curl -i -k -X POST https://api.paion-data.dev:8444/services/wilhelm-ws-search/routes \
  --data "paths[]=/wilhelm/search" \
  --data name=wilhelm-ws-search
```

We should see `HTTP/1.1 201 Created` as signs of success.

#### Example requests:

- vocabulary count: https://api.paion-data.dev/wilhelm/languages/german?perPage=100&page=1
- query vocabulary paged: https://api.paion-data.dev/wilhelm/languages/german/count
- expand: https://api.paion-data.dev/wilhelm/expand/nämlich
- search: https://api.paion-data.dev/wilhelm/search/das

License
-------

The use and distribution terms for [Aristotle]() are covered by the [Apache License, Version 2.0].

[Apache License Badge]: https://img.shields.io/badge/Apache%202.0-F25910.svg?style=for-the-badge&logo=Apache&logoColor=white
[Apache License, Version 2.0]: https://www.apache.org/licenses/LICENSE-2.0

[Java Version Badge]: https://img.shields.io/badge/Java-17-brightgreen?style=for-the-badge&logo=OpenJDK&logoColor=white
[JAX-RS]: https://jcp.org/en/jsr/detail?id=370
[JSR 370]: https://jcp.org/en/jsr/detail?id=370
