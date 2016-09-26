package com.ohagner.deviations.repository

import com.ohagner.deviations.domain.Deviation

interface DeviationRepository {

    List<Deviation> retrieveAll()

    List<Deviation> getFiltered(Closure filter)

}