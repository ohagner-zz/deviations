package com.ohagner.deviations.worker.watch.task

import com.ohagner.deviations.api.deviation.Deviation
import com.ohagner.deviations.api.watch.Watch

interface DeviationsApiClient {

    boolean sendNotifications(Watch watch, Set<Deviation> matchingDeviations)

    boolean update(Watch watch)
}