Aristotle
=========

![Java Version Badge][Java Version Badge]
![HashiCorp Packer Badge][HashiCorp Packer Badge]
![HashiCorp Terraform Badge][HashiCorp Terraform Badge]
[![Apache License Badge]][Apache License, Version 2.0]

Aristotle is a [JSR 370] [JAX-RS] webservice of CRUD operations against a graph database. It supports Neo4J now.

```bash
mvn clean verify
mvn clean package
```

Configuration
-------------

- `NEO4J_URI`
- `NEO4J_USERNAME`
- `NEO4J_PASSWORD`
- `NEO4J_DATABASE`

Deployment
----------

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
