package com.ohagner.deviations.repository

import com.ohagner.deviations.domain.User

interface UserRepository {

    Optional<User> findByUsername(String username)

    List<User> retrieveAll()

    User create(User user)

    void delete(User user)

    User update(String username, User update)

    boolean userExists(String username)

}
