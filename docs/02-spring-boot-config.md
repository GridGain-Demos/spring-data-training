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

## Understanding Entity Classes

Now that you've confirmed the schema exists, let's look at how Java classes map to those tables. This is a key concept for working with Ignite 3 and Spring Data.

### Schema-First Development

Ignite 3 follows a **schema-first** approach. You define tables using SQL DDL, and then create Java classes that map to those tables. This is different from ORM frameworks like Hibernate, where you might define entities first and generate the schema from them.

The World Database schema was created in Module 1 when you ran `world.sql`. Here's what those table definitions look like:

```sql
CREATE TABLE Country (
  Code VARCHAR(3) PRIMARY KEY,
  Name VARCHAR,
  Continent VARCHAR,
  Region VARCHAR,
  SurfaceArea DECIMAL(10,2),
  IndepYear SMALLINT,
  Population INT,
  LifeExpectancy DECIMAL(3,1),
  GNP DECIMAL(10,2),
  GNPOld DECIMAL(10,2),
  LocalName VARCHAR,
  GovernmentForm VARCHAR,
  HeadOfState VARCHAR,
  Capital INT,
  Code2 VARCHAR(2)
);

CREATE TABLE City (
  ID INT,
  Name VARCHAR,
  CountryCode VARCHAR(3),
  District VARCHAR,
  Population INT,
  PRIMARY KEY (ID)
);

CREATE INDEX idx_country_code ON city (CountryCode);
```

The schema defines two tables with their column types, primary keys, and an index for JOIN performance. Your Java classes need to match this structure.

### SQL to Java Type Mapping

When translating SQL columns to Java fields, use these type mappings:

| SQL Type       | Java Type                    | Notes                                         |
|----------------|------------------------------|-----------------------------------------------|
| `VARCHAR`      | `String`                     |                                               |
| `INT`          | `Integer`                    | Use wrapper type to allow null                |
| `SMALLINT`     | `Short`                      |                                               |
| `DECIMAL(p,s)` | `BigDecimal`                 | Preserves precision for currency/measurements |
| `BOOLEAN`      | `Boolean`                    |                                               |
| `DATE`         | `LocalDate`                  |                                               |
| `TIMESTAMP`    | `Instant` or `LocalDateTime` |                                               |

For columns that might be NULL, use wrapper types (`Integer`, `Short`) rather than primitives (`int`, `short`). Primitives can't represent null values.

### Spring Data JDBC Annotations

Spring Data JDBC uses annotations to map Java classes to database tables. The template includes two entity classes in `src/main/java/com/gridgain/training/spring/model/`. Let's examine how they're built.

**The City entity** maps to the CITY table:

```java
package com.gridgain.training.spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public class City {
    @Id
    private Integer id;

    @Column(value = "COUNTRYCODE")
    private String countryCode;

    private String name;
    private String district;
    private Integer population;

    // Constructors, getters, setters, toString...
}
```

Key points:

- **`@Id`** marks the primary key field. Spring Data uses this for `findById()`, `deleteById()`, and to determine insert vs. update behavior.

- **`@Column`** maps a Java field to a SQL column when names don't match. The field `countryCode` (camelCase) needs to map to column `COUNTRYCODE` (no underscore). Without this annotation, Spring Data would look for a column named `COUNTRY_CODE`.

- **Fields without annotations** map by convention. The field `name` maps to column `NAME`, `district` to `DISTRICT`, and so on. Spring Data's convention is case-insensitive.

**The Country entity** demonstrates additional patterns:

```java
public class Country {
    @Id
    private String code;          // VARCHAR(3) -> String, natural key

    private String name;
    private String continent;
    private String region;
    private Integer population;   // INT -> Integer (nullable)

    @Column(value = "SURFACEAREA")
    private BigDecimal surfaceArea;   // DECIMAL(10,2) -> BigDecimal

    @Column("INDEPYEAR")
    private Short indepYear;          // SMALLINT -> Short

    @Column(value = "LIFEEXPECTANCY")
    private BigDecimal lifeExpectancy;

    // ... additional fields with @Column where needed
}
```

The Country entity shows:

- **Natural keys**: The primary key is `code` (a 3-letter country code like "USA"), not an auto-generated integer.

- **BigDecimal for precision**: Financial and measurement data uses `BigDecimal` to avoid floating-point rounding errors.

- **Multiple @Column annotations**: Any field with a camelCase name that differs from the SQL column name needs explicit mapping.

### What About Relationships?

You might notice that `City.countryCode` references `Country.code`, but there's no `@ManyToOne` or relationship annotation. Spring Data JDBC doesn't automatically resolve foreign key relationships like JPA does.

This is intentional. Spring Data JDBC favors explicit queries over implicit lazy loading. When you need data from both tables, write a JOIN query (you'll do this in Module 3). This approach is more predictable and works well with Ignite's distributed SQL engine.

### Ignite 3 Native Annotations

For completeness, Ignite 3 also provides its own annotation API in `org.apache.ignite.catalog.annotations`. These annotations serve a different purpose: **schema creation from code**.

```java
// Ignite 3 native annotations (for schema creation)
@Table(value = "my_table", zone = @Zone(value = "my_zone", replicas = 2))
class MyEntity {
    @Id
    Integer id;

    @Column(value = "full_name", length = 100)
    String name;
}

// Create the table from the annotated class
ignite.catalog().createTable(MyEntity.class);
```

The Ignite annotations include features like distribution zones, colocation, and indexes that are specific to distributed systems. However, for this training we use the schema-first approach: the tables already exist, and we use Spring Data annotations to map to them.

Both approaches are valid. Schema-first gives DBAs control over table design. Code-first with Ignite annotations works well when developers own the schema. Choose based on your team's workflow.

## What You've Done

Your Spring Boot application now:

- Has all required dependencies for Spring Data and Ignite integration
- Knows how to generate Ignite-compatible SQL
- Can connect via both the native thin client and JDBC
- Logs cluster information on startup to verify connectivity

You also understand:

- How Ignite 3's schema-first approach works
- How to map SQL types to Java types
- How Spring Data JDBC annotations connect Java classes to database tables
- Why the template's entity classes are structured the way they are

## Next Module

Continue to [Module 3: Building Repositories](03-repositories.md) to create your first Spring Data repositories.
