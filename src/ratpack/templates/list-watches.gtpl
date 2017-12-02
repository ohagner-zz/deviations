layout 'layout.gtpl',
title: title,
msg: msg,
user: user,
weeklyWatches: weeklyWatches,
bodyContents: contents {
    if (username) {
        p(class: "navbar-text navbar-right") {
            span(class: "glyphicon glyphicon-user") {}
            yield 'Signed in as, ' strong(username)
        }
    }

    h1('Bevakningar')

    h2('Veckobevakningar')
    table(class:"table table-striped") {
        thead {
            tr {
                ['Namn','Avresetid', 'Veckodagar', 'Transportsätt', 'Starta bevakning', ''].each { th(it) }
            }
        }
        tbody {
            weeklyWatches.each { watch ->

                tr {
                    td (watch.name)
                    td (watch.schedule.timeOfEvent)
                    td(watch.schedule.weekDays)
                    td {
                        watch.transports.each {
                            yield "$it.transportMode: $it.line\n"
                        }
                    }
                    td("${watch.notifyMaxHoursBefore}h innan avresetid")
                    td {
                        form(action: "/watches/delete/${watch.id}", method: 'post', style: 'display: inline') {
                            input(type: "submit", name: "delete", value: "Radera", class: "btn btn-danger ", '')
                        }
                    }
                }
            }
        }
    }

}