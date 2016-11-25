layout 'layout.gtpl',
title: title,
msg: msg,
user: user,
bodyContents: contents {


    h1('Uppdatera användare')

    includeGroovy '_user_update_form.gtpl'
}