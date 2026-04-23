# Apache Ignite with Spring Boot and Spring Data — Training Project

A free instructor-led training on building Spring Boot applications backed by a GridGain / Apache Ignite cluster using Spring Data repositories and the thin client. Check the [complete schedule](https://www.gridgain.com/products/services/training/apache-ignite-spring-boot-and-spring-data-development) and join an upcoming session.

During the live training you build a RESTful web service on top of a three-node GG8 cluster: Spring Data repositories for `Country` and `City`, a `@Query` SQL join for top-N most-populated cities, and a single REST endpoint that exercises the full stack. When you are done, diff your work against the finished solution on the [`gg8_docker_solution`](https://github.com/GridGain-Demos/spring-data-training/tree/gg8_docker_solution) branch.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Layout](#project-layout)
- [1. Clone the Project](#1-clone-the-project)
- [2. Start the Cluster](#2-start-the-cluster)
- [3. Configure Spring Boot and the Thin Client](#3-configure-spring-boot-and-the-thin-client)
- [4. Load the World Database](#4-load-the-world-database)
- [5. Auto-Generated Repository Queries](#5-auto-generated-repository-queries)
- [6. Direct Queries With SQL Joins](#6-direct-queries-with-sql-joins)
- [7. REST Controller](#7-rest-controller)
- [8. Build and Run](#8-build-and-run)
- [Shutdown](#shutdown)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Git
- Docker Desktop
- Your favorite IDE (IntelliJ, Eclipse, VS Code, or a plain editor)
- `curl` or Postman for exercising REST endpoints

JDK 17 and Maven are optional — the `app` sidecar provides both. Install JDK 17 locally only if you use the standalone paths.

**Linux only:** the GridGain container image runs as UID 10000. If nodes fail to start on Linux, run `chown -R 10000:10000 docker/data/` and retry.

---

## Project Layout

Three GridGain nodes (`node1`, `node2`, `node3`) run on an isolated Docker bridge network. Only `node1` publishes port `10800` to the host — that is the thin-client address the app connects to. The `app` service is a Maven 3.9 + JDK 17 sidecar: it shares the project directory via a bind mount, so you can build and run the app without installing Maven locally. The build writes only to `libs/` — a directory the server nodes do not mount — so the cluster can stay up during sidecar builds.

```
config/
  world.sql               ← schema and data loaded into the cluster
docker/
  docker-compose.yaml     ← full topology rationale and mount details live here
  config/                 ← training-node-config.xml + ignite-log4j2.xml,
  │                          bind-mounted read-only into every server node
  data/
  │  node1/log/           ← node1 log files on the host (also via `docker compose logs node1`)
  │  node2/log/           ← node2 log files
  │  node3/log/           ← node3 log files
libs/                     ← app.jar lands here after a build
src/                      ← training source — edit these for the exercises
```

---

## 1. Clone the Project

```bash
git clone -b gg8_docker https://github.com/GridGain-Demos/spring-data-training.git
cd spring-data-training
```

---

## 2. Start the Cluster

```bash
docker compose -f docker/docker-compose.yaml up -d
```

Verify all three nodes joined:

```bash
docker compose -f docker/docker-compose.yaml logs node1 | grep "Topology snapshot" | tail -1
```

Expect `servers=3` in the output.

---

## 3. Configure Spring Boot and the Thin Client

### 3.1 Add dependencies to `pom.xml`

Add these entries to the `<dependencies>` block. The extensions provide the `IgniteRepository` interface backed by an auto-configured thin client:

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
    <artifactId>ignite-spring-data-ext</artifactId>
    <version>3.1.0</version>
</dependency>

<dependency>
    <groupId>org.apache.ignite</groupId>
    <artifactId>ignite-spring-boot-thin-client-autoconfigure-ext</artifactId>
    <version>2.0.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-commons</artifactId>
</dependency>

<!--
  Workaround: ignite-spring-data-ext:3.1.0 uses classes from
  org.springframework.dao.* but does not declare spring-tx as a
  transitive dep. Upstream bug; remove this entry once fixed.
-->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-tx</artifactId>
</dependency>
```

The skeleton already declares `org.gridgain:ignite-core:8.9.32` — leave that alone.

### 3.2 Enable the Spring Data Ignite repositories

Edit `src/main/java/com/gridgain/training/spring/Application.java` and add `@EnableIgniteRepositories`:

```java
@SpringBootApplication
@EnableIgniteRepositories
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3.3 Point Spring Boot at the cluster

Edit `src/main/resources/application.properties`:

```properties
ignite-client.addresses=${IGNITE_ADDRESS:localhost:10800}
```

The `${IGNITE_ADDRESS:localhost:10800}` default lets the same jar run from the host (`localhost:10800`) and from the docker `app` sidecar (`IGNITE_ADDRESS=node1:10800` is baked into the compose service). The thin-client bean is auto-configured by the autoconfigure extension — no `IgniteConfig` class needed.

---

## 4. Load the World Database

### 4.1 Bind the cache entries to Java types

Edit `config/world.sql`. On the `CREATE TABLE Country` statement, add `VALUE_TYPE` inside the `WITH` clause:

```sql
) WITH "template=partitioned, backups=1, CACHE_NAME=Country, VALUE_TYPE=com.gridgain.training.spring.model.Country";
```

On the `CREATE TABLE City` statement, add both `VALUE_TYPE` and `KEY_TYPE`:

```sql
) WITH "template=partitioned, backups=1, affinityKey=CountryCode, CACHE_NAME=City, VALUE_TYPE=com.gridgain.training.spring.model.City, KEY_TYPE=com.gridgain.training.spring.model.CityKey";
```

These bindings make Ignite's binary metadata point at the Java model classes so the thin client can round-trip query results into `Country` and `City` objects.

### 4.2 Run the SQL script

```bash
docker compose -f docker/docker-compose.yaml exec -T node1 /opt/gridgain/bin/sqlline.sh -u "jdbc:ignite:thin://127.0.0.1/" --silent=true < config/world.sql
```

Verify row counts:

```bash
printf 'SELECT COUNT(*) FROM Country;\nSELECT COUNT(*) FROM City;\n!quit\n' | docker compose -f docker/docker-compose.yaml exec -T node1 /opt/gridgain/bin/sqlline.sh -u "jdbc:ignite:thin://127.0.0.1/" --silent=true
```

Expect **239** countries and **4079** cities.

---

## 5. Auto-Generated Repository Queries

Create `src/main/java/com/gridgain/training/spring/CountryRepository.java`:

```java
package com.gridgain.training.spring;

import java.util.List;

import com.gridgain.training.spring.model.Country;
import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@RepositoryConfig(cacheName = "Country")
@Repository
public interface CountryRepository extends IgniteRepository<Country, String> {

    List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
}
```

Add a test in `src/test/java/com/gridgain/training/spring/ApplicationTests.java`:

```java
@Autowired CountryRepository countryRepository;

@Test
void countryRepositoryWorks() {
    System.out.println("count=" +
        countryRepository.findByPopulationGreaterThanOrderByPopulationDesc(100_000_000).size());
}
```

---

## 6. Direct Queries With SQL Joins

Create `src/main/java/com/gridgain/training/spring/CityRepository.java`:

```java
package com.gridgain.training.spring;

import java.util.List;
import javax.cache.Cache;

import com.gridgain.training.spring.model.City;
import com.gridgain.training.spring.model.CityKey;
import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@RepositoryConfig(cacheName = "City")
@Repository
public interface CityRepository extends IgniteRepository<City, CityKey> {

    Cache.Entry<CityKey, City> findById(int id);

    @Query("SELECT city.name, MAX(city.population), country.name FROM country " +
           "JOIN city ON city.countrycode = country.code " +
           "GROUP BY city.name, country.name, city.population " +
           "ORDER BY city.population DESC LIMIT ?")
    List<List<?>> findTopXMostPopulatedCities(int limit);
}
```

Extend the test:

```java
@Autowired CityRepository cityRepository;

@Test
void cityRepositoryWorks() {
    System.out.println("city = " + cityRepository.findById(34));
    System.out.println("top 5 = " + cityRepository.findTopXMostPopulatedCities(5));
}
```

---

## 7. REST Controller

Create `src/main/java/com/gridgain/training/spring/WorldDatabaseController.java`:

```java
package com.gridgain.training.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WorldDatabaseController {

    @Autowired
    CityRepository cityRepository;

    @GetMapping("/api/mostPopulated")
    public List<List<?>> getMostPopulatedCities(@RequestParam("limit") int limit) {
        return cityRepository.findTopXMostPopulatedCities(limit);
    }
}
```

---

## 8. Build and Run

### Build

Two paths — pick whichever suits your environment.

**Standalone (host Maven):**

The cluster can stay up during a host Maven build.

```bash
mvn clean package -DskipTests
```

**Docker:**

The cluster can stay up — the build writes to `libs/`, which the server nodes do not mount.

```bash
docker compose -f docker/docker-compose.yaml run --rm app mvn -B clean package -DskipTests
```

Both paths produce `libs/app.jar`.

### Run

Wait approximately 15 seconds after starting for the `Started Application in …` log line.

**Standalone:**

```bash
java @src/main/resources/j17.params -jar libs/app.jar --server.port=18080 &
```

**Docker:**

```bash
docker compose -f docker/docker-compose.yaml run --rm -p 18080:18080 --name sd-app app java @/work/src/main/resources/j17.params -jar /work/libs/app.jar --server.port=18080 &
```

`IGNITE_ADDRESS=node1:10800` is baked into the compose service so the `app` container reaches the cluster automatically.

### Verify the endpoints

```bash
curl -s -w "HTTP %{http_code}\n" "http://localhost:18080/api/mostPopulated?limit=5"
```

Expect `[["Mumbai (Bombay)",10500000,"India"],["Seoul",...],...]` with HTTP 200.

### Stop the application

**Standalone:**

```bash
kill %1
```

`%1` refers to the first job backgrounded in this shell session with `&`. Run this in the same terminal where you started the app.

**Docker:**

```bash
docker rm -f sd-app
```

---

## Shutdown

```bash
docker compose -f docker/docker-compose.yaml down
```

The `docker/data/` directory is kept on the host (holds logs and marshaller metadata; only gains `db/` and `wal/` subdirectories when persistence is enabled).

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `docker compose up -d` hangs on the second attempt | Port 10800 still held by a cluster running in another directory | `docker compose -f docker/docker-compose.yaml down` in that directory first |
| Nodes start but produce no logs; `docker/data/` empty (Linux only) | Container runs as UID 10000; host `docker/data/` owned by your user | `chown -R 10000:10000 docker/data/` |
| `Web server failed to start. Port 8080 was already in use.` | Something on the host owns port 8080 | Pass `--server.port=18080` (already in the commands above) |
| `InaccessibleObjectException: Unable to make field long java.nio.Buffer.address accessible` | Missing `@src/main/resources/j17.params` before `-jar` | Add the `@` argfile argument |
| Sidecar: `Connection refused` to thin client | `IGNITE_ADDRESS` env var not set | Check `environment:` block in `docker/docker-compose.yaml` sets `IGNITE_ADDRESS=node1:10800` |
| `NoClassDefFoundError: InvalidDataAccessApiUsageException` | `ignite-spring-data-ext:3.1.0` omits `spring-tx` as a transitive dependency | Add `spring-tx` explicitly to `pom.xml` (see step 3.1) |
