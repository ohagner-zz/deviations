Trafikbevakaren
-----------------------------

This project allows people to set up monitoring for the public transportation in Stockholm and receive alerts when something goes wrong.

## Guidelines

### Package organization
Organize packages with regard to domain first

### Chains
Chains should preferably only delegate to handlers

### Handlers
This is where the orchestration is made against services and repositories.

### Services
Business logic and other non-trivial stuff here

### Repositories
Simple CRUD functionality against databases, other webservices etc.

