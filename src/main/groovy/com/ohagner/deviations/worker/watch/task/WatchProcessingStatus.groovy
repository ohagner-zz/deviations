package com.ohagner.deviations.worker.watch.task

enum WatchProcessingStatus {
    STARTED, NOT_TIME_TO_CHECK, FAILED, MATCHED, NO_MATCH, NOTIFIED, NOTIFICATION_FAILED, WATCH_UPDATE_FAILED
}
