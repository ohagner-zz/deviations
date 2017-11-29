package com.ohagner.deviations.api.stop.repository.trafiklab

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ohagner.deviations.api.stop.domain.Stop
import groovy.util.logging.Slf4j
import org.junit.Rule
import ratpack.exec.ExecResult
import ratpack.http.client.HttpClient
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.Duration

import static com.github.tomakehurst.wiremock.client.WireMock.*

@Slf4j
class HttpStopRepositorySpec extends Specification {

    public static final int PORT = 8089

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT)

    @AutoCleanup
    ExecHarness execHarness = ExecHarness.harness()

    HttpClient httpClient = HttpClient.of { spec -> spec.readTimeout(Duration.ofSeconds(10l)) }
    HttpStopRepository repository = new HttpStopRepository(httpClient: httpClient, baseUri: URI.create("http://localhost:8089/test"))


    void 'retrieve and transform stop data'() {
        given:
            def mockedBackendResponse = new File("src/test/resources/stops/trafiklab/stopSearchResponse.json").text
            wireMockRule.stubFor(get(anyUrl())
                    .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockedBackendResponse)
                    .withStatus(200)))
        expect:
            ExecResult<List<Stop>> result = execHarness.yieldSingle {
                repository.findStops("Somewhere")
            }
            result.value.size() == 10
    }

}
