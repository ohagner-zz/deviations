package com.ohagner.deviations.api.deviation.repository

import com.ohagner.deviations.api.deviation.domain.Deviation

interface DeviationRepository {

    List<Deviation> retrieveAll()

}