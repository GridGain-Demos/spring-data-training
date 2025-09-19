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
* Tool to query a REST endpoint such as:
  * [Postman REST tool](https://www.postman.com/)
  * [curl](https://curl.se/)
  * [httpie](https://httpie.io)
  * A web browser

**<u>Note</u>**  

This project has been tested most thoroughly using Java 17 and Ignite 3. (Apache Ignite 3 supports Java 11, but the minimum version for Spring Boot is Java 17.) Later versions _may_ work; earlier versions will not. We test most frequently on Macs, but it should also work on Windows and Linux machines. Please create an Issue (or a PR!) if you find any issues.

## Hands-on part 1

### 1. Clone the Project

Open a terminal window and clone the project to your dev environment:

```bash
git clone https://github.com/GridGain-Demos/spring-data-training.git
```

### 2. Start your Apache Ignite cluster

1. Start your nodes using Docker Compose. Using Apache Ignite 3:

    ```bash
    docker compose -f docker-compose.yml up -d
    ```

    Or GridGain 9:

    ```bash
    docker compose -f docker-compose-gg9.yml up -d
    ```

2. Initialize your cluster:

   a. Start the Command Line Interface (CLI).  Using Apache Ignite 3:

    ```bash
   docker run -e LANG=C.UTF-8 -e LC_ALL=C.UTF-8 -v ./config/world.sql:/opt/ignite/downloads/world.sql --rm --network spring-boot-data-training_default -it apacheignite/ignite:3.0.0 cli
   ```

   Or GridGain 9:

    ```bash
   docker run -e LANG=C.UTF-8 -e LC_ALL=C.UTF-8 -v ./gridgain-license.json:/opt/ignite/downloads/gridgain-license.json -v ./config/world.sql:/opt/ignite/downloads/world.sql --rm --network spring-boot-data-training_default -it gridgain/gridgain9:9.1.0 cli
   ```
   (Ensure your license file is in your current directory.)

   b. Connect to the cluster.

   ```bash
   connect http://node1:10300
   ```

   c. Execute command to initialize the cluster. Using Apache Ignite 3:

   ```bash
   cluster init --name=spring-data-training --metastorage-group=node1,node2
   ```

   Or GridGain 9:

   ```bash
   cluster init --name=spring-data-training --metastorage-group=node1,node2 --license=/opt/ignite/downloads/gridgain-license.json
   ```

Leave the CLI connected to the cluster.

### 3. Load World Database

1. Open a terminal window and navigate to the root directory of this project.

2. Load the media store database by executing the SQL command to load the sample data.

   ```bash
   sql --file=/opt/ignite/downloads/world.sql
    ```

## Hands-on part 2 

### 4. Configure Ignite Spring Boot and Data Extensions

  1. Enable Ignite Spring Boot and Spring Data extensions by adding the following artifacts to the `pom.xml` file

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

  2. Configure Spring Data to speak the right SQL dialect. Create a file `resources/META-INF/spring.factories` and add the following parameter:

      ```properties
     org.springframework.data.jdbc.repository.config.DialectResolver$JdbcDialectProvider=org.apache.ignite.data.IgniteDialectProvider
     ```

### 5. Configure Spring Boot to connect to Ignite

  1. Update the `application.properties` by adding an option that tells Spring Boot where to find the Ignite server node:

      ```properties
       ignite.client.addresses=127.0.0.1:10800
       spring.datasource.url=jdbc:ignite:thin://localhost:10800/
       spring.datasource.driver-class-name=org.apache.ignite.jdbc.IgniteJdbcDriver
      ```
     
  2. Edit the `Application.java` class. Autowire our connection to the Ignite servers:

      ```java
      @Autowired
      private Ignite ignite;
      ```

  3. Add some diagnostics code to run when the server starts:

      ```java
      private Logger log = LoggerFactory.getLogger(Application.class);

      @EventListener(ApplicationReadyEvent.class)
      public void startupLogger() {
          log.info("Table names existing in cluster: {}", ignite.tables().tables().stream().map(Table::name).toList());

          log.info("Node information:");
          for (var n : ignite.cluster().nodes()) {
              log.info("ID: {}, Name: {}, Address: {}", n.id(), n.name(), n.address());
          }
      }
      ```

  4. Run your new Spring Boot application. It should connect to your Ignite servers and list information about the tables and cluster topology

## Hand-on part 3

### 6. Run Simple Auto-Generated Queries Via Ignite Repository

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
        var results = countryRepository.findByPopulationGreaterThanOrderByPopulationDesc(100_000_000);
        System.out.println("count=" + results.size());
        Assertions.assertTrue(results.size() > 0);
      }
      ```
      Add the following line after ApplicationTests class declaration:
      ```java
      @Autowired CountryRepository countryRepository;
      ```
     
  4. Run the tests:

      ```shell
      mvn compile test
      ```

### 7. Run Direct Queries With JOINs Via Ignite Repository

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
          var city = cityRepository.findById(34);
          Assertions.assertTrue(city.isPresent());
          Assertions.assertEquals("Tirana", city.get().getName());
   
          var populatedCities = cityRepository.findTopXMostPopulatedCities(5);
          Assertions.assertEquals(5, populatedCities.size());
          Assertions.assertEquals("Mumbai (Bombay)", populatedCities.get(0).cityName());
	  }
      ```
   
      Add the following line after ApplicationTests class declaration:
      ```java
      @Autowired CityRepository cityRepository;
      ```
   
  4. Run the tests:

      ```shell
      mvn compile test
      ```

## Hands-on part 4

### 8. Create Spring REST Controller

In this section, we'll bring together the REST end-points supported by Spring Boot and the database access provided by Spring Data. By starting Ignite and loading the data (as mentioned in the above steps), this code can be directly used for the REST APIs. 

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
      public List<CityRepository.PopulousCity> getMostPopulatedCities(@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
          return cityRepository.findTopXMostPopulatedCities(limit);
      }
      ```

  3.  Restart the `Application` and then test the controller method either in REST endpoint viewer: http://localhost:8080/api/mostPopulated?limit=5

## Hands-on wrap-up

When you have finished the exercises, you can shut down your cluster.

1. Shut down the client application in your IDE
2. Shut down your cluster with the following command if you're using Apache Ignite:

      ```shell
      docker compose -f docker-compose.yaml down
      ```
   
   Or if you're using GridGain:

      ```shell
      docker compose -f docker-compose-gg9.yaml down
      ```

   The "down" command shuts down and deletes the containers. You can also use the "stop" command, which stops the cluster but keeps the containers, meaning that they can be restarted.