# Java web crawler micro-framework

## Features

* cache
* cookie support in default agent

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

## Test mode (TODO)

In this mode webcrawler use pre-downloaded pages to run unit-tests.
