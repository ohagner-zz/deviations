layout 'layout.gtpl',
        title: title,
        msg: msg,
        bodyContents: contents {

            script(src: '/js/stops.js') {}

            h1('Stops')

            h2('Skapa bevakning för enkel resa')
            div(class: "form-group") {
                label(for: "name", class: "control-label col-sm-2 col-md-2", 'Namn')
                div(class: "col-md-4") {
                    input(type: "text", id: "name", name: "name", class: "form-control", placeholder: "Namn", required: '', autofocus: '')
                }
            }
            div(class: "col-sm-offset-2 col-sm-10") {
                button(class: "btn btn-default", id: "getstopsButton", type: "submit", 'Hämta stationer')
            }
            div(class: "col-md-2", id: "results") {

            }
            button(type:"button", class:"btn btn-primary", 'data-toggle':"modal", 'data-target':"#exampleModalLong", 'Launch demo modal')
            div(class: "modal fade", id: "exampleModalLong", tabindex: "-1", role: "dialog", 'aria-labelledby': "exampleModalLongTitle", 'aria-hidden': "true") {
                div(class: "modal-dialog", role: "document") {
                    div(class: "modal-content") {
                        div(class: "modal-header") {
                            h5(class: "modal-title", id: "exampleModalLongTitle", 'Modal title')
                            button(type: "button", class: "close", 'data-dismiss': "modal", 'aria-label': "Close") {
                                span('aria-hidden': "true", '&times;')
                            }
                        }
                        div(class: "modal-body") {
                            p("hej")
                        }
                        div(class: "modal-footer") {
                            button(type: "button", class: "btn btn-secondary", 'data-dismiss': "modal", 'Close')
                            button(type: "button", class: "btn btn-primary", 'Save changes')
                        }
                    }
                }
            }
        }