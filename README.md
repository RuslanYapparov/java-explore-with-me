[По-русски](readme_files%2FREADME_rus.md)

# Explore with me

---

*The backend of a service that allows users to share information about interesting events and find a company to participate in them.*

---

It's not an easy task to organize an event that requires gathering a group of friends or simply interested people . 
This application can help to aggregate information about an upcoming event, understand how popular it is, gather 
a company to hold it, evaluate the idea and its realization.

Application frontend design option:

![frontend_variant.jpg](readme_files%2Ffrontend_variant.jpg)

---

This application has three levels of user interaction:

Public - available without registration to any network user, provides the following abilities:

1. View a list of events with search and filtering support (number of views, event date, category and compilations of events);
2. View event details.

Closed - available only to authorized users, provides the following features:

1. Add to application new events, edit them and view them after adding;
2. Evaluate the idea of an event (that has not yet happened) or how it was carried out;
3. Add a request for participation in event;
4. (for event organizers) Confirm request submitted by other users.

Administrative - available to administrators, provides the ability to configure and support the service:

1. User management - adding, activating, viewing and deleting;
2. Moderation of events posted by users - publication or rejection;
3. Manage compilations - add, delete and pin compilations of events on the main page;
4. Manage categories - add, change and delete categories for events.

---

Java version - 11;

The application is based on the Spring Boot v. 2.7.9;

Build system - Apache Maven;

Database - PostgreSQL;

Accessing the database and mapping entities - spring-boot-starter-data-jpa, hibernate;

Testing -JUnit, Mockito;

Containerization system - Docker + docker-compose.

The service is divided into 2 modules:

*stats_service* - stores view statistics in its own database (based on the number of user clicks on event links), 
allows you to obtain samples for analyzing the operation of the application, contains a client submodule for handy integration.

*main_service* - contains all product business logic, accepts user requests, validates and processes them, 
accesses the main database to provide answers to the user.

---

Instructions for running the application locally:

Software required to run the application
- Git (installation guide option - https://learn.microsoft.com/ru-ru/devops/develop/git/install-and-set-up-git);
- JDK (java SE11+, version of the installation guide - https://blog.sf.education/ustanovka-jdk-poshagovaya-instrukciya-dlya-novichkov/);
- Apache Maven (version of installation guide on Windows -https://byanr.com/installation-guides/maven-windows-11/);
- Docker (& docker-compose) - to work in a Windows environment you will need a virtual machine running Linux - 
a version of the guide for installing it - https://learn.microsoft.com/ru-ru/windows/wsl/install.
After launch, the application will accept http requests in accordance with the API (see below) on port 8080 (http://localhost:8080/), 
and also send data to the statistics service module on port 9090 (http://localhost:9090 /), make sure that the ports are free, 
otherwise you will need to change the corresponding settings in the application.properties and docker-compose.yml files

Launch terminal/command line/PowerShell, execute the commands one by one, waiting for each one to complete:

```
cd {destination directory to download the project}

git clone git@github.com:RuslanYapparov/java-explore-with-me.git

cd java-explore-with-me/

mvn package

docker-compose up
```

To run a test script, you can use the test collection (see below).

---
API Description(OpenAPI):

stats service - [ewm-stats-service-spec.json](readme_files%2Fewm-stats-service-spec.json)

main service - [ewm-main-service-spec.json](readme_files%2Fewm-main-service-spec.json)

to view you need to copy and open the content in Swagger editor

---
Postman Test Collection:

stats service - [explore-with-me_stats-service-test_postman-collection.json](readme_files%2Fexplore-with-me_stats-service-test_postman-collection.json)

main service - [explore-with-me_main-service-test_postman-collection.json](readme_files%2Fexplore-with-me_main-service-test_postman-collection.json)

functionality of likes and ratings - [explore-with-me_rating-feature-test_postman-collection.json](readme_files%2Fexplore-with-me_rating-feature-test_postman-collection.json)

import a collection by copying the contents into the field as Raw text

---

The application database is designed in accordance with the ER diagram (created using dbdiagram.io):
![explore-with-me_er-diagram.jpg](readme_files%2Fexplore-with-me_er-diagram.jpg)
---

The application is written in Java. Sample code:
```java
public class ExploreWithMe { 

    public static void main(String[] args) { 
        System.out.println("Let's start attracting people to our events!"); 
    }

}
```