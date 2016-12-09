package com.ohagner.deviations.api.user

import ratpack.handling.Context
import ratpack.render.Renderer

import static ratpack.jackson.Jackson.json

/**
 * Removes sensitive data from rendered user
 */
class UserRenderer implements Renderer<User> {

    @Override
    Class<User> getType() {
        return User
    }

    @Override
    void render(Context context, User user) throws Exception {
        if(user.credentials) {
            user.credentials.passwordHash = null
            user.credentials.passwordSalt = null
            user.credentials.role = null
        }
        context.render json(user)
    }
}
