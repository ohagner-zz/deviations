package com.ohagner.deviations.domain

import com.ohagner.deviations.Role
import com.ohagner.deviations.domain.user.Credentials
import com.ohagner.deviations.domain.user.Token
import com.ohagner.deviations.domain.user.User
import spock.lang.Specification
import java.time.LocalDate

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals
import static org.hamcrest.MatcherAssert.assertThat

class UserSpec extends Specification {

    def 'create json from User object'() {
        given:
            String expected = new File("src/test/resources/users/user.json").text
            User user = createUser()
        expect:
            assertThat(user.toJson(), jsonEquals(expected))
    }

    def 'create User object from json'() {
        given:
            String userAsJson = new File("src/test/resources/users/user.json").text
            User expected = createUser()
        expect:
            assert User.fromJson(userAsJson).equals(expected)
    }

    private User createUser() {
        Credentials credentials = Credentials.builder()
            .role(Role.USER)
            .username("username")
            .passwordHash("passwordHash")
            .passwordSalt("passwordSalt")
            .apiToken(new Token(value: "value", expirationDate: LocalDate.of(2016, 10, 10))).build()
        return User.builder()
                .credentials(credentials)
                .firstName("firstName")
                .lastName("lastName")
                .emailAddress("emailAddress")
                .build()
    }

}
