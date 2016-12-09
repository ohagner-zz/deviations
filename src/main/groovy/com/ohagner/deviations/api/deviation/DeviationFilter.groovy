package com.ohagner.deviations.api.deviation

import java.time.LocalDateTime

import static com.ohagner.deviations.config.Constants.ZONE_ID

final class DeviationFilter {

    private DeviationFilter() {}

    public static final int MAX_DEVIATION_DURATION_HOURS = 12

    static List<Deviation> apply(List<Deviation> deviationList) {

        return deviationList
                .findAll { it.getDuration().toHours() < MAX_DEVIATION_DURATION_HOURS }
                .findAll { it.to.isAfter(LocalDateTime.now(ZONE_ID)) }
    }

}
