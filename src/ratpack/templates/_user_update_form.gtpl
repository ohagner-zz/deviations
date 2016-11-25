def inputText(id, value, opts=[:], disabledFields=[]) {
    def attributes = [type: 'text', name: id, class: 'form-control', id: id, value: value]
    attributes.putAll(opts)
    if(disabledFields.contains(id)) { attributes.disabled = 'disabled' }
    attributes
}

form(class:"form-user", method:"POST", action:"/user/update") {
    input(inputText('username', username, [required:'',placeholder:'Username'], disabledFields))
    input(inputText('firstName', firstName, [placeholder:'First name'], disabledFields))
    input(inputText('lastName', lastName, [placeholder:'Last name'], disabledFields))
    input(id:'emailAddress', name:'emailAddress', value: emailAddress, class:'form-control', type:'email' ,placeholder:'Email address')
    button(type: "submit", class: "btn btn-lg btn-primary", buttonText)
}