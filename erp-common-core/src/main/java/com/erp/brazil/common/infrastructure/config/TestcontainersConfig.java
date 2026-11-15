package com.erp.brazil.common.infrastructure.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base Testcontainers configuration for integration tests.
 * Provides shared containers for MySQL, MongoDB, Redis, and RabbitMQ.
 * Extend this class in test classes to use the containers.
 */
@Testcontainers
public abstract class TestcontainersConfig {

    // MySQL 8.4
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("erp_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/init-test.sql")
            .withReuse(true);

    // MongoDB 7.0
    @Container
    protected static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withReuse(true);

    // Redis 7
    @Container
    protected static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)
            .waitingFor(Wait.forListeningPort());

    // RabbitMQ 3.13
    @Container
    protected static final RabbitMQContainer RABBITMQ_CONTAINER = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"))
            .withReuse(true);

    /**
     * Gets the MySQL JDBC URL for test configuration.
     *
     * @return JDBC URL
     */
    public static String getMysqlUrl() {
        return MYSQL_CONTAINER.getJdbcUrl();
    }

    /**
     * Gets the MySQL username.
     *
     * @return username
     */
    public static String getMysqlUsername() {
        return MYSQL_CONTAINER.getUsername();
    }

    /**
     * Gets the MySQL password.
     *
     * @return password
     */
    public static String getMysqlPassword() {
        return MYSQL_CONTAINER.getPassword();
    }

    /**
     * Gets the MongoDB connection string.
     *
     * @return connection string
     */
    public static String getMongoUri() {
        return MONGO_CONTAINER.getReplicaSetUrl("erp_test");
    }

    /**
     * Gets the Redis host.
     *
     * @return host
     */
    public static String getRedisHost() {
        return REDIS_CONTAINER.getHost();
    }

    /**
     * Gets the Redis port.
     *
     * @return port
     */
    public static int getRedisPort() {
        return REDIS_CONTAINER.getMappedPort(6379);
    }

    /**
     * Gets the RabbitMQ host.
     *
     * @return host
     */
    public static String getRabbitMqHost() {
        return RABBITMQ_CONTAINER.getHost();
    }

    /**
     * Gets the RabbitMQ AMQP port.
     *
     * @return port
     */
    public static int getRabbitMqPort() {
        return RABBITMQ_CONTAINER.getAmqpPort();
    }

    /**
     * Gets the RabbitMQ management port.
     *
     * @return port
     */
    public static int getRabbitMqManagementPort() {
        return RABBITMQ_CONTAINER.getManagementPort();
    }

    /**
     * Starts all containers if not already running.
     * Call this in @BeforeAll if not using @Testcontainers annotation.
     */
    public static void startAll() {
        MYSQL_CONTAINER.start();
        MONGO_CONTAINER.start();
        REDIS_CONTAINER.start();
        RABBITMQ_CONTAINER.start();
    }

    /**
     * Stops all containers.
     * Call this in @AfterAll if not using @Testcontainers annotation.
     */
    public static void stopAll() {
        MYSQL_CONTAINER.stop();
        MONGO_CONTAINER.stop();
        REDIS_CONTAINER.stop();
        RABBITMQ_CONTAINER.stop();
    }
}