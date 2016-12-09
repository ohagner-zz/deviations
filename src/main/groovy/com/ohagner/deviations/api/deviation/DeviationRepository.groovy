package com.ohagner.deviations.api.deviation

import com.ohagner.deviations.api.deviation.Deviation

interface DeviationRepository {

    List<Deviation> retrieveAll()

}