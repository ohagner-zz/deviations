package com.ohagner.deviations.api.journey.repository.trafiklab

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ohagner.deviations.api.journey.domain.Journey
import com.ohagner.deviations.api.journey.domain.JourneySearch
import com.ohagner.deviations.api.stop.domain.Stop
import org.junit.Rule
import ratpack.exec.ExecResult
import ratpack.http.client.HttpClient
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import static com.github.tomakehurst.wiremock.client.WireMock.get


class HttpJourneyRepositorySpec extends Specification {

    public static final int PORT = 8089

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT)

    @AutoCleanup
    ExecHarness execHarness = ExecHarness.harness()

    HttpClient httpClient = HttpClient.of { spec -> spec.readTimeout(Duration.ofSeconds(10l)) }
    HttpJourneyRepository repository = new HttpJourneyRepository(httpClient: httpClient, baseUri: URI.create("http://localhost:8089/test"))


    void 'retrieve and transform journey data'() {
        given:
            def mockedBackendResponse = new File("src/test/resources/trafiklab/reseplanerare/reseplanerareResponse.json").text
            wireMockRule.stubFor(get(anyUrl())
                    .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockedBackendResponse)
                    .withStatus(200)))
            JourneySearch journeySearch = new JourneySearch(origin: new Stop(externalId: "1234"), destination: new Stop(externalId: "2345"), from: LocalDateTime.now(), to: LocalDateTime.now())
        expect:
            ExecResult<List<Journey>> result = execHarness.yieldSingle {
                repository.search(journeySearch)
            }
            result.value.size() == 5
    }
    
}
