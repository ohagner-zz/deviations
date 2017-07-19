package com.ohagner.deviations.api.user.repository

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification


class MongoUserRepositorySpec extends Specification {

    private DBCollection usersMock = Mock()

    private MongoUserRepository userRepository = new MongoUserRepository(usersMock)

    @AutoCleanup
    ExecHarness execHarness = ExecHarness.harness()

    void "should return user"() {
        given:
            usersMock.findOne(_) >> new BasicDBObject(["firstName":"Test", "lastName":"Testsson"])
        expect:
            execHarness.yieldSingle {
                userRepository.findByUsername("testuser")
            }.value.firstName == "Test"
    }

    void "should return null when user is not found"() {
        given:
            usersMock.findOne(_) >> null
        expect:
            execHarness.yieldSingle {
                userRepository.findByUsername("testuser")
            }.value == null
    }

}
