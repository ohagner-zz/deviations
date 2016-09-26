package com.ohagner.deviations.modules

import com.gmongo.GMongo
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.repository.IncrementalCounter
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
class MongoModule extends AbstractModule {

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
            DB db = connectToDatabase(mongoConfig)
            return new UserRepository(db.getCollection(mongoConfig.userCollectionName))
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
            DB db = connectToDatabase(mongoConfig)
            IncrementalCounter counter = IncrementalCounter.createCounter(db.getCollection(mongoConfig.counterCollectionName), mongoConfig.watchCollectionName)
            return new WatchRepository(db.getCollection(mongoConfig.watchCollectionName), counter)
        } catch (Exception e) {
            log.error("Failed to create DB connection", e)
            throw e
        }
    }

    private DB connectToDatabase(MongoConfig mongoConfig) {
        log.info "Initializing DB with connection to host: ${mongoConfig.host}"
        MongoCredential credential = MongoCredential.createCredential(mongoConfig.username, 'admin', mongoConfig.password as char[])
        ServerAddress serverAddress = new ServerAddress(mongoConfig.host, mongoConfig.port)
        MongoClient mongoClient = new MongoClient(serverAddress, [credential])
        return new GMongo(mongoClient).getDB(mongoConfig.databaseName)
    }

}
