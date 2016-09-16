package com.ohagner.deviations.module

import com.gmongo.GMongoClient
import com.google.inject.Provides
import com.mongodb.MongoClientURI
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.guice.ConfigurableModule

@Slf4j
class MongoModule extends ConfigurableModule<MongoConfig> {


    @Override
    protected void configure() {

    }

    @Provides
    UserRepository provideUserRepository(MongoConfig mongoConfig) {
        try {
            log.info "Initializing DB with connection uri: ${mongoConfig.connectionUri}"
            MongoClientURI mongoClientURI = new MongoClientURI(mongoConfig.connectionUri)
            GMongoClient client = new GMongoClient(mongoClientURI)
            new UserRepository(client.getDB(mongoConfig.databaseName).getCollection(mongoConfig.userCollectionName))
        } catch(Exception e) {
            log.error("Failed to create DB connection", e)
        }
    }
}
