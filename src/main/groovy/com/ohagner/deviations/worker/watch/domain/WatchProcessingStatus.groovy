package com.ohagner.deviations.worker.watch.domain

enum WatchProcessingStatus {
    STARTED, NOT_TIME_TO_CHECK, FAILED, MATCHED, NO_MATCH, NOTIFIED, NOTIFICATION_FAILED, WATCH_UPDATE_FAILED
}
