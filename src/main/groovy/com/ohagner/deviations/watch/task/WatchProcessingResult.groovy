package com.ohagner.deviations.watch.task

import com.ohagner.deviations.domain.Deviation

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
        return "WatchProcessingResult{" +
                "status=" + status +
                ", message=" + messages.join(". ") +
                ", executionTime=" + executionTime.toMillis() + " ms" +
                ", matchingDeviations=" + matchingDeviations.collect { it.id }.join(",") +
                '}';
    }
}

