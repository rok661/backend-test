
# DEMO money transfer backend test #
## Introduction
Application demonstrates API for money transfer between two accounts. Jetty + Jersey used for api implementation. H2 database is used to store account data in storage.
Key API defined in AccountsController.class.
API integration tests used:
* AccountsControllerMultiThreadTest - multithreaded test using Rest Assured to demonstrate safe transfers.
* AccountsControllerTransferTest - key transfer logic tests.
* AccountsController - some helper API tests used for other services than *transfer*.
Business logic key tests:
* LockManagerTest - checks locks manager functionality.
## How to run application

### How to compile
```$xslt
mvn clean install
```
### Tests
Running separately tests:
```$xslt
mvn test
```
### How to run Application manually
lt.revolut.backendtest.App class main method should be run.