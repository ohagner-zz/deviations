package com.ohagner.deviations.api.user.repository

import com.ohagner.deviations.api.user.domain.User
import ratpack.exec.Promise

interface UserRepository {

    Promise<User> findByUsername(String username)

    Promise<User> findByApiToken(String apiToken)

    List<User> retrieveAll()

    User create(User user, String password)

    void delete(User user)

    User update(String username, User update)

    boolean userExists(String username)

}
