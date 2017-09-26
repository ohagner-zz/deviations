package com.ohagner.deviations.api.watch.repository

import com.ohagner.deviations.api.watch.domain.Watch
import ratpack.exec.Promise

interface WatchRepository {

        Promise<Optional<Watch>> findById(long id)

        Promise<List<Watch>> findByUsername(String username)

        Promise<Optional<Watch>> findByUsernameAndId(String username, long id)

        Promise<List<Watch>> retrieveAll()

        List<Watch> retrieveRange(int pageNumber, int maxNumPerPage)

        Promise<Optional<Watch>> create(Watch watch)

        Promise<Watch> update(Watch watch)

        Promise<Optional<Watch>> delete(String username, long id)

        boolean exists(String username, long id)

    }

