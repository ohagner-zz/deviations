package com.ohagner.deviations.module

import com.gmongo.GMongoClient
import com.google.inject.Provides
import com.mongodb.DB
import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
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
    UserRepository provideUserRepository(MongoConfig mongoConfig) {
        try {
            MongoCredential
            log.info "Initializing DB with connection uri: ${mongoConfig.connectionUri} to get access to ${mongoConfig.userCollectionName} collection"
            MongoClientURI uri  = new MongoClientURI("mongodb://deviationsmongo:Laxen2004@ds033056.mlab.com:33056/heroku_w7fp9nkc?authSource=heroku_w7fp9nkc&authMechanism=SCRAM-SHA-1");
            GMongoClient client = new GMongoClient(uri);
            DB db = client.getDB(uri.getDatabase());
            new UserRepository(db.getCollection(mongoConfig.userCollectionName))
        } catch (Exception e) {
            log.error("Failed to create DB connection", e)
            throw e
        }
    }
}
