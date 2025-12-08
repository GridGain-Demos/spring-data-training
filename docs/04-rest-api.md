# Module 4: REST API

The final module ties everything together with a REST endpoint. You'll expose your Spring Data repository through HTTP so any client can query your distributed database.

## Step 14: Create the Controller

Create `WorldDatabaseController.java`:

```java
@RestController
public class WorldDatabaseController {

    @Autowired
    CityRepository cityRepository;

    @GetMapping("/api/mostPopulated")
    public List<CityRepository.PopulousCity> getMostPopulatedCities(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        return cityRepository.findTopXMostPopulatedCities(limit);
    }
}
```

If you haven't worked with Spring MVC before, here's what each annotation does:

**@RestController**

This marks the class as a REST API controller. It combines two annotations: `@Controller` (this class handles web requests) and `@ResponseBody` (return values should be written directly to the HTTP response body, not interpreted as view names). Spring automatically serializes your return objects to JSON.

**@GetMapping("/api/mostPopulated")**

This maps HTTP GET requests to the specified path to this method. When someone requests `GET /api/mostPopulated`, Spring calls `getMostPopulatedCities()`. There are also `@PostMapping`, `@PutMapping`, `@DeleteMapping` for other HTTP methods.

**@RequestParam**

This extracts query parameters from the URL. For `/api/mostPopulated?limit=5`, the `limit` parameter becomes the method argument. The annotation specifies:
- `value = "limit"`: the query parameter name
- `required = false`: the parameter is optional
- `defaultValue = "10"`: use this value if the parameter is missing

Spring handles the type conversion from the string "5" in the URL to the Integer 5 in your code.

## Step 15: Try It Out

Restart the application and open your browser to:

```
http://localhost:8080/api/mostPopulated?limit=5
```

You should see JSON with the five most populous cities in the world, along with their countries.

## Shutting Down

When you're done experimenting, shut down the cluster:

**For Apache Ignite:**

```bash
docker compose -f docker-compose.yml down
```

**For GridGain:**

```bash
docker compose -f docker-compose-gg9.yml down
```

The `down` command removes the containers entirely. Use `stop` instead if you want to keep them for later.

## What You've Built

Congratulations! You've built a complete Spring Boot application that:

- Connects to a distributed Apache Ignite cluster
- Uses Spring Data repositories for database operations
- Generates queries automatically from method names
- Executes custom SQL with JOINs across distributed tables
- Exposes data through REST endpoints

The request flow looks like this:

1. HTTP request arrives at your REST controller
2. Controller calls the repository method
3. Spring Data generates SQL from your method or `@Query` annotation
4. SQL is sent to Ignite via JDBC
5. Ignite executes the query in parallel across cluster nodes
6. Results flow back through Spring Data, which maps them to your objects
7. Spring MVC serializes the objects to JSON and sends the response

The patterns here work the same whether your cluster has three nodes or three hundred. Spring Data handles the abstraction, and Ignite handles the distribution.

## Going Further

Now that you've completed the training, consider exploring:

- [Architecture Guide](reference/architecture.md): Deeper explanation of how the components fit together
- [Apache Ignite Documentation](https://ignite.apache.org/docs/latest/): Transactions, compute, continuous queries
- [Spring Data JDBC Reference](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/): More query derivation options, custom converters

## Feedback

If you attended the instructor-led session, we'd love to hear your feedback. If you worked through this on your own and found issues, please open an Issue or PR on the repository.
