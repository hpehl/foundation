This repository contains the foundation for the next major version of the HAL management console and the upcoming OpenShift version halOS.

The work is in a very early state and very much in progress.

# Technical Stack

- [Java 11](https://jdk.java.net/java-se-ri/11)
- [J2CL](https://github.com/google/j2cl)
- [Crysknife CDI](https://github.com/crysknife-io/crysknife)
- [Elemento](https://github.com/hal/elemento)
- [PatternFly Java](https://github.com/patternfly-java)
- [Maven](https://maven.apache.org/) and [Parcel](https://parceljs.org/)

# Get Started

## Development mode

In the root folder, run

```shell
mvn j2cl:watch -P op
```

and wait until you see the message

```
[INFO] -----  Build Complete: ready for browser refresh  -----
```

In another shell run

```shell
cd console/op
yarn run watch
```

This will open a browser at http://localhost:1234 and watch for changes to your HTML, CSS, JavaScript, and Java code. Each time a change is made, the browser reloads the page automatically.

## Production mode

In the root folder, run

```shell
mvn package -P op,prod
```

This will compile the application in production mode to `console/op/target/console`.

You can use any HTTP server you want to serve the files from this folder. The project contains configuration to start an instance of the [http-server](https://www.npmjs.com/package/http-server) package. Use it by running

```shell
cd console/op
mvn com.github.eirslett:frontend-maven-plugin:yarn@http-server
```

This will open a browser at http://localhost:8080.

# Contributing

This is an open source project. That means that everybody can contribute. It's not hard to get started. So start [contributing](CONTRIBUTING.md) today!

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
