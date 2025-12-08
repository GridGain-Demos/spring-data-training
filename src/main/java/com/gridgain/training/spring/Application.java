package com.gridgain.training.spring;

import org.apache.ignite.Ignite;
import org.apache.ignite.sql.ResultSet;
import org.apache.ignite.sql.SqlRow;
import org.apache.ignite.sql.Statement;
import org.apache.ignite.table.KeyValueView;
import org.apache.ignite.table.RecordView;
import org.apache.ignite.table.Table;
import org.apache.ignite.table.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Spring Boot entry point for the World Database application.
 *
 * This class demonstrates three key integration points between Spring Boot and Apache Ignite 3:
 *
 * 1. {@code @SpringBootApplication} - Standard Spring Boot bootstrap that triggers component scanning
 *    and auto-configuration. The spring-boot-starter-ignite-client dependency enables automatic
 *    creation of an Ignite thin client connection.
 *
 * 2. {@code @EnableJdbcRepositories} - Activates Spring Data JDBC repository support. Combined with
 *    the IgniteDialectProvider configured in META-INF/spring.factories, this allows Spring Data
 *    repositories to generate SQL compatible with Ignite's distributed SQL engine.
 *
 * 3. {@code Ignite} injection - The thin client instance is auto-configured by
 *    spring-boot-starter-ignite-client based on the ignite.client.addresses property. This client
 *    provides access to the Tables API and Cluster API for direct Ignite operations.
 *
 * The application uses two separate connection mechanisms:
 * - Thin client (port 10800) for Ignite-native APIs (tables, cluster topology)
 * - JDBC driver (same port) for Spring Data repository operations
 */
@SpringBootApplication
@EnableJdbcRepositories
public class Application {

	/**
	 * Ignite thin client instance, auto-configured by spring-boot-starter-ignite-client.
	 * Connection address is specified via ignite.client.addresses in application.properties.
	 */
	@Autowired
	Ignite ignite;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	private Logger log = LoggerFactory.getLogger(Application.class);

	/**
	 * Diagnostic logging executed after Spring context initialization completes.
	 * Verifies cluster connectivity by querying table metadata and node topology.
	 *
	 * The Tables API (ignite.tables()) provides schema introspection for all tables
	 * in the cluster. The Cluster API (ignite.cluster()) exposes topology information
	 * including node identifiers and network addresses.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void startupLogger() {
		log.info("Table names existing in cluster: {}", ignite.tables().tables().stream().map(Table::name).toList());

		log.info("Node information:");
		for (var n : ignite.cluster().nodes()) {
			log.info("ID: {}, Name: {}, Address: {}", n.id(), n.name(), n.address());
		}
	}

	/**
	 * Demonstrates the three primary ways to access data in Apache Ignite 3.
	 *
	 * Ignite 3 provides three distinct APIs for data access, each suited to different use cases:
	 *
	 * 1. RecordView: Works with complete records as Tuples or mapped POJOs. Best when you need
	 *    the entire record or want to work with strongly-typed objects.
	 *
	 * 2. KeyValueView: Separates key and value into distinct objects. Useful when you only need
	 *    the value portion or want explicit control over key construction.
	 *
	 * 3. SQL API: Standard SQL queries via Statement builder. Best for complex queries with
	 *    JOINs, aggregations, or when you prefer a declarative approach.
	 *
	 * This training uses Spring Data repositories (which use SQL under the hood) because they
	 * provide familiar patterns and reduce boilerplate. However, understanding all three APIs
	 * helps you choose the right tool for production scenarios.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void demonstrateDataAccessApis() {
		log.info("--- Demonstrating Ignite 3 Data Access APIs ---");

		// Get a table reference. Tables are the entry point for all data operations.
		Table cityTable = ignite.tables().table("CITY");

		// 1. RecordView: Work with complete records as Tuples
		// The recordView() method returns a view that treats rows as single Tuple objects.
		// Each Tuple contains all columns from the record.
		RecordView<Tuple> recordView = cityTable.recordView();

		// Create a key Tuple containing only the primary key column(s)
		Tuple key = Tuple.create().set("ID", 34);

		// The first parameter is the transaction context. Passing null uses an implicit
		// auto-commit transaction, which is fine for read operations.
		Tuple record = recordView.get(null, key);
		log.info("RecordView result: City ID 34 = {} (population: {})",
				record.stringValue("NAME"), record.intValue("POPULATION"));

		// 2. KeyValueView: Separate key and value objects
		// The keyValueView() separates the primary key columns from the value columns.
		// This is useful when you want to work with just the value portion.
		KeyValueView<Tuple, Tuple> kvView = cityTable.keyValueView();

		// The get() returns only the value Tuple (non-key columns)
		Tuple value = kvView.get(null, key);
		log.info("KeyValueView result: City ID 34 value = {} in {}",
				value.stringValue("NAME"), value.stringValue("DISTRICT"));

		// 3. SQL API with Statement builder
		// For complex queries, the SQL API provides full query capabilities.
		// The Statement builder allows parameterized queries with positional arguments.
		Statement stmt = ignite.sql().statementBuilder()
				.query("SELECT name, population FROM CITY WHERE id = ?")
				.build();

		// ResultSet implements AutoCloseable, so use try-with-resources
		try (ResultSet<SqlRow> rs = ignite.sql().execute(null, stmt, 34)) {
			if (rs.hasNext()) {
				SqlRow row = rs.next();
				log.info("SQL API result: City = {}, Population = {}",
						row.stringValue("NAME"), row.intValue("POPULATION"));
			}
		}

		log.info("--- End of API Demonstration ---");
	}

}
