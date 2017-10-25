package com.ohagner.deviations.api.stop.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.ohagner.deviations.api.stop.repository.StopRepository
import com.ohagner.deviations.api.stop.repository.trafiklab.HttpStopRepository
import com.ohagner.deviations.config.AppConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.func.Action
import ratpack.http.client.HttpClient
import ratpack.http.client.HttpClientSpec

import java.time.Duration

@Slf4j
class StopsModule extends AbstractModule {

    String schemeAndHost
    String path
    String apiKey

    @Override
    protected void configure() {
        schemeAndHost = AppConfig.envOrProperty("TRAFIKLAB_BASE_URL")
        path = AppConfig.envOrProperty("TRAFIKLAB_STOPS_LOOKUP_PATH")
        apiKey = AppConfig.envOrProperty("TRAFIKLAB_STOP_LOOKUP_API_KEY")
    }

    @Provides
    @CompileStatic
    @Singleton
    StopRepository createStopRepo() {
        URI baseUri = URI.create("$schemeAndHost$path?key=$apiKey")
        log.info "Initialized HttpStopRepository with baseUri: $schemeAndHost$path?key=${apiKey.substring(0,5)}***"
        HttpClient httpClient = HttpClient.of(Action.from({ HttpClientSpec spec ->
            spec.readTimeout(Duration.ofSeconds(3l))
        }))
        return new HttpStopRepository(httpClient, baseUri)
    }

}
