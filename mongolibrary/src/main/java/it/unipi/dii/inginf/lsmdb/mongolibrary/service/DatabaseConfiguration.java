package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfiguration {
    private static final Logger LOGGER = LogManager.getLogger(DatabaseConfiguration.class);

    // Name of the configuration file
    private static final String FILENAME = "dbconfig.properties";

    // Properties
    private final String mongoUri;
    private final String mongoDatabaseName;
    private final String reportedReviewsId;

    private final String neo4jUri;
    private final String neo4jUsername;
    private final String neo4jPassword;
    private final int neo4jConnectionAcquisitionTimeout;
    private final int neo4jConnectionTimeout;


    /**
     * Allocate the class and read the configuration file
     */
    DatabaseConfiguration() {
        Properties properties = new Properties();

        // Get the configuration file from the resources of our application
        try (InputStream inputStream =
                     DatabaseConfiguration.class.getClassLoader().getResourceAsStream(FILENAME)
        ) {
            // Read the file and load the property fields
            properties.load(inputStream);

        } catch (IOException e) {
            LOGGER.fatal("Database configuration not loaded");
            throw new RuntimeException("Database configuration not loaded");
        }

        // Read the fields
        mongoUri = properties.getProperty("mongoUri");
        mongoDatabaseName = properties.getProperty("mongoDatabaseName");
        reportedReviewsId = properties.getProperty("reportedReviewsId");

        neo4jUri = properties.getProperty("neo4jUri");
        neo4jUsername = properties.getProperty("neo4jUsername");
        neo4jPassword = properties.getProperty("neo4jPassword");
        neo4jConnectionAcquisitionTimeout =
                Integer.parseInt(properties.getProperty("neo4jConnectionAcquisitionTimeout"));
        neo4jConnectionTimeout = Integer.parseInt(properties.getProperty("neo4jConnectionTimeout"));

        LOGGER.info("Database configuration loaded");
    }


    /**
     * Get the URI for MongoDB. URI contains the IPs and ports of the MongoDB servers.
     * @return URI for MongoDB
     */
    public String getMongoUri() {
        return mongoUri;
    }

    /**
     * Get the name of the MongoDB database
     * @return MongoDB database name
     */
    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    /**
     * Get the Id of the reported reviews in the database
     * @return reported reviews Id
     */
    public String getReportedReviewsId() {
        return reportedReviewsId;
    }

    /**
     * Get the URI for Neo4J. URI contains the IPs and ports of the NEO4J servers.
     * @return URI for Neo4J
     */
    public String getNeo4jUri() {
        return neo4jUri;
    }

    public String getNeo4jUsername() {
        return neo4jUsername;
    }

    public String getNeo4jPassword() {
        return neo4jPassword;
    }

    public int getNeo4jConnectionAcquisitionTimeout() {
        return neo4jConnectionAcquisitionTimeout;
    }

    public int getNeo4jConnectionTimeout() {
        return neo4jConnectionTimeout;
    }
}
