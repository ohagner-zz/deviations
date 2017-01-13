package com.ohagner.deviations.worker.watch.domain

enum WatchProcessingStatus {
    STARTED, TIME_TO_CHECK, NOT_TIME_TO_CHECK, FAILED, MATCHED, NO_MATCH, NOTIFIED, UPDATED, NOTIFICATION_FAILED, UPDATE_FAILED
}
