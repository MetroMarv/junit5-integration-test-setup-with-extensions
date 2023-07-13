# JUnit5 Integration Test Setup with Extensions

In this repository we demonstrate how to use JUnit 5 extensions for test setup in integration tests. We show how
to use a `PropertyResolver` to inject parameters into test methods and also how to use a lifecycle callback to
do the setup.

This repo was created for a blog article published here
[Conciso Wissensbeiträge (GER)](https://conciso.de/wissensbeitraege/).

## Example context

The context of this application is a simple webshop written in Java using Spring Boot. It comes with an endpoint to
manage a shopping cart. It uses a relational database for persistence and Keycloak for authentication.

For this application we want to create integration tests using TestContainers and JUnit 5 extensions.

## Integration test setup

As we'd like to use JUnit 5 extensions for integration test setup, we do not focus on the particular test, but on its
setup.

We show how to use JUnit 5 `ParameterResolver` for the test setup in the package
`de.conciso.junit5extensionsforintegrationtests.parameterresolver`, how to use a parameterized `ParameterResolver` in
the package `de.conciso.junit5extensionsforintegrationtests.parameterresolverwithparams` and how to use JUnit lifecycle
callbacks in the package `de.conciso.junit5extensionsforintegrationtests.lifecyclehooks`.

## How to execute tests

In order to run this project you're required to have a Docker environment installed on your system.

To run the tests in this project execute `./mvnw test`.