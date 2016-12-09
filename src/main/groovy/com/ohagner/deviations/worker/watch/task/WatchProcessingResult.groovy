package com.ohagner.deviations.worker.watch.task

import com.ohagner.deviations.api.deviation.Deviation
import groovy.json.JsonOutput

import java.time.Duration

class WatchProcessingResult {

    WatchProcessingStatus status
    List<String> messages = []
    Duration executionTime

    Set<Deviation> matchingDeviations = []

    def addMessage(def message) {
        messages.add(message)
    }


    @Override
    public String toString() {
        return JsonOutput.toJson {
            status(status)
            message(messages.join(". "))
            executionTime(executionTime.toMillis() + " ms")
            matchingDeviations(matchingDeviations ? matchingDeviations.collect { it.id }.join(",") : "[]")
        }
//        return "WatchProcessingResult{" +
//                "status=" + status +
//                ", message=" + messages.join(". ") +
//                ", executionTime=" + executionTime.toMillis() + " ms" +
//                ", matchingDeviations=" + matchingDeviations ? matchingDeviations.collect { it.id }.join(",") : "[]" +
//                '}'
    }
}

