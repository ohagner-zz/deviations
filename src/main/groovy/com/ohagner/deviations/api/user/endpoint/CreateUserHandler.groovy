package com.ohagner.deviations.api.user.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.user.domain.Role
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import groovy.json.JsonSlurper
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler


class CreateUserHandler extends GroovyHandler {

    UserRepository userRepository

    @Inject
    CreateUserHandler(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            request.getBody().then {
                String request = it.text
                def json = new JsonSlurper().parseText(request)
                String password = json.credentials.password
                User requestUser = User.fromJson(request)

                userRepository.userExists(requestUser.credentials.username).then { Boolean userExists ->
                    if (userExists) {
                        response.status(400)
                        render json(["message": "User already exists"])
                    } else {
                        //validate user
                        requestUser.credentials.apiToken = null
                        requestUser.credentials.role = Role.USER
                        userRepository.create(requestUser, password).then { createdUser ->
                            response.status(201)
                            render createdUser
                        }

                    }
                }
            }
        }
    }
}
