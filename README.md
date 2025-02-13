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
* Java Developer Kit, version 8 or later
* Apache Maven 3.6.x
* Your favorite IDE, such as IntelliJ IDEA, or Eclipse, or a simple text editor.
* Postman REST tool (<https://www.postman.com/>) or a web browser

**<u>Note</u>**  

Although it is be possible to later versions of the JDK by following the instructions at <https://ignite.apache.org/docs/latest/quick-start/java#running-ignite-with-java-11-or-later>, 
we strongly suggest that you only use JDK 8 (1.8).

## 1. Clone the Project

Open a terminal window and clone the project to your dev environment:

```bash
git clone https://github.com/GridGain-Demos/spring-data-training.git
```

## 2. Configure Ignite Spring Boot and Data Extensions

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
         <artifactId>ignite-spring-data-2.2-ext</artifactId>
         <version>1.0.0</version>
      </dependency>

      <dependency>
         <groupId>org.apache.ignite</groupId>
         <artifactId>ignite-spring-boot-autoconfigure-ext</artifactId>
         <version>1.0.0</version>
      </dependency>
      ```

  2. The following property **has already been added** to the pom.xml to select a version of H2 supported by Ignite so there is **no need to take any action**.  Just remember that this property needs to set in the pom.xml to insure proper function of Ignite.

      ```xml
      <properties>
          <h2.version>1.4.197</h2.version>
      </properties>
      ```

## 3. Start Ignite Server Node With Spring Boot

  1. Add the `IgniteConfig` class in the `com.gridgain.training.spring` package with the following code.  This class returns an instance of Ignite started by Spring Boot:

      ```java
      @Configuration
      public class IgniteConfig {
          @Bean(name = "igniteInstance")
          public Ignite igniteInstance(Ignite ignite) {
              return ignite;
          }
      }
      ```

  2. Update the `Application` class by tagging it with `@EnableIgniteRepositories` annotation.

  3. Start the application and confirm Spring Boot started an Ignite server node instance.  You should see logging output something like that below.  
     The key part is the line in the center that has a timestamp followed by "Topology Snapshot".  This indicates that the Ignite cluster was started 
     and has one server and zero clients as a part of it.

 
     ```text
     >>> Local ports: TCP:10800 TCP:11211 TCP:47100 UDP:47400 TCP:47500
     >>> +-----------------------------------------------------------------------+

     [15:50:20] Topology snapshot [ver=1, locNode=297d311a, servers=1, clients=0, state=ACTIVE, CPUs=12, offheap=7.2GB, heap=8.0GB]
     [15:50:20]   ^-- Baseline [id=0, size=1, online=1, offline=0]
     2025-02-12 15:50:20.886  INFO 11298 --- [           main] o.a.i.i.m.d.GridDiscoveryManager         : Topology snapshot [ver=1, locNode=297d311a, servers=1
     ```

## 4. Change Spring Boot Settings to Start Ignite Client Node

  1. Update the `IgniteConfig` by adding an `IgniteConfigurer` that tells Spring Boot to start an Ignite **client** node instead of a server node:

      ```java
       @Bean
       public IgniteConfigurer configurer() {
           return igniteConfiguration -> {
               igniteConfiguration.setClientMode(true);
           };
       }
      ```

  2. Add the `ServerNodeStartup` class (in the `com.gridgain.training.spring` package) that will be used to start a separate application/process for an Ignite server node.

      ```java
      public class ServerNodeStartup {
          public static void main(String[] args) {
              Ignition.start();
          }
      }
      ```

  3. Start the Spring Boot application and the `ServerNodeStartupClass` application, and confirm the client node can
  connect to the server.

## 5. Load World Database

  1. Open the `world.sql` script in the project's `config` folder and add the `VALUE_TYPE` property to the `CREATE TABLE Country` statement.  
     Be sure to add it inside the "s in the WITH statement and to use a comma to separate the values in the string.

      ```sql
      VALUE_TYPE=com.gridgain.training.spring.model.Country
      ```
     
      The resulting line should look like this:

      ```sql
      ) WITH "template=partitioned, backups=1, CACHE_NAME=Country,VALUE_TYPE=com.gridgain.training.spring.model.Country";
      ```

  2. Add the following `VALUE_TYPE` property to the `CREATE TABLE City` statement.  Again, do this within the "s and use a comma to separate the values.

      ```sql
      VALUE_TYPE=com.gridgain.training.spring.model.City
      ```

  3. Add the following `KEY_TYPE` property to the `CREATE TABLE City` statement.  Once again, do this within the "s and use a comma to separate the values.

      ```sql
      KEY_TYPE=com.gridgain.training.spring.model.CityKey
      ```

     The resulting line should look like this:
     ```sql
     ) WITH "template=partitioned, backups=1, affinityKey=CountryCode, CACHE_NAME=City,VALUE_TYPE=com.gridgain.training.spring.model.City,KEY_TYPE=com.gridgain.training.spring.model.CityKey";
     ```
  4. Build a shaded package for the app:
      ```shell script
      mvn clean package -DskipTests=true
      ```

  5. Start an SQLLine process:

      ```shell script
      java -cp libs/app.jar sqlline.SqlLine
      ```

  6. Connect to the cluster:

      ```shell script
      !connect jdbc:ignite:thin://127.0.0.1/ ignite ignite
      ```

  7. Load the database:

      ```shell script
      !run config/world.sql
      ```

## 6. Run Simple Auto-Generated Queries Via Ignite Repository

  1. Create the `CountryRepository` class (in the `com.gridgain.training.spring` package):

      ```java
      @RepositoryConfig (cacheName = "Country")
      @Repository
      public interface CountryRepository extends IgniteRepository<Country, String> {

      }
      ```

  2. Add a method that returns countries with a population bigger than provided one:

      ```java
      public List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
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
      @RepositoryConfig(cacheName = "City")
      @Repository
      public interface CityRepository extends IgniteRepository<City, CityKey> {
      }
      ```

  2. Add a query that returns a complete key-value pair:

      ```java
      public Cache.Entry<CityKey, City> findById(int id);
      ```
  3. Add a direct SQL query that joins two tables:

      ```java
      @Query("SELECT city.name, MAX(city.population), country.name FROM country " +
              "JOIN city ON city.countrycode = country.code " +
              "GROUP BY city.name, country.name, city.population " +
              "ORDER BY city.population DESC LIMIT ?")
      public List<List<?>> findTopXMostPopulatedCities(int limit);
      ```

  4. Create a test in ApplicationTests to validate the methods respond properly:

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
      public List<List<?>> getMostPopulatedCities(@RequestParam(value = "limit", required = false) Integer limit) {
          return cityRepository.findTopXMostPopulatedCities(limit);
      }
      ```

  3.  Restart the `Application` and then test the controller method either in Postman or your browser:
  
      <http://localhost:8080/api/mostPopulated?limit=5>

## 9. Create an Ignite Thin Client Application
1. Create a new java package named `com.gridgain.training.thinclient`.
  1. Add the `IgniteThinClient` class to the `com.gridgain.training.thinclient` package that performs a join query on the City & Country tables

  ```java
  @SpringBootApplication
  public class IgniteThinClient implements ApplicationRunner {

  	@Autowired
  	private IgniteClient client;

  	private static final String QUERY = "SELECT city.name, MAX(city.population), country.name FROM country JOIN city ON city.countrycode = country.code GROUP BY city.name, country.name, city.population ORDER BY city.population DESC LIMIT ?";

  	@Override
  	public void run(ApplicationArguments args) throws Exception {
  		System.out.println("ServiceWithIgniteClient.run");
  		System.out.println("Cache names existing in cluster: " + client.cacheNames());

  		ClientCache<CityKey, City> cityCache = client.cache("City");
  		FieldsQueryCursor<List<?>> cursor = cityCache.query(new SqlFieldsQuery(QUERY).setArgs(3));
  		System.out.printf("%15s %12s %10s\n", "City", "Country", "Population");
  		System.out.printf("%15s %12s %10s\n", "===============", "============", "==========");
  		cursor.forEach((row) -> {
  			System.out.printf("%15s %12s %10d\n", row.get(0), row.get(2), row.get(1));
  		});
  	}
  }
  ```
     
  2. Add the following to the pom.xml:
  ```xml
  <dependency>
      <groupId>org.apache.ignite</groupId>
      <artifactId>ignite-spring-boot-thin-client-autoconfigure-ext</artifactId>
      <version>1.0.0</version>
  </dependency>
  ```   

  3. Add the `ThinClientApplication` class (in the `com.gridgain.training.thinclient` package)that bootstraps the Thin Client Application.

  ```java
  @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, IgniteAutoConfiguration.class}, scanBasePackages = "com.gridgain.training.thinclient")
  public class ThinClientApplication {
      public static void main(String[] args) {
          SpringApplication.run(ThinClientApplication.class);
      }

      @Bean
      IgniteClientConfigurer configurer() {
          return cfg -> {
          	cfg.setAddresses("127.0.0.1:10800");
          	cfg.setSendBufferSize(64*1024);
          };
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
