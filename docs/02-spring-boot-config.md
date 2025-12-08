# Module 2: Spring Boot Configuration

Now for the interesting part. In this module, you'll configure your Spring Boot application to talk to the Ignite cluster.

## Step 5: Add Dependencies

Open `pom.xml` and add the following dependencies:

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

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | Embedded Tomcat server and Spring MVC for REST endpoints |
| `spring-boot-starter-data-jdbc` | Spring Data JDBC framework for repository support |
| `spring-boot-starter-test` | JUnit, assertion libraries, and Spring test utilities |
| `spring-data-ignite` | Ignite SQL dialect so Spring Data generates compatible queries |
| `spring-boot-starter-ignite-client` | Auto-configures an Ignite thin client from properties |

Note that we're using Spring Data **JDBC**, not Spring Data **JPA**. JPA (Hibernate) adds an ORM layer with caching, lazy loading, and entity state management. Spring Data JDBC is simpler: it maps objects to tables without the ORM complexity. For Ignite, this direct SQL approach is a better fit.

**Using GridGain instead?** Update the `ignite.project` property to `org.gridgain` and `ignite.version` to your GridGain version (e.g., `9.1.8`).

## Step 6: Configure the SQL Dialect

When Spring Data generates SQL for operations like pagination, identity columns, or certain functions, it needs to know which database it's talking to. PostgreSQL handles `LIMIT` differently than Oracle. MySQL's quoting rules differ from SQL Server's. These variations are called **SQL dialects**.

Spring Data JDBC needs to know how to generate SQL for Ignite. Create a file at `src/main/resources/META-INF/spring.factories` with this content:

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

Edit `Application.java` to inject the Ignite client and log some diagnostic information on startup.

Add this field to the class:

```java
@Autowired
private Ignite ignite;
```

The `@Autowired` annotation is Spring's dependency injection mechanism. Instead of creating the `Ignite` object yourself, you declare that you need one, and Spring provides it. The `spring-boot-starter-ignite-client` creates this bean automatically based on your `ignite.client.addresses` property.

Now add this method:

```java
private Logger log = LoggerFactory.getLogger(Application.class);

@EventListener(ApplicationReadyEvent.class)
public void startupLogger() {
    log.info("Table names existing in cluster: {}",
        ignite.tables().tables().stream().map(Table::name).toList());

    log.info("Node information:");
    for (var n : ignite.cluster().nodes()) {
        log.info("ID: {}, Name: {}, Address: {}", n.id(), n.name(), n.address());
    }
}
```

The `@EventListener` annotation tells Spring to call this method when a specific event occurs. `ApplicationReadyEvent` fires after the application has fully started: all beans are created, the web server is listening, and everything is ready for traffic. This is a good place for startup diagnostics because you know all your dependencies are available.

Run the application. You should see output showing the COUNTRY and CITY tables and the three cluster nodes. If you see connection errors, make sure your Docker containers are still running.

## What You've Done

Your Spring Boot application now:

- Has all required dependencies for Spring Data and Ignite integration
- Knows how to generate Ignite-compatible SQL
- Can connect via both the native thin client and JDBC
- Logs cluster information on startup to verify connectivity

## Next Module

Continue to [Module 3: Building Repositories](03-repositories.md) to create your first Spring Data repositories.
