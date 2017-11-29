package com.ohagner.deviations.api.deviation.repository

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ohagner.deviations.api.deviation.domain.Deviation
import org.junit.Rule
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification
import wslite.rest.RESTClient

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.ohagner.deviations.config.Constants

import static com.github.tomakehurst.wiremock.client.WireMock.*

class HttpDeviationRepositorySpec extends Specification {

    DateTimeFormatter backendDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    public static final int PORT = 8089

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT)

    @AutoCleanup
    ExecHarness execHarness = ExecHarness.harness()

    HttpDeviationRepository repository = new HttpDeviationRepository(new RESTClient("http://localhost:$PORT"), "")

    void 'should return deviation list'() {
        given:
            String now = LocalDateTime.now(Constants.ZONE_ID).format(backendDateTimeFormat)
            String inOneHour = LocalDateTime.now(Constants.ZONE_ID).plusHours(1).format(backendDateTimeFormat)
            def mockedBackendResponse = new File("src/test/resources/deviations/trafiklabTrainDeviationResponse.json").text
                        .replaceAll('<NOW>', now)
                        .replaceAll('<IN_ONE_HOUR>', inOneHour)
            wireMockRule.stubFor(get(anyUrl())
                    .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockedBackendResponse)
                        .withStatus(200)))
        expect:
            List<Deviation> allDeviations = execHarness.yieldSingle {
                repository.retrieveAll()
            }.value
            allDeviations.size() == 6
    }
}
