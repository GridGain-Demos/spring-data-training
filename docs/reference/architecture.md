# Architecture Guide

This guide walks through the architecture of the Spring Boot + Spring Data + Apache Ignite integration. Whether you completed the instructor-led training or you're exploring on your own, this document helps you understand what each piece does and why it matters.

## The Big Picture

At its core, this application is a standard Spring Boot REST service. What makes it interesting is the database layer: instead of PostgreSQL or MySQL, you're talking to an Apache Ignite cluster. Ignite stores your data in memory, distributes it across multiple nodes, and executes SQL queries in parallel across the cluster.

The good news? Spring Data abstracts most of this complexity away. You write repository interfaces, Spring generates the implementation, and Ignite handles the distributed execution. The patterns you already know from Spring Data JPA or Spring Data JDBC work here too.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Spring Boot Application                          │
├─────────────────────────────────────────────────────────────────────────┤
│  WorldDatabaseController          REST endpoints (/api/mostPopulated)   │
│           │                                                             │
│           ▼                                                             │
│  CityRepository / CountryRepository    Spring Data repositories         │
│           │                                                             │
│           ▼                                                             │
│  Spring Data JDBC + IgniteDialectProvider    SQL generation             │
│           │                                                             │
├───────────┼─────────────────────────────────────────────────────────────┤
│           │                                                             │
│           ▼                                                             │
│  IgniteJdbcDriver (port 10800)         JDBC thin client connection      │
│  Ignite Client API (port 10800)        Native thin client connection    │
│                                                                         │
└───────────┼─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     Apache Ignite 3 Cluster                             │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐                              │
│  │  node1  │◄──►│  node2  │◄──►│  node3  │    Distributed SQL Engine    │
│  └─────────┘    └─────────┘    └─────────┘                              │
│        │              │              │                                  │
│        └──────────────┴──────────────┘                                  │
│                       │                                                 │
│              COUNTRY and CITY tables                                    │
│              (partitioned across nodes)                                 │
└─────────────────────────────────────────────────────────────────────────┘
```

## Project Structure

Here's what each file does:

```
src/main/java/com/gridgain/training/spring/
├── Application.java              Entry point, sets up Ignite client connection
├── WorldDatabaseController.java  REST endpoints that call the repositories
├── CityRepository.java           Spring Data repository with a custom SQL query
├── CountryRepository.java        Spring Data repository using query derivation
└── model/
    ├── City.java                 Entity class mapped to the CITY table
    └── Country.java              Entity class mapped to the COUNTRY table
```

## Two Ways to Talk to Ignite

The application connects to the Ignite cluster through two separate paths. This might seem redundant at first, but each serves a different purpose.

### The Thin Client Connection

The `spring-boot-starter-ignite-client` dependency auto-configures an Ignite thin client based on the `ignite.client.addresses` property. This gives you the native Ignite API:

```java
@Autowired
Ignite ignite;

// Access cluster topology
ignite.cluster().nodes();

// Introspect tables
ignite.tables().tables();
```

Use this when you need Ignite-specific features that go beyond SQL: cluster management, table metadata, compute operations, and so on.

### The JDBC Connection

Spring Data JDBC uses a standard JDBC DataSource configured via `spring.datasource.url`. The `IgniteJdbcDriver` translates JDBC calls into Ignite's thin client protocol.

```properties
spring.datasource.url=jdbc:ignite:thin://localhost:10800/
spring.datasource.driver-class-name=org.apache.ignite.jdbc.IgniteJdbcDriver
```

All your repository operations flow through this path. When you call `countryRepository.findById("USA")`, Spring Data generates SQL, the JDBC driver sends it to the cluster, and Ignite returns the result.

Both connections use port 10800, the thin client protocol port.

## The Glue: IgniteDialectProvider

Spring Data JDBC generates SQL for its operations, but different databases have different SQL dialects. PostgreSQL and MySQL handle pagination differently. Oracle has its own quirks with sequences.

The `IgniteDialectProvider` teaches Spring Data how to generate SQL that Ignite understands. You configure it in `META-INF/spring.factories`:

```properties
org.springframework.data.jdbc.repository.config.DialectResolver$JdbcDialectProvider=org.apache.ignite.data.IgniteDialectProvider
```

Without this, Spring Data would generate SQL that might not work correctly with Ignite's distributed SQL engine.

## Entity Mapping

Spring Data JDBC uses convention-based mapping. The class name determines the table, and field names map to columns. When the Java naming convention (camelCase) differs from the SQL naming convention (UPPERCASE or snake_case), use `@Column`:

```java
public class City {
    @Id
    private Integer id;          // Maps to ID column

    @Column(value = "COUNTRYCODE")
    private String countryCode;  // Maps to COUNTRYCODE column (not COUNTRY_CODE)

    private String name;         // Maps to NAME column
    private String district;     // Maps to DISTRICT column
    private Integer population;  // Maps to POPULATION column
}
```

The `@Id` annotation marks the primary key. Spring Data uses this to determine whether to INSERT or UPDATE when you call `save()`, and to implement `findById()` and `deleteById()`.

## Query Derivation

One of Spring Data's most useful features is query derivation. Instead of writing SQL, you declare a method with a descriptive name, and Spring generates the query:

```java
List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
```

Spring parses this method name into components:

| Component | Meaning |
|-----------|---------|
| `findBy` | SELECT query |
| `Population` | WHERE clause on the population column |
| `GreaterThan` | Comparison operator (>) |
| `OrderByPopulationDesc` | ORDER BY population DESC |

The generated SQL looks like:

```sql
SELECT * FROM country WHERE population > ? ORDER BY population DESC
```

This works great for simple queries. For anything involving JOINs, aggregations, or complex logic, you'll need `@Query`.

## Custom Queries with @Query

When query derivation isn't enough, write the SQL yourself:

```java
record PopulousCity(String cityName, Integer population, String countryName) {}

@Query("SELECT city.name as city_name, MAX(city.population) as population, " +
       "country.name as country_name FROM country " +
       "JOIN city ON city.countrycode = country.code " +
       "GROUP BY city.name, country.name, city.population " +
       "ORDER BY city.population DESC LIMIT :limit")
List<PopulousCity> findTopXMostPopulatedCities(int limit);
```

A few things to notice here:

**Record as return type**: When your query returns data from multiple tables, you need somewhere to put it. A Java record is perfect for this. Spring Data maps query result columns to record components based on name (with underscore-to-camelCase conversion).

**Column aliasing**: The `AS city_name` alias ensures the result column maps to the `cityName` record component.

**Named parameters**: The `:limit` syntax binds the method parameter to the query. Cleaner than positional parameters, especially when you have several.

**Distributed execution**: Ignite executes this query across the cluster. Each node processes its local data, then results are aggregated and returned.

## The Data Model

The World Database contains two related tables:

**COUNTRY** (239 rows)
- Primary key: `code` (3-letter ISO code like "USA", "IND", "BRA")
- Contains demographic and political data: population, surface area, government form, head of state

**CITY** (4079 rows)
- Primary key: `id` (integer)
- Foreign key: `countrycode` references COUNTRY.code
- Contains city data: name, district, population

The tables are created and loaded by `config/world.sql` during cluster initialization. This script also sets up an index on the foreign key to optimize JOIN performance.

## Request Flow

When a client calls `GET /api/mostPopulated?limit=5`:

1. Spring MVC routes the request to `WorldDatabaseController.getMostPopulatedCities()`
2. The controller calls `cityRepository.findTopXMostPopulatedCities(5)`
3. Spring Data JDBC executes the `@Query` SQL through the JDBC driver
4. The JDBC driver sends the query to the Ignite cluster via thin client protocol
5. Ignite's distributed SQL engine executes the query across all nodes
6. Results flow back through the same path
7. Spring serializes the `List<PopulousCity>` to JSON and returns it

The whole trip typically takes milliseconds because Ignite stores data in memory and executes queries in parallel.

## Scaling Considerations

The patterns in this training scale well:

- **More nodes**: Add nodes to increase storage capacity and query parallelism
- **More tables**: Add entity classes and repositories following the same patterns
- **More complex queries**: Use `@Query` for any SQL that Ignite supports
- **Production deployment**: The thin client protocol works the same in containers, VMs, or bare metal

The architecture stays the same whether you're querying thousands of rows or billions.

## Going Further

Now that you understand the basics, consider exploring:

- **Ignite's compute capabilities**: Run code on cluster nodes, close to the data
- **Transactions**: Ignite supports ACID transactions across partitions
- **Colocation**: Optimize JOINs by storing related data on the same node
- **Continuous queries**: Get notified when data changes

The [Apache Ignite documentation](https://ignite.apache.org/docs/latest/) and [GridGain documentation](https://docs.gridgain.com/) cover these topics in depth.
