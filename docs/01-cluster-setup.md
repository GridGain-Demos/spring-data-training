# Module 1: Cluster Setup

Before writing any code, you need an Ignite cluster to connect to. In this module, you'll start a three-node cluster using Docker Compose and load sample data.

## Step 1: Clone the Project

Open a terminal and clone the project:

```bash
git clone https://github.com/GridGain-Demos/spring-data-training.git
cd spring-data-training
```

## Step 2: Start the Cluster

Start a three-node cluster with Docker Compose.

**For Apache Ignite 3:**

```bash
docker compose -f docker-compose.yml up -d
```

**For GridGain 9:**

```bash
docker compose -f docker-compose-gg9.yml up -d
```

This starts three Ignite nodes in the background. Give them a few seconds to discover each other.

## Step 3: Initialize the Cluster

New Ignite clusters need to be initialized before they accept connections. This might seem odd if you're used to traditional databases that are ready the moment they start. The difference is that Ignite is a distributed system. Before nodes can accept client connections, they need to agree on who's in the cluster, where metadata lives, and how to coordinate.

The initialization step establishes the **metastorage group**, which is the set of nodes responsible for storing cluster metadata (schema definitions, configuration, etc.). You typically want at least two nodes in this group for redundancy. If you lose all metastorage nodes, you lose the cluster's brain.

You'll do this through the Ignite CLI.

**Start the CLI for Apache Ignite 3:**

```bash
docker run -v ./config/world.sql:/opt/ignite/downloads/world.sql \
  --rm --network spring-boot-data-training_default \
  -it apacheignite/ignite:3.1.0 cli
```

**Or for GridGain 9** (make sure your license file is in the current directory):

```bash
docker run \
  -v ./gridgain-license.json:/opt/ignite/downloads/gridgain-license.json \
  -v ./config/world.sql:/opt/ignite/downloads/world.sql \
  --rm --network spring-boot-data-training_default \
  -it gridgain/gridgain9:9.1.8 cli
```

Once the CLI starts, connect to the cluster:

```bash
connect http://node1:10300
```

Now initialize it.

**For Apache Ignite 3:**

```bash
cluster init --name=spring-data-training --metastorage-group=node1,node2
```

**For GridGain 9:**

```bash
cluster init --name=spring-data-training --metastorage-group=node1,node2 \
  --license=/opt/ignite/downloads/gridgain-license.json
```

## Step 4: Load the Sample Data

Still in the CLI, load the World Database. This creates COUNTRY and CITY tables with real geographic data.

```bash
sql --file=/opt/ignite/downloads/world.sql
```

This takes a few seconds. When it finishes, you have 239 countries and 4,079 cities to query.

Keep the CLI window open. You'll come back to it later if you want to run queries directly.

## What You've Done

You now have a running three-node Ignite cluster with:

- Two tables: COUNTRY (239 rows) and CITY (4,079 rows)
- A metastorage group for cluster coordination
- Port 10800 exposed for client connections

## Next Module

Continue to [Module 2: Spring Boot Configuration](02-spring-boot-config.md) to connect your application to the cluster.
