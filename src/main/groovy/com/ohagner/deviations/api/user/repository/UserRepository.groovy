package com.ohagner.deviations.api.user.repository

import com.ohagner.deviations.api.user.domain.User
import ratpack.exec.Promise

interface UserRepository {

    Promise<User> findByUsername(String username)

    Promise<User> findByApiToken(String apiToken)

    Promise<List<User>> retrieveAll()

    Promise<User> create(User user, String password)

    Promise<User> delete(User user)

    Promise<User> update(String username, User update)

    Promise<Boolean> userExists(String username)

}
