package com.ohagner.deviations.handlers

import com.ohagner.deviations.api.user.domain.Role
import com.ohagner.deviations.api.user.endpoint.UserAuthorizationHandler
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.api.user.domain.Credentials
import com.ohagner.deviations.api.user.domain.Token
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import ratpack.exec.Promise
import ratpack.jackson.JsonRender
import ratpack.test.handling.RequestFixture
import spock.lang.Specification

import java.time.LocalDate

class UserAuthorizationHandlerSpec extends Specification {

    public static final String TOKEN = "abcd-1234"
    public static final String USERNAME = "username"
    public static final Token VALID_TOKEN = new Token(value: TOKEN, expirationDate: LocalDate.now(Constants.ZONE_ID).plusWeeks(4))
    public static final Token EXPIRED_TOKEN = new Token(value: TOKEN, expirationDate: LocalDate.now(Constants.ZONE_ID).minusDays(1))
    public static final Credentials VALID_USER_CREDENTIALS = Credentials.builder().apiToken(VALID_TOKEN).role(Role.USER).username(USERNAME).build()
    public static final Credentials ADMIN_USER_CREDENTIALS = Credentials.builder().apiToken(VALID_TOKEN).role(Role.ADMIN).username(USERNAME).build()
    public static final Credentials EXPIRED_USER_CREDENTIALS = Credentials.builder().apiToken(EXPIRED_TOKEN).role(Role.USER).username(USERNAME).build()



    UserRepository userRepository
    UserAuthorizationHandler handler
    RequestFixture requestFixture

    def setup() {
        userRepository = Mock(UserRepository)
        handler = new UserAuthorizationHandler(userRepository)
        requestFixture = RequestFixture.requestFixture()
                .header(Constants.Headers.USER_TOKEN, TOKEN)
                .registry { r ->
            r.add(UserRepository, userRepository)
        }
    }

    void 'should pass execution on for valid user'() {
        when:
            def result = requestFixture.pathBinding(["username": USERNAME])handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(VALID_USER_CREDENTIALS).build())
            result.calledNext
            result.registry.maybeGet(User).isPresent()
    }

    void 'should fail when apiToken user differs from username in path and user is not admin'() {
        when:
            def result = requestFixture.pathBinding(["username": "OTHER_USERNAME"])handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(VALID_USER_CREDENTIALS).build())
            result.status.code == 403
            result.rendered(JsonRender).object.message == UserAuthorizationHandler.ERROR_MSG_USERNAME_MISMATCH
    }

    void 'should pass when apiToken user differs from username in path and user is admin'() {
        when:
            def result = requestFixture.pathBinding(["username": "OTHER_USERNAME"])handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(ADMIN_USER_CREDENTIALS).build())
            result.registry.maybeGet(User).isPresent()
    }

    void 'should fail when token has expired'() {
        when:
            def result = requestFixture.pathBinding(["username": USERNAME])handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(EXPIRED_USER_CREDENTIALS).build())
            result.status.code == 401
            result.rendered(JsonRender).object.message == UserAuthorizationHandler.ERROR_MSG_API_TOKEN_EXPIRED
    }

    void 'should fail when token has expired and username differs'() {
        when:
            def result = requestFixture.pathBinding(["username": "OTHER_USERNAME"])handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(EXPIRED_USER_CREDENTIALS).build())
            result.status.code == 401
            result.rendered(JsonRender).object.message == UserAuthorizationHandler.ERROR_MSG_API_TOKEN_EXPIRED
    }

    void 'should fail when no token is supplied'() {
        when:
            def result = RequestFixture.requestFixture()
                    .registry { r ->
                r.add(UserRepository, userRepository)
            }.pathBinding(["username": "OTHER_USERNAME"]).handle(handler)
        then:
            1 * userRepository.findByApiToken(_) >> Promise.value(null)
            result.status.code == 401
            result.rendered(JsonRender).object.message == UserAuthorizationHandler.ERROR_MSG_INVALID_API_TOKEN
    }

    void 'should fail when no user is found'() {
        when:
            def result = requestFixture.pathBinding(["username": USERNAME]).handle(handler)
        then:
            1 * userRepository.findByApiToken(_) >> Promise.value(null)
            result.status.code == 401
            result.rendered(JsonRender).object.message == UserAuthorizationHandler.ERROR_MSG_INVALID_API_TOKEN
    }


}
