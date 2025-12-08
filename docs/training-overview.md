# Training Overview

Welcome to the Spring Boot and Spring Data with Apache Ignite training. Over the next couple of hours, you'll build a RESTful web service that uses Apache Ignite as its database layer.

## What You'll Build

By the end of this training, you'll have a working Spring Boot application that:

- Connects to a three-node Apache Ignite cluster
- Uses Spring Data repositories for database operations
- Generates queries automatically from method names
- Executes custom SQL with JOINs across distributed tables
- Exposes data through REST endpoints

The application queries a World Database containing countries and cities, demonstrating how Spring Data patterns work with a distributed in-memory database.

## Training Structure

The training is divided into four modules, each building on the previous:

| Module                                                   | What You'll Do                                                       |
|----------------------------------------------------------|----------------------------------------------------------------------|
| [1. Cluster Setup](01-cluster-setup.md)                  | Start a three-node Ignite cluster with Docker and load sample data   |
| [2. Spring Boot Configuration](02-spring-boot-config.md) | Configure dependencies, connections, and verify connectivity         |
| [3. Building Repositories](03-repositories.md)           | Create Spring Data repositories with query derivation and custom SQL |
| [4. REST API](04-rest-api.md)                            | Expose your data through REST endpoints                              |

Each module takes roughly 20-30 minutes, depending on your familiarity with the technologies involved.

## Prerequisites

Before starting, make sure you have:

- **Git** for cloning the repository
- **Docker Desktop** for running the Ignite cluster
- **Java 17 or later** (Spring Boot 3.x requires Java 17)
- **Maven 3.6+** for building and testing
- **An IDE** like IntelliJ IDEA or VS Code
- **Something to test REST endpoints**: a browser, curl, httpie, or Postman

## Who This Training Is For

This training assumes you're comfortable with Java and have some familiarity with Spring Boot. You don't need prior experience with Apache Ignite or distributed systems.

If you're an experienced Ignite developer but new to Spring, the modules explain Spring concepts as they come up. If you're a Spring developer new to Ignite, the training focuses on the integration points rather than Ignite internals.

## Reference Material

As you work through the modules, you may want to consult:

- [Architecture Guide](reference/architecture.md): How the application components fit together
- [Apache Ignite Documentation](https://ignite.apache.org/docs/latest/)
- [Spring Data JDBC Reference](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/)

## Getting Started

Ready to begin? Start with [Module 1: Cluster Setup](01-cluster-setup.md).
