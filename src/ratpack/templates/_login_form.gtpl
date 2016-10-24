def inputText(id, type, opts=[:]) {
    def attributes = [type: type, name: id, class: 'form-control', id: id]
    attributes.putAll(opts)
    attributes
}

form(class:"form-login", method:"POST", action:"/login") {
    input(inputText('username', 'text', [required:'',placeholder:'Username']))
    input(inputText('password', 'password', [required:'',placeholder:'Password']))
    button(type: "Submit", class: "btn btn-lg btn-primary", buttonText)
}