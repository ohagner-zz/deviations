package com.ohagner.deviations.chains

import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class AdminChain extends GroovyChainAction {
    @Override
    void execute() throws Exception {
        path("watchesToProcess") { WatchRepository watchRepo ->
            List<Watch> watchList = watchRepo.retrieveRange(1,50)
            log.info "Size: ${watchList.size()}"
            render json(watchList)
        }
    }
}
