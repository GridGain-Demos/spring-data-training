# Module 3: Building Repositories

With the connection working, you can start writing Spring Data repositories. This module covers the core of Spring Data: repository interfaces, query derivation, and custom SQL queries.

## What Are Spring Data Repositories?

If you've worked with databases in Java, you've probably written code like this:

```java
public List<Country> findCountriesWithPopulationOver(int population) {
    String sql = "SELECT * FROM country WHERE population > ? ORDER BY population DESC";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, population);
        ResultSet rs = stmt.executeQuery();
        List<Country> results = new ArrayList<>();
        while (rs.next()) {
            Country c = new Country();
            c.setCode(rs.getString("code"));
            c.setName(rs.getString("name"));
            c.setPopulation(rs.getInt("population"));
            // ... and so on for every field
            results.add(c);
        }
        return results;
    }
}
```

That's a lot of boilerplate for a simple query. Connection management, parameter binding, result set iteration, object mapping. Every query method looks roughly the same, just with different SQL and different field mappings.

Spring Data repositories flip this around. Instead of writing implementation code, you declare an interface:

```java
public interface CountryRepository extends CrudRepository<Country, String> {
    List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
}
```

That's it. No implementation class. Spring Data reads the method name, figures out what query you want, generates the SQL, executes it, and maps the results back to objects. You get `findById()`, `findAll()`, `save()`, and `delete()` for free just by extending `CrudRepository`.

**Why does this matter for Ignite developers?**

You could use Ignite's native APIs directly for all your data access. But Spring Data gives you three advantages:

1. **Familiar patterns**: If your team already uses Spring Data with PostgreSQL or MySQL, the same skills transfer. Same interfaces, same annotations, different backend.

2. **Less coupling**: Your repository interfaces don't mention Ignite anywhere. If you ever need to swap databases or run tests against a different store, your data access layer doesn't change.

3. **Focus on what matters**: The interesting part of Ignite is distributed storage and parallel query execution. Spring Data handles the mundane parts so you can focus on architecture.

## Step 9: Create CountryRepository

Create a new interface `CountryRepository.java` in the `com.gridgain.training.spring` package:

```java
@Repository
public interface CountryRepository extends CrudRepository<Country, String> {

}
```

That's it. You now have `findById()`, `findAll()`, `save()`, and `delete()` operations for countries. Spring Data generates the implementation at runtime.

## Step 10: Add a Derived Query

Add a method to find countries by population:

```java
List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
```

Spring Data parses this method name and generates the corresponding SQL. No implementation needed.

**How does query derivation work?**

Spring Data breaks the method name into keywords and builds a query:

| Keyword | SQL Equivalent | Example |
|---------|---------------|---------|
| `findBy` | `SELECT ... WHERE` | `findByName` |
| `And` | `AND` | `findByNameAndRegion` |
| `Or` | `OR` | `findByNameOrCode` |
| `GreaterThan` | `>` | `findByPopulationGreaterThan` |
| `LessThan` | `<` | `findByPopulationLessThan` |
| `Between` | `BETWEEN` | `findByPopulationBetween` |
| `Like` | `LIKE` | `findByNameLike` |
| `IsNull` | `IS NULL` | `findByCapitalIsNull` |
| `OrderBy...Asc/Desc` | `ORDER BY ... ASC/DESC` | `findByRegionOrderByNameAsc` |
| `First` / `Top` | `LIMIT` | `findFirstByOrderByPopulationDesc` |

So `findByPopulationGreaterThanOrderByPopulationDesc(int population)` becomes:

```sql
SELECT * FROM country WHERE population > ? ORDER BY population DESC
```

This works well for single-table queries with simple conditions. When you need JOINs, GROUP BY, or anything more complex, use `@Query` instead.

## Step 11: Test It

Add a test in `ApplicationTests.java` to verify the repository works.

First, add these static imports at the top of the file (this is the preferred style for JUnit 5):

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

Then add the repository field and test method:

```java
@Autowired
CountryRepository countryRepository;

@Test
void countryRepositoryWorks() {
    var results = countryRepository.findByPopulationGreaterThanOrderByPopulationDesc(100_000_000);
    log.info("count={}", results.size());
    assertFalse(results.isEmpty());
}
```

Note: `assertFalse(results.isEmpty())` is cleaner than `assertTrue(results.size() > 0)`. It reads naturally and produces a clearer failure message.

Run the tests:

```bash
mvn compile test
```

You should see results. If you're curious, there are about 10 countries with over 100 million people.

## Understanding Ignite 3 Data Access APIs

Before we write a custom SQL query, let's step back and look at Ignite 3's data access options. This context helps you understand where Spring Data fits in the bigger picture.

Ignite 3 provides three primary ways to access data:

### RecordView: Complete Records

RecordView treats each table row as a single object. You work with the entire record at once, either as a generic `Tuple` or as a mapped POJO.

```java
Table cityTable = ignite.tables().table("CITY");
RecordView<Tuple> recordView = cityTable.recordView();

// Create a key containing the primary key column(s)
Tuple key = Tuple.create().set("ID", 34);

// Fetch the complete record (null = implicit transaction)
Tuple record = recordView.get(null, key);
String name = record.stringValue("NAME");
int population = record.intValue("POPULATION");
```

Use RecordView when you need the full row or want to work with strongly-typed POJOs.

### KeyValueView: Separate Key and Value

KeyValueView separates the primary key columns from the value columns. The key and value are distinct objects.

```java
KeyValueView<Tuple, Tuple> kvView = cityTable.keyValueView();

Tuple key = Tuple.create().set("ID", 34);
Tuple value = kvView.get(null, key);  // Returns only non-key columns
String district = value.stringValue("DISTRICT");
```

Use KeyValueView when you only need the value portion, or when your access patterns naturally separate key from value.

### SQL API: Declarative Queries

The SQL API executes standard SQL queries. You can use the Statement builder for parameterized queries:

```java
Statement stmt = ignite.sql().statementBuilder()
    .query("SELECT name, population FROM CITY WHERE id = ?")
    .build();

try (ResultSet<SqlRow> rs = ignite.sql().execute(null, stmt, 34)) {
    if (rs.hasNext()) {
        SqlRow row = rs.next();
        String name = row.stringValue("NAME");
    }
}
```

Use SQL when you need JOINs, aggregations, complex filtering, or when you prefer declarative queries over imperative code.

### Working Example

The `Application.java` file includes a `demonstrateDataAccessApis()` method that shows all three APIs working with the CITY table. Run the application and check the logs to see each API in action.

### Why This Training Uses Spring Data

Spring Data repositories use SQL under the hood, but they generate it for you based on method names or `@Query` annotations. This approach offers:

- **Familiar patterns**: Same interfaces you'd use with PostgreSQL or MySQL
- **Less boilerplate**: No manual `Tuple.create()` or `ResultSet` iteration
- **Cleaner code**: Method names describe what you want, not how to get it

For production applications, choose the API that fits your use case. RecordView and KeyValueView are efficient for key-based lookups. SQL (whether through Spring Data or the native API) is better for complex queries.

## Step 12: Create CityRepository with Custom SQL

Query derivation works great for simple queries, but JOINs need actual SQL. Create `CityRepository.java`:

```java
@Repository
public interface CityRepository extends CrudRepository<City, Integer> {

    record PopulousCity(String cityName, Integer population, String countryName) {}

    @Query("SELECT city.name as city_name, MAX(city.population) as population, " +
           "country.name as country_name FROM country " +
           "JOIN city ON city.countrycode = country.code " +
           "GROUP BY city.name, country.name, city.population " +
           "ORDER BY city.population DESC LIMIT :limit")
    public List<PopulousCity> findTopXMostPopulatedCities(int limit);
}
```

There's a lot happening here, so let's break it down:

**Why a record?**

This query returns data from two tables: city names from CITY, country names from COUNTRY. The `City` entity class can't hold country names because it only maps to the CITY table. You need a new type to hold the combined result.

A Java record is perfect for this. Records are immutable data carriers with automatic `equals()`, `hashCode()`, and `toString()` methods. They're concise (one line vs. dozens for an equivalent class) and they signal intent: this is just data, no behavior.

Defining the record inside the interface keeps related code together. You could put it in a separate file, but for a simple DTO, inline is cleaner.

**Why column aliases?**

Spring Data maps query result columns to record components by name. The query returns `city.name`, but the record component is `cityName`. The alias `AS city_name` bridges this gap. Spring Data converts underscores to camelCase automatically, so `city_name` maps to `cityName`.

Without aliases, Spring Data wouldn't know which `name` column goes where, since both tables have a `name` column.

**The :limit parameter**

The `:limit` syntax creates a named parameter. Spring Data binds the method argument to this placeholder. Named parameters are clearer than positional (`?1`, `?2`) when you have multiple parameters.

## Step 13: Test the Join Query

Add another test:

```java
@Autowired
CityRepository cityRepository;

@Test
void cityRepositoryWorks() {
    // Test inherited CrudRepository.findById()
    var city = cityRepository.findById(34);
    log.info("city={}", city.orElse(null));
    assertTrue(city.isPresent());
    assertEquals("Tirana", city.get().getName());

    // Test custom @Query with JOIN, GROUP BY, ORDER BY, LIMIT
    var populatedCities = cityRepository.findTopXMostPopulatedCities(5);
    log.info("top 5 cities={}", populatedCities);
    assertEquals(5, populatedCities.size());
    assertEquals("Mumbai (Bombay)", populatedCities.get(0).cityName());
}
```

The comments clarify which repository feature each section exercises. Run the tests again. Mumbai should come out on top.

## What You've Done

You now have two Spring Data repositories:

- `CountryRepository` with inherited CRUD methods and a derived query
- `CityRepository` with a custom SQL query that joins two tables

Both repositories work against your distributed Ignite cluster, with queries executing in parallel across nodes.

## Next Module

Continue to [Module 4: REST API](04-rest-api.md) to expose your repositories through HTTP endpoints.
