Aristotle
=========

![Java Version Badge][Java Version Badge]
![HashiCorp Packer Badge][HashiCorp Packer Badge]
![HashiCorp Terraform Badge][HashiCorp Terraform Badge]
[![Apache License Badge]][Apache License, Version 2.0]

Aristotle is a [JSR 370] [JAX-RS] webservice of CRUD operations against a graph database. It supports Neo4J now.

Configuration
-------------

- `NEO4J_URI`
- `NEO4J_USERNAME`
- `NEO4J_PASSWORD`
- `NEO4J_DATABASE`

Test
----

```console
mvn clean verify
```

Deployment
----------

```bash
mvn clean package
```

### Gateway Registration

```bash
export GATEWAY_PUBLIC_IP=52.53.186.26

curl -v -i -s -k -X POST https://api.paion-data.dev:8444/services \
  --data name=wilhelm-ws-languages \
  --data url="http://${GATEWAY_PUBLIC_IP}:8080/v1/data/languages"
curl -i -k -X POST https://api.paion-data.dev:8444/services/wilhelm-ws-languages/routes \
  --data "paths[]=/wilhelm/languages" \
  --data name=wilhelm-ws-languages

curl -v -i -s -k -X POST https://api.paion-data.dev:8444/services \
  --data name=wilhelm-ws-expand \
  --data url="http://${GATEWAY_PUBLIC_IP}:8080/v1/data/expand"
curl -i -k -X POST https://api.paion-data.dev:8444/services/wilhelm-ws-expand/routes \
  --data "paths[]=/wilhelm/expand" \
  --data name=wilhelm-ws-expand
```

We should see `HTTP/1.1 201 Created` as signs of success.

#### Example requests:

- https://api.paion-data.dev/wilhelm/languages/german?perPage=100&page=1
- https://api.paion-data.dev/wilhelm/expand/nämlich

License
-------

The use and distribution terms for [Aristotle]() are covered by the [Apache License, Version 2.0].

[Apache License Badge]: https://img.shields.io/badge/Apache%202.0-F25910.svg?style=for-the-badge&logo=Apache&logoColor=white
[Apache License, Version 2.0]: https://www.apache.org/licenses/LICENSE-2.0

[HashiCorp Packer Badge]: https://img.shields.io/badge/Packer-02A8EF?style=for-the-badge&logo=Packer&logoColor=white
[HashiCorp Terraform Badge]: https://img.shields.io/badge/Terraform-7B42BC?style=for-the-badge&logo=terraform&logoColor=white

[Java Version Badge]: https://img.shields.io/badge/Java-17-brightgreen?style=for-the-badge&logo=OpenJDK&logoColor=white
[JAX-RS]: https://jcp.org/en/jsr/detail?id=370
[JSR 370]: https://jcp.org/en/jsr/detail?id=370
