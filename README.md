![](mediminder-fe/src/assets/icons/icon-192x192.png)

# Mediminder

This project was developed for fun and contains a Spring Boot backend with an Angular frontend.
The goal of the application is to provide a mobile web application to make it easier to manage the medication you have in your cabinet and at which times you have to take them.

Existing apps usually try to solve either the intake schedule or the inventory management, but not both.
This application does both by updating the inventory as soon as you take your medication.
The benefit is that it provides more integrated features for the user, such as:

* You get notified when your medication is about to expire
* You get notified when you need to take your medication
* You get notified when you're about to run out of medication
* You can use the planner to see how much medication you're missing until a given date
* ...

## Getting started

To start the applciation, there are three prerequisites:

* Docker must be installed (not necessary if you manually set up your environment)
* JDK LTS v21.x must be installed
* Node.js LTS v22.x must be installed

To run the application, do the following:

```
./mediminder-fe/build.sh
./mediminder-api/build.sh
docker-compose up
```

If you want to run the application in development mode, you can use the following commands to run the backend:

```
cd mediminder-api/
./mvnw spring-boot:run
```

And following commands to run the frontend:

```
cd mediminder-fe/
npm run start
```
