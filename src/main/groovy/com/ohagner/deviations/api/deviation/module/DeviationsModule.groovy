package com.ohagner.deviations.api.deviation.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.ohagner.deviations.api.deviation.repository.CachedDeviationRepository
import com.ohagner.deviations.api.deviation.repository.DeviationRepository
import com.ohagner.deviations.api.deviation.repository.HttpDeviationRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import wslite.rest.RESTClient

import java.time.Duration

@Slf4j
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
        log.info "Creating cached deviation repo with ${Duration.ofMinutes(config.cacheTimeoutInMinutes).toMinutes()} minute cache timeout"
        RESTClient client = new RESTClient(config.baseUrl + config.deviationsResourcePath)
        return new CachedDeviationRepository(new HttpDeviationRepository(client, config.apiKey), Duration.ofMinutes(config.cacheTimeoutInMinutes))
    }

}
