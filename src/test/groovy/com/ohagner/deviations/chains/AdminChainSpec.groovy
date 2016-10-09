package com.ohagner.deviations.chains

import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.repository.WatchRepository
import ratpack.http.client.ReceivedResponse
import ratpack.jackson.JsonRender
import ratpack.test.embed.EmbeddedApp
import ratpack.test.handling.RequestFixture
import spock.lang.Specification


class AdminChainSpec extends Specification {

    def watchRepo = Mock(WatchRepository)

    def requestFixture = RequestFixture.requestFixture()
        .registry { registry ->
            registry.add(WatchRepository, watchRepo)
    }

    def setup() {

    }

    def 'should return list of watches'() {
        given:
            Watch watch = Watch.fromJson(new File("src/test/resources/watches/weeklyScheduleWatch.json").text)
        when:
            def result = requestFixture.uri("watchesToProcess").handleChain(new AdminChain())
        then:
            1 * watchRepo.retrieveRange(_,_) >> [watch]
            assert result.rendered(JsonRender).object == [watch]
    }

    def 'should return response'() {
        given:
            Watch watch = Watch.fromJson(new File("src/test/resources/watches/weeklyScheduleWatch.json").text)

        def app = EmbeddedApp.of{ s ->

                s.registryOf { r ->
                    r.add(WatchRepository, watchRepo)
                }
                s.handlers(new AdminChain())
            }
        when:
            ReceivedResponse response = app.httpClient.get("watchesToProcess")
        then:
            1 * watchRepo.retrieveRange(_,_) >> [watch]
            assert response.statusCode == 200
    }
}
