package com.ohagner.deviations.module

import com.gmongo.GMongoClient
import com.google.inject.Provides
import com.mongodb.MongoClientURI
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.repository.UserRepository
import ratpack.guice.ConfigurableModule

class MongoModule extends ConfigurableModule<MongoConfig> {


    @Override
    protected void configure() {

    }

    @Provides
    UserRepository provideUserRepository(MongoConfig mongoConfig) {
        MongoClientURI mongoClientURI = new MongoClientURI(mongoConfig.connectionUri)
        GMongoClient client = new GMongoClient(mongoClientURI)
        new UserRepository(client.getDB(mongoConfig.databaseName).getCollection(mongoConfig.userCollectionName))
    }
}
