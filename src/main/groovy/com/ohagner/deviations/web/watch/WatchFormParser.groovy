package com.ohagner.deviations.web.watch

import java.time.DayOfWeek

class WatchFormParser {

    private static final String TRANSPORT_PROPERTY_PATTERN = /transport\[(\d*)\]\.(.*)/
    private static final String NOTIFY_BY_PATTERN = /notifyBy(.*)/

    private static final List<String> WEEKDAYS = DayOfWeek.values().collect { it.name() }

    static List<Map<String, String>> getTransports(Map form) {

        def transports = [:]
        form.each { key, value ->
            def matcher = (key =~ TRANSPORT_PROPERTY_PATTERN)
            if(matcher.matches()) {
                def index = matcher[0][1]
                def property = matcher[0][2]
                transports.get(index, [:]).put(property, value)
            }
        }
        return transports.collect { key, value -> value }
    }

    static List<String> getNotificationTypes(Map form) {
        def notificationTypes = []
        form.each { key, value ->
            def matcher = (key =~ NOTIFY_BY_PATTERN)
            if(matcher.matches()) {
                notificationTypes.add("${matcher[0][1]}".toUpperCase())
            }
        }
        return notificationTypes
    }

    static Set<String> getWeekdays(Map form) {
        return form.keySet().findAll { key ->
            WEEKDAYS.contains(key)
        }
    }
}
