package com.ohagner.deviations.config

import groovy.util.logging.Slf4j

import static com.ohagner.deviations.config.AppConfig.*

@Slf4j
class MongoConfig {
    String host
    int port
    String username
    String password
    String userDatabaseName
    String databaseName
    String userCollectionName
    String watchCollectionName
    String counterCollectionName

    public static final String HOST = "MONGO_HOST"
    public static final String PORT = "MONGO_PORT"

    public static final String USERNAME = "MONGO_USERNAME"
    public static final String PASSWORD = "MONGO_PASSWORD"

    public static final String USER_DATABASE_NAME = "MONGO_USER_DATABASE_NAME"
    public static final String DATABASE_NAME = "MONGO_DATABASE_NAME"
    public static final String USER_COLLECTION_NAME = "MONGO_USER_COLLECTION_NAME"
    public static final String WATCH_COLLECTION_NAME = "MONGO_WATCH_COLLECTION_NAME"
    public static final String COUNTER_COLLECTION_NAME = "MONGO_COUNTER_COLLECTION_NAME"

    static MongoConfig getInstance() {
        MongoConfig instance = new MongoConfig()
        instance.with {
            host = envOrProperty(HOST)
            port = envOrProperty(PORT) as int
            userDatabaseName = envOrProperty(USER_DATABASE_NAME)
            username = envOrProperty(USERNAME)
            password = envOrProperty(PASSWORD)
            databaseName = envOrProperty(DATABASE_NAME)
            userCollectionName = envOrProperty(USER_COLLECTION_NAME)
            watchCollectionName = envOrProperty(WATCH_COLLECTION_NAME)
            counterCollectionName = envOrProperty(COUNTER_COLLECTION_NAME)
        }
        log.debug "Initializing mongoDB with configuration:\n instance.toString()"
        return instance
    }


    @Override
    public String toString() {
        return "MongoConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", userCollectionName='" + userCollectionName + '\'' +
                ", watchCollectionName='" + watchCollectionName + '\'' +
                ", counterCollectionName='" + counterCollectionName + '\'' +
                '}';
    }
}
