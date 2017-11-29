package com.ohagner.deviations.modules

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoDatabase
import com.ohagner.deviations.api.user.repository.MongoUserRepository
import com.ohagner.deviations.api.user.repository.UserRepository
import com.ohagner.deviations.api.watch.repository.IncrementalCounter
import com.ohagner.deviations.api.watch.repository.MongoWatchRepository
import com.ohagner.deviations.api.watch.repository.WatchRepository
import com.ohagner.deviations.config.MongoConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.exec.Blocking

@Slf4j
class RepositoryModule extends AbstractModule {

    MongoConfig mongoConfig

    @Override
    protected void configure() {
        this.mongoConfig = MongoConfig.getInstance()
    }

    @Provides
    @CompileStatic
    @Singleton
    UserRepository provideUserRepository() {
        try {
            MongoDatabase db
            db = connectToDatabase(mongoConfig)
            log.info "Connected to user db"
            return new MongoUserRepository(db.getCollection(mongoConfig.userCollectionName))
        } catch (Exception e) {
            log.error("Failed to create DB connection", e)
            throw e
        }
    }

    @Provides
    @CompileStatic
    @Singleton
    WatchRepository provideWatchRepository() {
        try {
            MongoDatabase db

            db = connectToDatabase(mongoConfig)
            log.info "Connected to watch db"
            IncrementalCounter counter = IncrementalCounter.createCounter(db.getCollection(mongoConfig.counterCollectionName), mongoConfig.watchCollectionName)
            log.info "Connected to counter"
            return new MongoWatchRepository(db.getCollection(mongoConfig.watchCollectionName), counter)
        } catch (Exception e) {
            log.error("Failed to create DB connection", e)
            throw e
        }
    }

    @CompileStatic
    private MongoDatabase connectToDatabase(MongoConfig mongoConfig) {
        log.info "Initializing DB with connection to host: ${mongoConfig.host}"
        MongoClient mongoClient = MongoClients.create("mongodb://${mongoConfig.username}:${mongoConfig.password}@${mongoConfig.host}:${mongoConfig.port}/${mongoConfig.userDatabaseName}")
        return mongoClient.getDatabase(mongoConfig.databaseName)
    }

}
