# Java web crawler micro-framework

[![Build Status](https://travis-ci.org/amage/webcrawler.svg?branch=master)](https://travis-ci.org/amage/webcrawler)
[![Coverage Status](https://coveralls.io/repos/github/amage/webcrawler/badge.svg?branch=master)](https://coveralls.io/github/amage/webcrawler?branch=master)
[![Gitter](https://badges.gitter.im/amage/webcrawler.svg)](https://gitter.im/amage/webcrawler?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

``` WARNING!!! Don't use it in production. ```


## Features

* simple http client with cookie support
* cache everything for tune parser without request to serever

## How to build

1. checkout last sources
2. run gradle

```
$ ./gradlew
```

## Quick start
Add webcrawler to project dependencies.
gradle:
```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
dependencies {
    ...
    compile('com.github.amage:webcrawler:-SNAPSHOT')
}
```

```java
WebClient web = new WebClient();
Document doc = web.go("http://google.com");
```
Document is org.jsoup.nodes.Document JSoup class.


   
