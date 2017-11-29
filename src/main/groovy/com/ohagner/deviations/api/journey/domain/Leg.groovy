package com.ohagner.deviations.api.journey.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.api.transport.domain.Transport
import com.ohagner.deviations.config.Constants
import groovy.transform.EqualsAndHashCode

import java.time.LocalDateTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@EqualsAndHashCode
class Leg {
    Transport transport
    Stop origin
    Stop destination

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime departure

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime arrival
}
