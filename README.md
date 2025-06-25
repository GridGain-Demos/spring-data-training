# Project Template for Apache Ignite With Spring Boot and Spring Data Training

This project template is used throughout a
[two-hour training session for Java developers and architects](https://www.gridgain.com/products/services/training/apache-ignite-spring-boot-and-spring-data-development)
who want to explore the best practices and nuances of using Spring Boot and Spring Data with Apache Ignite.
During that instructor-led training, you build a RESTful web service that uses Apache Ignite as an in-memory database.
The service is a Spring Boot application that interacts with the Ignite cluster via Spring Data repository abstractions.

Check [the schedule a join one of our upcoming sessions](https://www.gridgain.com/products/services/training/apache-ignite-spring-boot-and-spring-data-development).
All the sessions are delivered by seasoned Ignite experts and committers.

## Setting Up Environment

* GIT command line or GitHub Desktop (<https://desktop.github.com/>)
* Docker Desktop
* Java Developer Kit, version 17 or later
* Apache Maven 3.6.x
* Your favorite IDE, such as IntelliJ IDEA, or Eclipse, or a simple text editor.
* Postman REST tool (<https://www.postman.com/>) or a web browser

**<u>Note</u>**  

Although it **is** possible to use later versions of the JDK by following the instructions at <https://ignite.apache.org/docs/latest/quick-start/java#running-ignite-with-java-11-or-later>, 
**we strongly suggest that you use JDK 17.** (Apache Ignite 3 supports Java 11, but the minimum version for Spring Boot is Java 17.)

## 1. Clone the Project

Open a terminal window and clone the project to your dev environment:

```bash
git clone https://github.com/GridGain-Demos/spring-data-training.git
```

## 2. Start your Apache Ignite cluster

1Start your nodes using Docker Compose:

    ```bash
    docker compose -f docker-compose.yml up -d
    ```

## 3. Load World Database

1. Open a terminal window and navigate to the root directory of this project.
2. Load the media store database:

   a. Start the Command Line Interface (CLI)

    ```bash
   docker run -e LANG=C.UTF-8 -e LC_ALL=C.UTF-8 -v ./config/world.sql:/opt/ignite/downloads/world.sql --rm --network ignite3_default -it apacheignite/ignite:3.0.0 cli
   ```

   b. Connect to the cluster.

   ```bash
   connect http://node1:10300
   ```

   c. Execute SQL command to load the sample data.

   ```bash
   sql --file=/opt/ignite/downloads/world.sql
    ```

## 4. Configure Ignite Spring Boot and Data Extensions

  1. Enable Ignite Spring Boot and Spring Data extensions by adding the following artifacts to the `pom.xml` file

      ```xml
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
      </dependency>

      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
      </dependency>

      <dependency>
         <groupId>org.apache.ignite</groupId>
         <artifactId>spring-data-ignite</artifactId>
         <version>3.0.0</version>
      </dependency>

      <dependency>
         <groupId>org.apache.ignite</groupId>
         <artifactId>spring-boot-ignite-client-autoconfigure</artifactId>
         <version>3.0.0</version>
      </dependency>
      ```

## 5. Configure Spring Boot to connect to Ignite

  1. Update the `application.properties` by adding an option that tells Spring Boot where to find the Ignite server node:

      ```properties
       ignite.client.addresses=127.0.0.1:10800
       spring.datasource.url=jdbc:ignite:thin://localhost:10800/
       spring.datasource.driver-class-name=org.apache.ignite.jdbc.IgniteJdbcDriver
      ```

## 6. Run Simple Auto-Generated Queries Via Ignite Repository

  1. Create the `CountryRepository` class (in the `com.gridgain.training.spring` package):

      ```java
      @Repository
      public interface CountryRepository extends CrudRepository<Country, String> {

      }
      ```

  2. Add a method that returns countries with a population bigger than provided one:

      ```java
      List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
      ```

  3. Add a test in ApplicationTests (in the `src/test` folder) that validates that the method returns a non-empty result:

      ```java
      @Test
      void countryRepositoryWorks() {
         System.out.println("count=" + countryRepository.findByPopulationGreaterThanOrderByPopulationDesc(100_000_000).size());
      }
      ```
      Add following line after ApplicationTests class declaration:
      ```java
      @Autowired CountryRepository countryRepository;
      ```

## 7. Run Direct Queries With JOINs Via Ignite Repository

  1. Create the `CityRepository` class (in the `com.gridgain.training.spring` package) :

      ```java
      @Repository
      public interface CityRepository extends CrudRepository<City, Integer> {
      }
      ```

  2. Add a direct SQL query that joins two tables:

      ```java
    record PopulousCity(String cityName, Integer population, String countryName) {}

    @Query("SELECT city.name as city_name, MAX(city.population) as population, country.name as country_name FROM country " +
            "JOIN city ON city.countrycode = country.code " +
            "GROUP BY city.name, country.name, city.population " +
            "ORDER BY city.population DESC LIMIT :limit")
    public List<PopulousCity> findTopXMostPopulatedCities(int limit);
      ```

3. Create a test in ApplicationTests to validate the methods respond properly:

      ```java
      @Test
      void cityRepositoryWorks() {
          System.out.println("city = " + cityRepository.findById(34));

          System.out.println("top 5 = " + cityRepository.findTopXMostPopulatedCities(5));
      }
      ```
      Add following line after ApplicationTests class declaration:
      ```java
      @Autowired CityRepository cityRepository;
      ```

## 8. Create Spring REST Controller
REST APIs are exposed using a thin client in the branch ThinClientREST. By starting ignite and loading the data (as mentioned in the above steps), this branch can be directly used for the REST APIs. The steps/code given below create a thick client.

  1. Create a REST Controller for the application by creating a new class named `WorldDatabaseController` (in the `com.gridgain.training.spring` package) with the following contents:

      ```java
      @RestController
      public class WorldDatabaseController {
          @Autowired CityRepository cityRepository;

      }
      ```

  2. Add a method that returns top X most populated cities:

      ```java
      @GetMapping("/api/mostPopulated")
      public List<PopulousCity> getMostPopulatedCities(@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
          return cityRepository.findTopXMostPopulatedCities(limit);
      }
      ```

  3.  Restart the `Application` and then test the controller method either in Postman or your browser:
  
      <http://localhost:8080/api/mostPopulated?limit=5>

## 9. Create an Ignite Client Application
1. Create a new java package named `com.gridgain.training.thinclient`.

   
  2. Add the `SpringIgniteClient` class to the `com.gridgain.training.client` package that performs a join query on the City & Country tables

  ```java
  @SpringBootApplication
public class SpringIgniteClient implements ApplicationRunner {

    @Autowired
    private IgniteClient client;

    private static final String QUERY = "SELECT city.name, MAX(city.population), country.name FROM country JOIN city ON city.countrycode = country.code GROUP BY city.name, country.name, city.population ORDER BY city.population DESC LIMIT ?";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ServiceWithIgniteClient.run");
        System.out.println("Cache names existing in cluster: " + client.tables().tables().stream().map(Table::name).toList());

        var cityCache = client.tables().table("City").recordView(City.class);
        try (var results = client.sql().execute(null, QUERY, 5)) {
            System.out.printf("%15s %12s %10s\n", "City", "Country", "Population");
            System.out.printf("%15s %12s %10s\n", "===============", "============", "==========");
            while (results.hasNext()) {
                var row = results.next();
                System.out.printf("%15s %12s %10d\n", row.stringValue(0), row.stringValue(2), row.intValue(1));
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringIgniteClient.class, args);
    }
}
```

  3. Stop the `Application` application (if it is currently running).  If you do not, you will receive an error about a port conflict.

  4. Run the `ThinClientApplication` class/application, and confirm the client node can connect to the server & run the query.


**<u>Notes</u>**
1. You can not run both the thin client and the "Application" at the same time since they will both attempt to run on port 8080.
   
2. To be able to run the Application once you have added the thin client code, you **will** have to modify the class definition in the Application class.
Simply remove the "//" from the `@SpringBootApplication` line.  The result should like the line below.

```java
@SpringBootApplication (scanBasePackages = "com.gridgain.training.spring", exclude = {IgniteClientAutoConfiguration.class})
```
