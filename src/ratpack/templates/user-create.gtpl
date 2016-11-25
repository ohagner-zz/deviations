layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {
    if (username) {
        p(class: "navbar-text navbar-right") {
            span(class: "glyphicon glyphicon-user") {}
        }
    }

    h1('Create User')
    div(class: 'alert alert-info') {
        p { strong('Username must be unique!') }
    }
    includeGroovy '_user_create_form.gtpl'
}