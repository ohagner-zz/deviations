package com.ohagner.deviations.watch.task

import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.Watch

interface DeviationsApiClient {

    boolean sendNotifications(Watch watch, Set<Deviation> matchingDeviations)

    boolean update(Watch watch)
}