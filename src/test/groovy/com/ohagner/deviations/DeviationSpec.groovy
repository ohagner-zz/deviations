package com.ohagner.deviations

import com.ohagner.deviations.domain.Deviation

import spock.lang.Specification

import java.time.LocalDateTime

import static org.junit.Assert.assertEquals

class DeviationSpec extends Specification {

    def json = """
    {
      "Created": "2016-05-03T15:14:22.72+02:00",
      "MainNews": false,
      "SortOrder": 1,
      "Header": "Inställd delsträcka mot Nynäshamn",
      "Details": "Tåg från Bålsta kl  14:53 mot Nynäshamn är inställd mellan Kallhäll och Jakobsberg pga vagnfel.\n\n Inställd Kallhäll kl  15:08.",
      "Scope": "Pendeltåg Nynäshamn-Bålsta-linje 35",
      "DevCaseGid": 9076001009188221,
      "DevMessageVersionNumber": 1,
      "ScopeElements": "Pendeltåg 35, 36",
      "FromDateTime": "2016-05-03T14:48:00",
      "UpToDateTime": "2016-05-03T17:02:00",
      "Updated": "2016-05-03T16:40:50.32+02:00"
    }
    """

    def "parse json to deviation"() {
        given:
            Deviation expected = Deviation.builder()
                .id("9076001009188221")
                .transportMode(Deviation.TransportMode.TRAIN)
                .header("Inställd delsträcka mot Nynäshamn")
                .details("Tåg från Bålsta kl  14:53 mot Nynäshamn är inställd mellan Kallhäll och Jakobsberg pga vagnfel.\nInställd Kallhäll kl  15:08.")
                .lineNumbers(["35", "36"])
                .created(LocalDateTime.of(2016, 5, 3, 15, 14, 22, 720_000_000))
                .from(LocalDateTime.of(2016, 5, 3, 14, 48, 0))
                .to(LocalDateTime.of(2016, 5, 3, 17, 02, 0))
                .updated(LocalDateTime.of(2016, 5, 3, 16, 40, 50, 320_000_000))
                .build()
        when:
            Deviation actual = Deviation.fromJson(json, Deviation.TransportMode.TRAIN)
        then:
            assertEquals(expected, actual)
    }
}
