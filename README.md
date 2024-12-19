Wilhelm Webservice
==================

![Java Version Badge][Java Version Badge]
[![Apache License Badge]][Apache License, Version 2.0]

__wilhelm-ws__ is a [JSR 370] [JAX-RS] webservice exclusively serving [wilhelmlang.com](https://wilhelmlang.com/)

Development
-----------

### Getting Source Code

```console
git@github.com:QubitPi/wilhelm-ws.git
cd wilhelm-ws
```

#### Running Tests

```console
mvn clean verify
```

### Generating WAR file

wilhelm-ws favors running in an external Jetty container, so its executable is not a fat JAR but a WAR which can be
compiled up with

```console
mvn clean package
```

### Starting Webservice Locally

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

### Deployment

wilhelm-ws has a dedicated release definition called
[wilhelm-ws-release-definition](https://github.com/QubitPi/wilhelm-ws-release-definition) for its automated deployment.
Please check that our for details.

License
-------

The use and distribution terms for [wilhelm-ws]() are covered by the [Apache License, Version 2.0].

[Apache License Badge]: https://img.shields.io/badge/Apache%202.0-F25910.svg?style=for-the-badge&logo=Apache&logoColor=white
[Apache License, Version 2.0]: https://www.apache.org/licenses/LICENSE-2.0

[Java Version Badge]: https://img.shields.io/badge/Java-17-brightgreen?style=for-the-badge&logo=OpenJDK&logoColor=white
[JAX-RS]: https://jcp.org/en/jsr/detail?id=370
[JSR 370]: https://jcp.org/en/jsr/detail?id=370
