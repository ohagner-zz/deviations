package com.ohagner.deviations.repository

import com.ohagner.deviations.api.deviation.repository.CachedDeviationRepository
import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.deviation.repository.DeviationRepository
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.Duration

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

class CachedDeviationRepositorySpec extends Specification {

    @AutoCleanup
    ExecHarness execHarness = ExecHarness.harness()

    Deviation one = new Deviation(id: "1")
    Deviation two = new Deviation(id: "2")

    def "should return different value for both calls since cache expired"() {
        given:
            DeviationRepository source = Mock()
            source.retrieveAll() >> Promise.sync { [one] } >> Promise.sync { [two] }
            CachedDeviationRepository cachedRepo = new CachedDeviationRepository(source, Duration.ofMillis(10L))
        expect:
            List<Deviation> firstResponse = execHarness.yieldSingle {
                cachedRepo.retrieveAll()
            }.value
            assertThat(firstResponse.size(), is(1))
            assertThat(firstResponse.first().id, is(one.id))
            Thread.sleep(1000)
            List<Deviation> secondResponse = execHarness.yieldSingle {
                cachedRepo.retrieveAll()
            }.value
            assertThat(secondResponse.size(), is(1))
            assertThat(secondResponse.first().id, is(two.id))
    }

    def "should return same value for both calls"() {
        given:
            DeviationRepository source = Mock()
            source.retrieveAll() >>  Promise.value([one]) >> Promise.value([two])
            CachedDeviationRepository cachedRepo = new CachedDeviationRepository(source, Duration.ofMinutes(30))
        when:
            List<Deviation> firstResponse = execHarness.yieldSingle {
                cachedRepo.retrieveAll()
            }.value
            Thread.sleep(1000)
            List<Deviation> secondResponse = execHarness.yieldSingle {
                cachedRepo.retrieveAll()
            }.value
        then:
            assertThat(firstResponse.first().id, is(one.id))
            assertThat(secondResponse.first().id, is(one.id))
    }


}
