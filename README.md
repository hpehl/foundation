# HAL Foundation

This repository contains the foundation for the next major version of the HAL management console and the upcoming OpenShift version halOS.

The work is in a very early state and very much in progress.

## Technical Stack

- [Java 11](https://jdk.java.net/java-se-ri/11)
- [J2CL](https://github.com/google/j2cl)
- [Crysknife CDI](https://github.com/crysknife-io/crysknife)
- [Elemento](https://github.com/hal/elemento)
- [PatternFly Java](https://github.com/patternfly-java)
- [Maven](https://maven.apache.org/) and [Parcel](https://parceljs.org/)

# Get Started

## Development mode

Run `./dev.sh` and wait until you see the message

```shell
[INFO] -----  Build Complete: ready for browser refresh  -----
```

Then in another shell run `cd console/op && yarn run watch`. This will open a browser at http://localhost:1234. Any changes to HTML, CSS, JavaScript and Java code is detected and the browser is reloaded automatically.

## Production mode

Run `./prod.sh`. This will compile the application and open a browser at http://localhost:3000.

# Contributing

This is an open source project. That means that everybody can contribute. It's not hard to get started. So start [contributing](CONTRIBUTING.md) today!

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
