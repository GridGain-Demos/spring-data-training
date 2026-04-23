# Spring Data Training — Apache Ignite

A free instructor-led training on building Spring Boot applications backed by a GridGain / Apache Ignite cluster using Spring Data repositories and the thin client. Check the [complete schedule](https://www.gridgain.com/products/services/training/apache-ignite-spring-boot-and-spring-data-development) and join an upcoming session.

During the live training you wire a Spring Boot REST service to a three-node GG8 cluster: Spring Data repositories for `Country` and `City`, a `@Query` SQL join for top-N most-populated cities, and a single REST endpoint that exercises the full stack.

This is the **`gg8_docker_solution`** branch — the finished picture, with `CountryRepository`, `CityRepository`, `WorldDatabaseController`, `@EnableIgniteRepositories`, the `ignite-client.addresses` property binding, and `VALUE_TYPE`/`KEY_TYPE` schema bindings already in place. If you are doing the training, start from [`gg8_docker`](https://github.com/GridGain-Demos/spring-data-training/tree/gg8_docker) instead and use [Diffing Against the Skeleton](#diffing-against-the-skeleton) to check your work.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Layout](#project-layout)
- [1. Clone the Project](#1-clone-the-project)
- [2. Start the Cluster](#2-start-the-cluster)
- [3. Load the World Dataset](#3-load-the-world-dataset)
- [4. Build](#4-build)
- [5. Run the Application](#5-run-the-application)
- [6. Verify the Endpoints](#6-verify-the-endpoints)
- [7. Stop the Application](#7-stop-the-application)
- [8. Shutdown](#8-shutdown)
- [Diffing Against the Skeleton](#diffing-against-the-skeleton)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Git
- Docker Desktop
- Your favorite IDE (IntelliJ, Eclipse, VS Code, or a plain editor)

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
git clone -b gg8_docker_solution https://github.com/GridGain-Demos/spring-data-training.git
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

## 3. Load the World Dataset

```bash
docker compose -f docker/docker-compose.yaml exec -T node1 /opt/gridgain/bin/sqlline.sh -u "jdbc:ignite:thin://127.0.0.1/" --silent=true < config/world.sql
```

Verify row counts:

```bash
printf 'SELECT COUNT(*) FROM Country;\nSELECT COUNT(*) FROM City;\n!quit\n' | docker compose -f docker/docker-compose.yaml exec -T node1 /opt/gridgain/bin/sqlline.sh -u "jdbc:ignite:thin://127.0.0.1/" --silent=true
```

Expect **239** countries and **4079** cities.

---

## 4. Build

Two paths — pick whichever suits your environment.

### Standalone (host Maven)

The cluster can stay up during a host Maven build.

```bash
mvn clean package -DskipTests
```

### Docker

The cluster can stay up — the build writes to `libs/`, which the server nodes do not mount.

```bash
docker compose -f docker/docker-compose.yaml run --rm app mvn -B clean package -DskipTests
```

Both paths produce `libs/app.jar` — the Spring Boot fat jar.

---

## 5. Run the Application

The app exposes REST endpoints on port 18080. Wait approximately 15 seconds after starting for the `Started Application in …` log line.

### Standalone

```bash
java @src/main/resources/j17.params -jar libs/app.jar --server.port=18080 &
```

### Docker

```bash
docker compose -f docker/docker-compose.yaml run --rm -p 18080:18080 --name sd-app app java @/work/src/main/resources/j17.params -jar /work/libs/app.jar --server.port=18080 &
```

`IGNITE_ADDRESS=node1:10800` is baked into the compose service so the `app` container reaches the cluster automatically.

---

## 6. Verify the Endpoints

```bash
curl -s -w "HTTP %{http_code}\n" "http://localhost:18080/api/mostPopulated?limit=5"
```

Expect `[["Mumbai (Bombay)",10500000,"India"],["Seoul",...],...]` with HTTP 200.

---

## 7. Stop the Application

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

## 8. Shutdown

```bash
docker compose -f docker/docker-compose.yaml down
```

The `docker/data/` directory is kept on the host (holds logs and marshaller metadata; only gains `db/` and `wal/` subdirectories when persistence is enabled).

---

## Diffing Against the Skeleton

```bash
git diff gg8_docker..gg8_docker_solution
```

Shows exactly the code a student adds during the training: the repository interfaces, the REST controller, the `@EnableIgniteRepositories` annotation, the dependency additions in `pom.xml`, the `VALUE_TYPE` and `KEY_TYPE` bindings in `world.sql`, and the `ignite-client.addresses` property.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `docker compose up -d` hangs on the second attempt | Port 10800 still held by a cluster running in another directory | `docker compose -f docker/docker-compose.yaml down` in that directory first |
| Nodes start but produce no logs; `docker/data/` empty (Linux only) | Container runs as UID 10000; host `docker/data/` owned by your user | `chown -R 10000:10000 docker/data/` |
| `InaccessibleObjectException: Unable to make field long java.nio.Buffer.address accessible` | Missing `@src/main/resources/j17.params` before `-jar` | Add the `@` argfile argument |
| `Web server failed to start. Port 8080 was already in use.` | Something on the host owns port 8080 | Pass `--server.port=18080` (already in the commands above) |
| Sidecar: `Connection refused` to thin client | `IGNITE_ADDRESS` env var not set or compose service using `localhost` | Check `environment:` block in `docker/docker-compose.yaml` sets `IGNITE_ADDRESS=node1:10800` |
| `NoClassDefFoundError: InvalidDataAccessApiUsageException` | `ignite-spring-data-ext:3.1.0` omits `spring-tx` as a transitive dependency | `pom.xml` already adds `spring-tx` explicitly as the workaround — verify it is present |
