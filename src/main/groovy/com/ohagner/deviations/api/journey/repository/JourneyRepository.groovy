package com.ohagner.deviations.api.journey.repository

import com.ohagner.deviations.api.journey.domain.Journey
import com.ohagner.deviations.api.journey.domain.JourneySearch
import ratpack.exec.Promise


interface JourneyRepository {

    Promise<List<Journey>> search(JourneySearch journeySearch)
}
