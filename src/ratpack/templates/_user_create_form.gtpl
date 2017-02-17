def inputText(id, value, opts=[:], disabledFields=[]) {
    def attributes = [type: 'text', name: id, class: 'form-control', id: id, value: value]
    attributes.putAll(opts)
    if(disabledFields.contains(id)) { attributes.disabled = 'disabled' }
    attributes
}

form(class:"form-user", method:"POST", action:"/user/create") {
    input(inputText('username', username, [required:'',placeholder:'Username'], disabledFields))
    input(id:'password', name:'password', class:'form-control', type:'password' ,placeholder:'Password')
    input(inputText('firstName', firstName, [placeholder:'First name'], disabledFields))
    input(inputText('lastName', lastName, [placeholder:'Last name'], disabledFields))
    input(id:'emailAddress', name:'emailAddress', class:'form-control', type:'email' ,placeholder:'Email address')
    input(id:'webhook', name:'webhook', class:'form-control', type:'text' ,placeholder:'Webhook URL')
    p('Vad är det här? Läs mer någonstans')
    input(id:'slackWebhook', name:'slackWebhook', class:'form-control', type:'text' ,placeholder:'Slack webhook URL')
    button(type: "submit", class: "btn btn-lg btn-primary", buttonText)
}