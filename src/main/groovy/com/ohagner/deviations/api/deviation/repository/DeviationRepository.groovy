package com.ohagner.deviations.api.deviation.repository

import com.ohagner.deviations.api.deviation.domain.Deviation
import ratpack.exec.Promise

interface DeviationRepository {

    Promise<List<Deviation>> retrieveAll()

}