package com.ohagner.deviations.api.deviation.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.ohagner.deviations.api.deviation.repository.CachedDeviationRepository
import com.ohagner.deviations.api.deviation.repository.DeviationRepository
import com.ohagner.deviations.api.deviation.repository.HttpDeviationRepository
import groovy.transform.CompileStatic
import wslite.rest.RESTClient

import java.time.Duration

class DeviationsModule extends AbstractModule {

    DeviationsConfig config

    @Override
    protected void configure() {
        config = DeviationsConfig.getInstance()
    }

    @Provides
    @CompileStatic
    @Singleton
    DeviationRepository createDeviationRepo() {
        RESTClient client = new RESTClient(config.baseUrl + config.deviationsResourcePath)
        return new CachedDeviationRepository(new HttpDeviationRepository(client, config.apiKey), Duration.ofMinutes(config.cacheTimeout))
    }

}
