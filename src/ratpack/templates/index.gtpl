layout 'layout.gtpl',
title: 'Deviation watcher',
msg: msg,
bodyContents: contents {
    if (username) {
        p(class: "navbar-text navbar-right") {
            span(class: "glyphicon glyphicon-user") {}
            yield 'Signed in as, ' strong(username)
        }
    }

    h1('Start here')

    p('This the starting point, high time to think of more to display here')
}
