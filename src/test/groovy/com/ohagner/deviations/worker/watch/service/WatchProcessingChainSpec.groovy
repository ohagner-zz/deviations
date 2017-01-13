package com.ohagner.deviations.worker.watch.service

import com.ohagner.deviations.api.watch.domain.Watch
import spock.lang.Specification

class WatchProcessingChainSpec extends Specification {

    WatchProcessingChain chain = new WatchProcessingChain()

    void 'should run all stages in succession'() {
        given:
            chain.appendStage(new NameAddingStage(name: "1"))
            chain.appendStage(new NameAddingStage(name: "2"))
            chain.appendStage(new NameAddingStage(name: "3"))
            Watch watch = new Watch(name: "")
        when:
            chain.process(watch)
        then:
            println watch.name
            watch.name == "123"
    }

}

