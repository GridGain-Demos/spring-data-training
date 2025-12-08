# Spring Boot and Spring Data with Apache Ignite

A hands-on training template for integrating Spring Boot and Spring Data with Apache Ignite 3 (or GridGain 9). Build a RESTful web service backed by a distributed in-memory database.

## About This Project

This repository is a **project template** that supports a [two-hour instructor-led training session](https://www.gridgain.com/products/services/training/apache-ignite-spring-boot-and-spring-data-development) for Java developers and architects. You start with a minimal Spring Boot application and build it up by following the training modules.

**What you'll build**: A Spring Boot application that queries a distributed database through Spring Data repositories. The application demonstrates query derivation, custom SQL with JOINs, and REST endpoint exposure.

**How it works**: The template provides entity classes and Docker configuration. You add the Spring Data dependencies, configuration files, repositories, and controllers by following step-by-step instructions in the documentation.

## Prerequisites

Before starting, make sure you have:

- **Java 17 or later** (Spring Boot 3.x requires Java 17)
- **Maven 3.6+**
- **Docker Desktop**
- **An IDE** (IntelliJ IDEA, VS Code, etc.)
- **A REST client** (browser, curl, httpie, or Postman)

## Getting Started

Start with [Module 1: Cluster Setup](docs/01-cluster-setup.md), which walks you through cloning the repository, starting the Ignite cluster, and loading sample data.

## Training Modules

Work through these modules in order:

| Module | What You'll Do |
|--------|----------------|
| [1. Cluster Setup](docs/01-cluster-setup.md) | Start a three-node Ignite cluster with Docker and load sample data |
| [2. Spring Boot Configuration](docs/02-spring-boot-config.md) | Add dependencies, configure connections, verify connectivity |
| [3. Building Repositories](docs/03-repositories.md) | Create Spring Data repositories with query derivation and custom SQL |
| [4. REST API](docs/04-rest-api.md) | Expose your data through REST endpoints |

Each module takes roughly 20-30 minutes, depending on your familiarity with the technologies involved.

## Project Structure

The template provides the foundation. You build on it during the training.

```
├── docs/                     Training modules (your guide)
├── config/
│   └── world.sql            Sample data (countries and cities)
├── src/main/java/.../
│   ├── Application.java     Spring Boot entry point (you'll modify this)
│   └── model/
│       ├── City.java        Entity class (provided)
│       └── Country.java     Entity class (provided)
├── src/test/java/.../
│   └── ApplicationTests.java  Test class (you'll add tests here)
├── pom.xml                  Maven config (you'll add dependencies)
└── docker-compose.yml       Three-node Ignite cluster
```

**Files you'll create during the training:**
- `CountryRepository.java` - Spring Data repository with derived queries
- `CityRepository.java` - Spring Data repository with custom SQL
- `WorldDatabaseController.java` - REST controller
- `META-INF/spring.factories` - SQL dialect configuration

## Technology Stack

| Component | Version |
|-----------|---------|
| Apache Ignite | 3.1.0 |
| Spring Boot | 3.x |
| Spring Data JDBC | 3.x |
| Java | 17+ |

For GridGain 9, update the Maven properties as described in the training modules.

## Reference Material

- [Training Overview](docs/training-overview.md) - Learning objectives and prerequisites
- [Architecture Guide](docs/reference/architecture.md) - How the completed application components fit together

## Running Tests

After completing the training modules, with the cluster running:

```bash
mvn compile test
```

## Shutting Down

```bash
docker compose -f docker-compose.yml down
```

## Issues and Feedback

If you find issues or have suggestions, please open an Issue or PR on this repository. For training-related questions, contact [GridGain Training](https://www.gridgain.com/products/services/training).

## License

This project is provided for educational purposes as part of GridGain's training program.
