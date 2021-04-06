# spring-data-training
Template project for Apache Ignite with Spring Boot and Spring Data Training

TBD

## Setting Up Environment

* Java Developer Kit, version 8 or later
* Apache Maven 3.0 or later
* Your favorite IDE, such as IntelliJ IDEA, or Eclipse, or a simple text editor.

## Configure Ignite Spring Boot and Data Extensions

1. Enable Ignite Spring Boot and Spring Data extensions by adding the following artifacts to the `pom.xml` file

    ```xml
    <dependency>
        <groupId>org.apache.ignite</groupId>
        <artifactId>ignite-spring-data_2.2</artifactId>
        <version>2.9.1</version>
    </dependency>

    <dependency>
        <groupId>org.apache.ignite</groupId>
        <artifactId>ignite-spring-boot-autoconfigure-ext</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

2. Add the following property to the pom.xml to select a version of H2 supported by Ignite:
    ```xml
    <properties>
        <h2.version>1.4.197</h2.version>
    </properties>
    ```
   
SLIDES_TODO: explain how to use spring boot autoconfigure and what types exist

## Start Ignite Server Node With Spring Boot

1. Add the `IgniteConfig` class that returns an instance of Ignite started by Spring Boot:

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

3. Start the application and confirm Spring Boot started an Ignite server node instance.

## Change Spring Boot Settings to Start Ignite Client Node

1. Update the `IgniteConfig` by adding an `IgniteConfigurer` that requires Spring Boot to start an Ignite client node:

    ```java
     @Bean
     public IgniteConfigurer configurer() {
         return igniteConfiguration -> {
         igniteConfiguration.setClientMode(true);
         };
     }
    ```

2. Add an `ServerNodeStartupClass` class that will be a separate application/process for an Ignite server node.

    ```java
    public class ServerNodeStartupClass {
        public static void main(String[] args) {
            Ignition.start();
        }
    }
    ```

3. Start the Spring Boot application and the `ServerNodeStartupClass` application, and confirm the client node can
connect to the server.

## Load World Database

1. Open the `world.sql` script and add the `VALUE_TYPE` property to the `CREATE TABLE Country` statement:
    
    ```sql
    VALUE_TYPE=com.gridgain.training.spring.model.Country
    ``` 

2. Add the following `VALUE_TYPE` property to the `CREATE TABLE City` statement

    ```sql
    VALUE_TYPE=com.gridgain.training.spring.model.City
    ``` 
  
3. Add the following `KEY_TYPE` property to the `CREATE TABLE City` statement

    ```sql
    KEY_TYPE=com.gridgain.training.spring.model.CityKey
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


## Run Simple Auto-Generated Queries Via Ignite Repository

1. Create the `CountryRepository` class:

    ```java
    @RepositoryConfig (cacheName = "Country")
    @Repository
    public interface CountryRepository extends IgniteRepository<Country, String> {
    
    }
    ```
   
2. Add a method that returns countries with a population bigger than provided one:

    ```java
    public List<Cache.Entry<String, Country>> findByPopulationGreaterThanOrderByPopulationDesc(int population);
    ```
   
## Run Direct Queries With JOINs Via Ignite Repository

## Create Spring RESTFul Services

1. Create GET Requests

2. Create POST Requests
