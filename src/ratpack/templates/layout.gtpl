yieldUnescaped '<!DOCTYPE html>'
html(lang:'en') {
    head {
        meta(charset:'utf-8')
        title(title ?: 'Deviation matcher')
        meta('http-equiv': "Content-Type", content:"text/html; charset=utf-8")
        meta(name: 'viewport', content: 'width=device-width, initial-scale=1.0')
        script(src: '/js/jquery.min.js') {}
        script(src: '/js/bootstrap.min.js') {}
        link(href: '/css/bootstrap.min.css', rel: 'stylesheet')
        link(href: '/css/deviations.css', rel: 'stylesheet')
        link(href: '/img/favicon.ico', rel: 'shortcut icon')
    }
    body {
        nav(class:"navbar navbar-default") {
            div(class:"container") {
                div(class:"navbar-header") {
                    a(class:"navbar-brand", href:"/", 'Trafikbevakaren')
                }
                ul(class:"nav navbar-nav") {
                    li(class:"dropdown") {
                        a(class:"dropdown-toggle", "data-toggle":"dropdown", href:"#") {
                            yield 'Bevakningar'
                            span(class:"caret") {}
                        }
                        ul(class:"dropdown-menu") {
                            li {
                                a(href:"/watches", 'Visa alla')
                            }
                            li {
                                a(href:"/watch/create/single", 'Skapa enkel')
                            }
                            li {
                                a(href:"/watch/create/weekly", 'Skapa veckobevakning')
                            }
                        }
                    }
                }
                ul(class:"nav navbar-nav navbar-right") {
                    li {
                        a(href:"/user/create") {
                            yield 'Registrera '
                            span(class:"glyphicon glyphicon-user")
                        }
                    }
                    li {
                        a(href:"/login") {
                            yield "Logga in  "
                            span(class:"glyphicon glyphicon-log-in")
                        }
                    }
                }
            }
        }
        div(class:'container') {
            if (msg) {
                div(class: 'alert alert-info alert-dismissable') {
                    button(type: 'button', class: 'close', 'data-dismiss': 'alert', 'aria-hidden':'true', '&times;')
                    yield msg
                }
            }
            bodyContents()
        }
    }
}