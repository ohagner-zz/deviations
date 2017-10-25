package com.ohagner.deviations.api.journey.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.config.Constants
import groovy.transform.ToString

import java.time.LocalDateTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@ToString
class JourneySearch {

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime from

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime to

    Stop origin
    Stop destination

}
