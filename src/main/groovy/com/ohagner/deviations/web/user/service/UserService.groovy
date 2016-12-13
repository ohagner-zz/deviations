package com.ohagner.deviations.web.user.service

import ratpack.exec.Promise

interface UserService {

    Promise<Boolean> update(Map user)

    Promise<Boolean> create(Map user)

}