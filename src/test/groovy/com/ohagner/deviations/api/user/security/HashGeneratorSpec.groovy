package com.ohagner.deviations.api.user.security

import spock.lang.Specification

class HashGeneratorSpec extends Specification {



    def 'should return same hash'() {
        given:
            String salt = HashGenerator.createSalt()
            String password = "Password"
        expect:
            assert HashGenerator.generateHash(password, salt) == HashGenerator.generateHash(password, salt)
    }
}
