package com.ohagner.deviations.api.watch.repository

import com.ohagner.deviations.api.watch.domain.Watch

interface WatchRepository {

        Optional<Watch> findById(long id)

        List<Watch> findByUsername(String username)

        Optional<Watch> findByUsernameAndId(String username, long id)

        List<Watch> retrieveAll()

        List<Watch> retrieveRange(int pageNumber, int maxNumPerPage)

        Watch create(Watch watch)

        Watch update(Watch watch)

        Optional<Watch> delete(String username, long id)

        boolean exists(String username, long id)

    }

