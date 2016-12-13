package com.ohagner.deviations.worker.deviation.service

import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.watch.domain.Watch

interface DeviationsApiClient {

    boolean sendNotifications(Watch watch, Set<Deviation> matchingDeviations)

    boolean update(Watch watch)
}