package com.ohagner.deviations.modules

import com.gmongo.GMongo
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.guice.ConfigurableModule

@Slf4j
class MongoModule extends ConfigurableModule<MongoConfig> {


    @Override
    protected void configure() {

    }

    @Provides
    @CompileStatic
    @Singleton
    UserRepository provideUserRepository(MongoConfig mongoConfig) {
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
    WatchRepository provideWatchRepository(MongoConfig mongoConfig) {
        try {
            DB db = connectToDatabase(mongoConfig)
            return new WatchRepository(db.getCollection(mongoConfig.watchCollectionName))
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
