layout 'layout.gtpl',
        title: title,
        msg: msg,
        user: user,
        bodyContents: contents {
            script(src: '/js/createWatch.js') {}

            form(id: "watchForm", class: "form-horizontal form-create-watch", method: "POST", action: "/watch/create/single") {
                h2('Skapa bevakning för enkel resa')
                div(class: "form-group") {
                    label(for: "name", class: "control-label col-sm-2 col-md-2", 'Namn')
                    div(class: "col-md-4") {
                        input(type: "text", id: "name", name: "name", class: "form-control", placeholder: "Namn", required: '', autofocus: '')
                    }
                }
                div(class: "form-group") {
                    label(for: "dateOfEvent", class: "control-label col-sm-2", 'Resdatum')
                    div(class: "col-sm-2") {
                        input(type: "date", id: "dateOfEvent", name: "dateOfEvent", placeholder: "yyyy-mm-dd", class: "form-control", required: '', autofocus: '')
                    }
                }
                div(class: "form-group") {
                    label(for: "timeOfEvent", class: "control-label col-sm-2", 'Tidpunkt')
                    div(class: "col-sm-2") {
                        input(type: "time", name: "timeOfEvent", class: "form-control", placeholder: "hh:mm", required: '', autofocus: '')
                    }
                }
                div(class: "form-group") {
                    label(for: "transportMode", class: "control-label col-sm-2", 'Transportsätt')
                    div(class: "col-sm-2") {
                        select(name: "transport[0].transportMode", class: "form-control") {
                            option(value: "TRAIN", 'Tåg')
                            option(value: "BUS", 'Buss')
                            option(value: "SUBWAY", 'Tunnelbana')
                        }
                    }
                    label(for: "line", class: "control-label col-sm-1", 'Linje')
                    div(class: "col-sm-2") {
                        input(type: "text", class: "form-control", name: "transport[0].line", placeholder: "Linjenummer", pattern: "[a-öA-Ö0-9]*", required: '', autofocus: '')
                    }
                    div(class: "col-sm-offset-1 col-sm-2") {
                        button(class: "btn btn-success form-control addButton", type: "button", 'Lägg till rad')
                    }
                }

                //<!-- Template used when creating multiple transports -->
                div(class: "form-group hide", id: "transportTemplate") {
                    label(for: "transportMode", class: "control-label col-sm-2", 'Transportsätt')
                    div(class: "col-sm-2") {
                        select(id: "transportMode", class: "form-control") {
                            option(value: "TRAIN", 'Tåg')
                            option(value: "BUS", 'Buss')
                            option(value: "SUBWAY", 'Tunnelbana')
                        }
                    }
                    label(for: "line", class: "control-label col-sm-1", 'Linjer')
                    div(class: "col-sm-2") {
                        input(type: "text", class: "form-control", id: "line", placeholder: "1, 2, 3...", pattern: "[a-öA-Ö0-9,\\s]*")
                    }
                    div(class: "col-sm-offset-1 col-sm-2") {
                        button(class: "btn btn-danger form-control removeButton", type: "button", 'Ta bort rad')
                    }
                }
                //<!-- End of template -->

                div(class: "form-group") {
                    label(for: "notifyBy", class: "control-label col-sm-2", 'Notifiera via')
                    div(class: "col-sm-10") {
                        label(class: "checkbox-inline") {
                            input(type: "checkbox", name: "notifyByEmail", checked: "true", 'E-post')
                        }
                        if(user.slackWebhook) {
                            label(class: "checkbox-inline") {
                                input(type: "checkbox", name: "notifyBySlack", 'Slack')
                            }
                        }
                    }
                }
                div(class: "form-group") {
                    label(for: "notifyMaxHoursBefore", class: "control-label col-sm-2", 'Starta bevakning max timmar innan')
                    div(class: "col-sm-1") {
                        input(type: "number", name: "notifyMaxHoursBefore", class: "form-control", placeholder: "1-5", default: "2", min: "1", max: "4", required: '', autofocus: '')
                    }
                    div(class: "col-sm-10") {
                        span(class: "help-block", 'Här anger du hur många timmar innan avresa du vill börja bevaka och få notifieringar')
                    }
                }
                div(class: "col-sm-offset-2 col-sm-10") {
                    button(class: "btn btn-default", type: "submit", 'Skapa')
                }
            }
        }