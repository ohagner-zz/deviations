package com.ohagner.deviations.repository

import com.ohagner.deviations.domain.user.User
import ratpack.exec.Promise

interface UserRepository {

    Optional<User> findByUsername(String username)

    Promise<User> findByApiToken(String apiToken)

    List<User> retrieveAll()

    User create(User user, String password)

    void delete(User user)

    User update(String username, User update)

    boolean userExists(String username)

}
