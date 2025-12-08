# Spring Boot and Spring Data with Apache Ignite

A hands-on training project for integrating Spring Boot and Spring Data with Apache Ignite 3 (or GridGain 9). Build a RESTful web service backed by a distributed in-memory database.

## About This Project

This repository supports a [two-hour instructor-led training session](https://www.gridgain.com/products/services/training/apache-ignite-spring-boot-and-spring-data-development) for Java developers and architects. The training is also designed for self-paced learning.

**What you'll build**: A Spring Boot application that queries a distributed database through Spring Data repositories. The application demonstrates query derivation, custom SQL with JOINs, and REST endpoint exposure.

## Quick Start

```bash
# Clone the repository
git clone https://github.com/GridGain-Demos/spring-data-training.git
cd spring-data-training

# Start the Ignite cluster
docker compose -f docker-compose.yml up -d

# Initialize the cluster (in the Ignite CLI)
docker run -v ./config/world.sql:/opt/ignite/downloads/world.sql \
  --rm --network spring-boot-data-training_default \
  -it apacheignite/ignite:3.1.0 cli

# Then run these commands in the CLI:
# connect http://node1:10300
# cluster init --name=spring-data-training --metastorage-group=node1,node2
# sql --file=/opt/ignite/downloads/world.sql
```

See the [Training Overview](docs/training-overview.md) for complete instructions.

## Prerequisites

- Java 17 or later
- Maven 3.6+
- Docker Desktop
- An IDE (IntelliJ IDEA, VS Code, etc.)

## Documentation

### Training Modules

Work through these modules in order:

1. [Cluster Setup](docs/01-cluster-setup.md) - Start the Ignite cluster and load sample data
2. [Spring Boot Configuration](docs/02-spring-boot-config.md) - Configure dependencies and connections
3. [Building Repositories](docs/03-repositories.md) - Create Spring Data repositories
4. [REST API](docs/04-rest-api.md) - Expose data through HTTP endpoints

### Reference

- [Architecture Guide](docs/reference/architecture.md) - How the application components fit together
- [Training Overview](docs/training-overview.md) - Prerequisites, structure, and learning objectives

## Project Structure

```
├── docs/                     Training modules and reference documentation
├── config/
│   └── world.sql            Sample data (countries and cities)
├── src/main/java/.../
│   ├── Application.java     Spring Boot entry point
│   ├── model/               Entity classes (City, Country)
│   ├── *Repository.java     Spring Data repositories
│   └── *Controller.java     REST endpoints
└── docker-compose.yml       Three-node Ignite cluster
```

## Technology Stack

| Component | Version |
|-----------|---------|
| Apache Ignite | 3.1.0 |
| Spring Boot | 3.x |
| Spring Data JDBC | 3.x |
| Java | 17+ |

For GridGain 9, update the Maven properties as described in the training modules.

## Running Tests

With the cluster running:

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
