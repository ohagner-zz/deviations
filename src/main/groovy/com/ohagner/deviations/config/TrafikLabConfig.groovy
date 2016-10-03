package com.ohagner.deviations.config

import groovy.util.logging.Slf4j

import static com.ohagner.deviations.config.AppConfig.envOrProperty

@Slf4j
class TrafikLabConfig {

    String baseUrl
    String deviationsResourcePath
    String apiKey

    public static final String BASE_URL = "TRAFIKLAB_BASE_URL"
    public static final String DEVIATIONS_PATH = "TRAFIKLAB_DEVIATIONS_PATH"
    public static final String API_KEY = "TRAFIKLAB_API_KEY"

    static TrafikLabConfig getInstance() {
        TrafikLabConfig instance = new TrafikLabConfig()
        instance.with {
            baseUrl = envOrProperty(BASE_URL)
            deviationsResourcePath = envOrProperty(DEVIATIONS_PATH)
            apiKey = envOrProperty(API_KEY)
        }
        log.info "Initializing trafiklab client with config $instance"
        return instance
    }


    @Override
    public String toString() {
        return "TrafikLabConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", deviationsResourcePath='" + deviationsResourcePath + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
