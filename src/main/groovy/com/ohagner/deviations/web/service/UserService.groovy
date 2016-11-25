package com.ohagner.deviations.web.service

import ratpack.exec.Promise

interface UserService {

    Promise<Boolean> update(Map user)

    Promise<Boolean> create(Map user)

}