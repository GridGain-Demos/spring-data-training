# Module 2: Spring Boot Configuration

Now for the interesting part. In this module, you'll configure your Spring Boot application to talk to the Ignite cluster.

## Step 5: Add Dependencies

Open `pom.xml` and add the following dependencies inside the `<dependencies>` section (after the existing Ignite dependencies):

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>${ignite.project}</groupId>
  <artifactId>spring-data-ignite</artifactId>
  <version>${ignite.version}</version>
</dependency>

<dependency>
  <groupId>${ignite.project}</groupId>
  <artifactId>spring-boot-starter-ignite-client</artifactId>
  <version>${ignite.version}</version>
</dependency>
```

Here's what each dependency provides:

| Dependency                          | Purpose                                                        |
|-------------------------------------|----------------------------------------------------------------|
| `spring-boot-starter-web`           | Embedded Tomcat server and Spring MVC for REST endpoints       |
| `spring-boot-starter-data-jdbc`     | Spring Data JDBC framework for repository support              |
| `spring-boot-starter-test`          | JUnit, assertion libraries, and Spring test utilities          |
| `spring-data-ignite`                | Ignite SQL dialect so Spring Data generates compatible queries |
| `spring-boot-starter-ignite-client` | Auto-configures an Ignite thin client from properties          |

Note that we're using Spring Data **JDBC**, not Spring Data **JPA**. JPA (Hibernate) adds an ORM layer with caching, lazy loading, and entity state management. Spring Data JDBC is simpler: it maps objects to tables without the ORM complexity. For Ignite, this direct SQL approach is a better fit.

**Using GridGain instead?** Update the `ignite.project` property to `org.gridgain` and `ignite.version` to your GridGain version (e.g., `9.1.8`).

## Step 6: Configure the SQL Dialect

When Spring Data generates SQL for operations like pagination, identity columns, or certain functions, it needs to know which database it's talking to. PostgreSQL handles `LIMIT` differently than Oracle. MySQL's quoting rules differ from SQL Server's. These variations are called **SQL dialects**.

Spring Data JDBC needs to know how to generate SQL for Ignite. Create the directory structure and file at `src/main/resources/META-INF/spring.factories` with this content:

```properties
org.springframework.data.jdbc.repository.config.DialectResolver$JdbcDialectProvider=org.apache.ignite.data.IgniteDialectProvider
```

The `IgniteDialectProvider` teaches Spring Data the specifics of Ignite's SQL syntax. Without this configuration, Spring Data would fall back to generic ANSI SQL, which might work for simple queries but could fail for anything database-specific.

## Step 7: Configure Connection Properties

Update `src/main/resources/application.properties` with the connection settings:

```properties
ignite.client.addresses=127.0.0.1:10800
spring.datasource.url=jdbc:ignite:thin://localhost:10800/
spring.datasource.driver-class-name=org.apache.ignite.jdbc.IgniteJdbcDriver
```

You might notice this configures two separate connections to the same cluster. That's intentional:

**The thin client connection** (`ignite.client.addresses`) gives you access to Ignite's native Java API. You can query cluster topology, inspect table metadata, run compute tasks, and use features that don't exist in SQL. The `spring-boot-starter-ignite-client` reads this property and auto-configures an `Ignite` bean you can inject anywhere.

**The JDBC connection** (`spring.datasource.*`) is what Spring Data uses for all repository operations. When you call `repository.findById()` or run a custom `@Query`, Spring Data generates SQL and sends it through this JDBC driver.

Both connections use port 10800, the thin client protocol port. The JDBC driver is essentially a thin client that speaks SQL instead of the native binary protocol.

## Step 8: Verify the Connection

Edit `Application.java` to inject the Ignite client and add repository support. Replace the entire file contents with:

```java
package com.gridgain.training.spring;

import org.apache.ignite.Ignite;
import org.apache.ignite.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Spring Boot entry point for the World Database application.
 *
 * This class demonstrates three key integration points between Spring Boot and Apache Ignite 3:
 *
 * 1. {@code @SpringBootApplication} - Standard Spring Boot bootstrap that triggers component scanning
 *    and auto-configuration. The spring-boot-starter-ignite-client dependency enables automatic
 *    creation of an Ignite thin client connection.
 *
 * 2. {@code @EnableJdbcRepositories} - Activates Spring Data JDBC repository support. Combined with
 *    the IgniteDialectProvider configured in META-INF/spring.factories, this allows Spring Data
 *    repositories to generate SQL compatible with Ignite's distributed SQL engine.
 *
 * 3. {@code Ignite} injection - The thin client instance is auto-configured by
 *    spring-boot-starter-ignite-client based on the ignite.client.addresses property. This client
 *    provides access to the Tables API and Cluster API for direct Ignite operations.
 *
 * The application uses two separate connection mechanisms:
 * - Thin client (port 10800) for Ignite-native APIs (tables, cluster topology)
 * - JDBC driver (same port) for Spring Data repository operations
 */
@SpringBootApplication
@EnableJdbcRepositories
public class Application {

	/**
	 * Ignite thin client instance, auto-configured by spring-boot-starter-ignite-client.
	 * Connection address is specified via ignite.client.addresses in application.properties.
	 */
	@Autowired
	Ignite ignite;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	private Logger log = LoggerFactory.getLogger(Application.class);

	/**
	 * Diagnostic logging executed after Spring context initialization completes.
	 * Verifies cluster connectivity by querying table metadata and node topology.
	 *
	 * The Tables API (ignite.tables()) provides schema introspection for all tables
	 * in the cluster. The Cluster API (ignite.cluster()) exposes topology information
	 * including node identifiers and network addresses.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void startupLogger() {
		log.info("Table names existing in cluster: {}", ignite.tables().tables().stream().map(Table::name).toList());

		log.info("Node information:");
		for (var n : ignite.cluster().nodes()) {
			log.info("ID: {}, Name: {}, Address: {}", n.id(), n.name(), n.address());
		}
	}

}
```

The `@Autowired` annotation is Spring's dependency injection mechanism. Instead of creating the `Ignite` object yourself, you declare that you need one, and Spring provides it. The `spring-boot-starter-ignite-client` creates this bean automatically based on your `ignite.client.addresses` property.

The `@EventListener` annotation tells Spring to call the `startupLogger()` method when `ApplicationReadyEvent` fires. This event occurs after the application has fully started: all beans are created, the web server is listening, and everything is ready for traffic. This is a good place for startup diagnostics because you know all your dependencies are available.

The `@EnableJdbcRepositories` annotation activates Spring Data JDBC repository support. This is required for Spring to discover and generate implementations for your repository interfaces in the next module.

Run the application. You should see output showing the COUNTRY and CITY tables and the three cluster nodes. If you see connection errors, make sure your Docker containers are still running.

## What You've Done

Your Spring Boot application now:

- Has all required dependencies for Spring Data and Ignite integration
- Knows how to generate Ignite-compatible SQL
- Can connect via both the native thin client and JDBC
- Logs cluster information on startup to verify connectivity

## Next Module

Continue to [Module 3: Building Repositories](03-repositories.md) to create your first Spring Data repositories.
