package com.ohagner.deviations.chains

import com.ohagner.deviations.api.user.domain.Credentials
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import com.ohagner.deviations.api.watch.service.WatchProcessQueueingService
import ratpack.exec.Promise
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.impose.ImpositionsSpec
import ratpack.impose.UserRegistryImposition
import ratpack.registry.Registry
import spock.lang.AutoCleanup
import spock.lang.Specification

class ApiChainSpec extends Specification {

    WatchRepository mockWatchRepository = Mock()

    WatchProcessQueueingService mockQueueingService = Mock()

    @AutoCleanup
    def aut = new GroovyRatpackMainApplicationUnderTest() {
        @Override
        protected void addImpositions(ImpositionsSpec impositions) {
            impositions.add(UserRegistryImposition.of(
                    Registry.of { r ->
                        r.add(WatchRepository, mockWatchRepository)
                    }
            ))

            impositions.add(UserRegistryImposition.of(
                    Registry.of { r ->
                        r.add(WatchProcessQueueingService, mockQueueingService)
                    }
            ))

            impositions.add(UserRegistryImposition.of(
                    Registry.of { r ->
                        r.add(User, new User(credentials: new Credentials(username: "username")))
                    }
            ))

        }
    }

}
