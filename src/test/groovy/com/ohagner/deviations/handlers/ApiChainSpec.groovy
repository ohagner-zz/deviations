package com.ohagner.deviations.handlers

import com.ohagner.deviations.chains.ApiChain
import com.ohagner.deviations.domain.Deviation

import com.ohagner.deviations.repository.DeviationRepository
import ratpack.jackson.JsonRender
import ratpack.test.handling.RequestFixture
import spock.lang.Specification

class ApiChainSpec extends Specification {

    private static final Deviation BUS_1 = new Deviation(transportMode: Deviation.TransportMode.BUS, lineNumbers: ["800"])
    private static final Deviation BUS_2 = new Deviation(transportMode: Deviation.TransportMode.BUS, lineNumbers: ["801"])
    private static final Deviation TRAIN = new Deviation(transportMode: Deviation.TransportMode.TRAIN, lineNumbers: ["35", "36"])
    private static final Deviation SUBWAY = new Deviation(transportMode: Deviation.TransportMode.SUBWAY, lineNumbers: ["1"])

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
            registry.add(DeviationRepository, deviationRepository)
        }
    }

    def 'should return all deviations'() {
        when:
            def result = requestFixture
                    .uri("deviations")
                    .method("GET")
                    .handleChain(new ApiChain())
        then:
            1 * deviationRepository.retrieveAll() >> allDeviations
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == allDeviations
    }

    def 'should only return all TRAIN deviations'() {
        when:
            def result = requestFixture
                    .uri("deviations/TRAIN")
                    .method("GET")
                    .handleChain(new ApiChain())
        then:
            1 * deviationRepository.retrieveAll() >> allDeviations
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == [TRAIN]
    }

    def 'should only return all BUS deviations'() {
        when:
        def result = requestFixture
                .uri("deviations/BUS")
                .method("GET")
                .handleChain(new ApiChain())
        then:
            1 * deviationRepository.retrieveAll() >> allDeviations
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == [BUS_1, BUS_2]
    }

    def 'should only return correct bus line deviation'() {
        when:
            def result = requestFixture
                    .uri("deviations/BUS/${BUS_2.lineNumbers[0]}")
                    .method("GET")
                    .handleChain(new ApiChain())
        then:
            1 * deviationRepository.retrieveAll() >> allDeviations
            assert result.status.'2xx'
            assert result.rendered(JsonRender).object == [BUS_2]
    }

}
