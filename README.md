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

## Persistence/Generative Crowling (TODO)

PGC is layer on top of WebClient. Main idea is that each request can generate set of new ones. Also we need to persist state or requests to be able to handle timeout errors.
There will be interface to detect type of page and process it.
```
IPageHandler
   // To find all handlers for this page.
   boolean isFit(page)
   // Process data and creete new transactions.
   List<Transaction> processPage(page)
```

To process PGC will take these steps:

1. Load initial transaction data and seeds (start urls)
2. Get transaction
3. If download is OK change it state NEW -> PROC
4. Find all fits handlers and execute them
5. Change state PROC->COMPLETE
6. Goto 2 until unprocessed transaction exists
   
