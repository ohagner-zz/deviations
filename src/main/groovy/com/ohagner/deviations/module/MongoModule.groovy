package com.ohagner.deviations.module

import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.repository.UserRepository
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
            log.info "Initializing DB with connection uri: ${mongoConfig.connectionUri} to get access to ${mongoConfig.userCollectionName} collection"
            MongoCredential credential = MongoCredential.createCredential('deviationsmongo', 'admin', 'deviationsmongo' as char[])

//            MongoClientURI uri  = new MongoClientURI(mongoConfig.connectionUri)
            ServerAddress serverAddress = new ServerAddress()
            MongoClient otherClient = new MongoClient(serverAddress, [credential])
            log.info "Other Client created"
            GMongo client = new GMongo(otherClient)
            log.info "Client created"
            DB db = client.getDB('test')
            log.info "DB retrieved"
            new UserRepository(db.getCollection(mongoConfig.userCollectionName))
        } catch (Exception e) {
            log.error("Failed to create DB connection", e)
            throw e
        }
    }
}
