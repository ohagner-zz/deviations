package com.ohagner.deviations.config

import groovy.util.logging.Slf4j

import static com.ohagner.deviations.config.AppConfig.envOrDefault
import static com.ohagner.deviations.config.AppConfig.envOrProperty

@Slf4j
class DeviationsConfig {

    String baseUrl
    String deviationsResourcePath
    String apiKey
    int cacheTimeout

    public static final String BASE_URL = "TRAFIKLAB_BASE_URL"
    public static final String DEVIATIONS_PATH = "TRAFIKLAB_DEVIATIONS_PATH"
    public static final String API_KEY = "TRAFIKLAB_API_KEY"
    public static final String CACHE_TIMEOUT_MINUTES = "DEVIATIONS_CACHE_TIMEOUT_MINUTES"

    static DeviationsConfig getInstance() {
        DeviationsConfig instance = new DeviationsConfig()
        instance.with {
            baseUrl = envOrProperty(BASE_URL)
            deviationsResourcePath = envOrProperty(DEVIATIONS_PATH)
            apiKey = envOrProperty(API_KEY)
            cacheTimeout = envOrDefault(CACHE_TIMEOUT_MINUTES, 30)

        }
        log.debug "Initializing trafiklab client with config $instance"
        return instance
    }


    @Override
    public String toString() {
        return "DeviationsConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", deviationsResourcePath='" + deviationsResourcePath + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", cacheTimeout='" + cacheTimeout + '\'' +
                '}';
    }
}
