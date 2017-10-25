package com.ohagner.deviations.api.journey.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.config.Constants
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDateTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@ToString
@EqualsAndHashCode
class Journey {

    Stop origin
    Stop destination

    List<Leg> legs = []

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime departure

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime arrival

}


