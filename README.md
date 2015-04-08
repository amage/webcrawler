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
```java
WebClientAgent web = new WebClientAgent();
Document doc = web.go("http://google.com");
```
Document is org.jsoup.nodes.Document JSoup class.

## Test mode (TODO)

In this mode webcrawler use pre-downloaded pages to run unit-tests.
