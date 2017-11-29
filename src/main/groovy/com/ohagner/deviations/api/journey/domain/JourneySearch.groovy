package com.ohagner.deviations.api.journey.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.config.Constants
import groovy.transform.ToString

import java.time.LocalDate
import java.time.LocalTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@ToString
class JourneySearch {

    Stop origin

    Stop destination

    TimeSearchType timeRelatesTo

    @JsonFormat(pattern=Constants.Date.SHORT_DATE_FORMAT, shape=STRING)
    LocalDate date

    @JsonFormat(pattern=Constants.Date.TIME_FORMAT, shape=STRING)
    LocalTime time


    static enum TimeSearchType {
        EARLIEST_DEPARTURE, LATEST_ARRIVAL
    }

}

