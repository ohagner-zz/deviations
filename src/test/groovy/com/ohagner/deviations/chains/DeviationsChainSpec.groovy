package com.ohagner.deviations.chains

import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.deviation.endpoint.DeviationsChain
import com.ohagner.deviations.api.deviation.repository.DeviationRepository
import com.ohagner.deviations.api.transport.domain.TransportMode
import com.ohagner.deviations.api.user.endpoint.UserAuthorizationHandler
import ratpack.exec.Promise
import ratpack.jackson.JsonRender
import ratpack.test.handling.RequestFixture
import spock.lang.Specification

class DeviationsChainSpec extends Specification {

    private static final Deviation BUS_1 = new Deviation(transportMode: TransportMode.BUS, lineNumbers: ["800"])
    private static final Deviation BUS_2 = new Deviation(transportMode: TransportMode.BUS, lineNumbers: ["801"])
    private static final Deviation TRAIN = new Deviation(transportMode: TransportMode.TRAIN, lineNumbers: ["35", "36"])
    private static final Deviation SUBWAY = new Deviation(transportMode: TransportMode.METRO, lineNumbers: ["1"])

    DeviationRepository deviationRepository
    RequestFixture requestFixture

    static List<Deviation> allDeviations

    def setupSpec() {
        allDeviations = [BUS_1, BUS_2, TRAIN, SUBWAY]
    }

    def setup() {
        deviationRepository = Mock(DeviationRepository)
        requestFixture = RequestFixture.requestFixture()
                .registry { registry ->
            registry.add(UserAuthorizationHandler, new UserAuthorizationHandler())
        }
    }

    def 'should return all deviations'() {
        when:
            def result = requestFixture
                    .uri("")
                    .method("GET")
                    .handleChain(new DeviationsChain(deviationRepository))
        then:
            1 * deviationRepository.retrieveAll() >> Promise.sync { allDeviations }
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == allDeviations
    }

    def 'should only return all TRAIN deviations'() {
        when:
            def result = requestFixture
                    .uri("TRAIN")
                    .method("GET")
                    .handleChain(new DeviationsChain(deviationRepository))
        then:
            1 * deviationRepository.retrieveAll() >> Promise.sync { allDeviations }
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == [TRAIN]
    }

    def 'should only return all BUS deviations'() {
        when:
            def result = requestFixture
                    .uri("BUS")
                    .method("GET")
                    .handleChain(new DeviationsChain(deviationRepository))
        then:
            1 * deviationRepository.retrieveAll() >> Promise.sync { allDeviations }
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == [BUS_1, BUS_2]
    }

    def 'should only return correct bus line deviation'() {
        when:
            def result = requestFixture
                    .uri("BUS/${BUS_2.lineNumbers[0]}")
                    .method("GET")
                    .handleChain(new DeviationsChain(deviationRepository))
        then:
            1 * deviationRepository.retrieveAll() >> Promise.sync { allDeviations }
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == [BUS_2]
    }
}
