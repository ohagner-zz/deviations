package com.ohagner.deviations.api.stop.repository

import com.ohagner.deviations.api.stop.domain.Stop
import ratpack.exec.Promise

interface StopRepository {

    Promise<List<Stop>> findStops(String searchString)

}
