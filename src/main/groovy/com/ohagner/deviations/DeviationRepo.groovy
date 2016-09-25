package com.ohagner.deviations

interface DeviationRepo {

    List<Deviation> retrieveAll()

    List<Deviation> getFiltered(Closure filter)

}