package com.ohagner.deviations.api.journey.domain

import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.api.transport.domain.Transport
import groovy.transform.EqualsAndHashCode

import java.time.LocalDateTime

@EqualsAndHashCode
class Leg {
    Transport transport
    Stop origin
    Stop destination
    LocalDateTime departure
    LocalDateTime arrival
}
