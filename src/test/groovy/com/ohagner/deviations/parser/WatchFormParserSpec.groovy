package com.ohagner.deviations.parser

import com.ohagner.deviations.web.watch.service.WatchFormParser
import spock.lang.Specification

class WatchFormParserSpec extends Specification {

    void 'should parse out the transport properties'() {
        given:
            def form = [:]
            form.put("notTransport[0].something", "notTransport")
            form.put("transport[0].line", "line0")
            form.put("transport[1].line", "line1")
            form.put("notTransport[1].somethingElse", "notTransport")
            form.put("transport[0].mode", "mode0")
            form.put("transport[1].mode", "mode1")
            form.put("name", "Name Namesson")
        when:
            List transports = WatchFormParser.getTransports(form)
        then:
            assert transports.size() == 2
            assert transports.any { transportMap -> transportMap.line == "line0" }
            assert transports.any { transportMap -> transportMap.line == "line1" }
            assert transports.any { transportMap -> transportMap.mode == "mode0" }
            assert transports.any { transportMap -> transportMap.mode == "mode1" }
    }

    void 'should return empty list for no transport properties'() {
        given:
            def form = [:]
            form.put("notTransport[0].something", "notTransport")
            form.put("notTransport[1].somethingElse", "notTransportEither")
            form.put("name", "Name Namesson")
        when:
            List transports = WatchFormParser.getTransports(form)
        then:
            assert transports.size() == 0
    }

    void 'should parse out the notification types'() {
        given:
            def form = [:]
            form.put("something", "something")
            form.put("transport[0].line", "line0")
            form.put("transport[1].line", "line1")
            form.put("notifyByEmail", "on")
            form.put("name", "Name Namesson")
            form.put("notifyBySomethingElse", "on")
        when:
            List notificationTypes = WatchFormParser.getNotificationTypes(form)
        then:
            assert notificationTypes.size() == 2
            assert notificationTypes.containsAll("SOMETHINGELSE", "EMAIL")

    }

    void 'should return empty list when there are no notification types'() {
        given:
            def form = [:]
        when:
            List notificationTypes = WatchFormParser.getNotificationTypes(form)
        then:
            assert notificationTypes.size() == 0

    }

    void 'should parse out the weekdays'() {
        given:
            def form = [:]
            form.put("something", "something")
            form.put("MONDAY", "on")
            form.put("transport[1].line", "line1")
            form.put("THURSDAY", "on")
            form.put("name", "Name Namesson")
            form.put("notifyBySomethingElse", "on")
        when:
            def weekdays = WatchFormParser.getWeekdays(form)
        then:
            assert weekdays.size() == 2
            assert weekdays.containsAll("MONDAY", "THURSDAY")

    }

    void 'should return empty list when there are no weekdays'() {
        given:
            def form = [:]
        when:
            def weekdays = WatchFormParser.getWeekdays(form)
        then:
            assert weekdays.size() == 0

    }

}
