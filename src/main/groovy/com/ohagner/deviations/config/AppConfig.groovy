package com.ohagner.deviations.config

import com.ohagner.deviations.api.error.ConfigurationNotFoundException

class AppConfig {

    static ConfigObject config = readProperties()

    static Map env = System.getenv()

    static envOrProperty(String key) {
        def value = env."$key" ?: config."$key"
        if(!value) {
            throw new ConfigurationNotFoundException("Configuration missing for property: $key")
        }
        return value
    }

    static envOrDefault(String key, Object defaultValue) {
        return env."$key" ?: defaultValue
    }

    static readProperties() {
        Properties properties = new Properties()
        properties.load(this.getClassLoader().getResourceAsStream("config/app.properties"))
        return new ConfigSlurper().parse(properties)
    }

}
