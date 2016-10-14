package com.ohagner.deviations.config

class AppConfig {

    static ConfigObject config = readProperties()

    static Map env = System.getenv()

    static envOrProperty(String key) {
        return env."$key" ?: config."$key"
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
