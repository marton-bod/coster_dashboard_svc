## Coster.io - Dashboard service

Microservice responsible for serving aggregated expense-related data used for statistical purposes or front-end chart generation.
Developed in spring-boot.

### Build the app:
* Prerequisites: Maven, JDK11
* `mvn clean install -Pdocker` - if you have docker engine
* `mvn clean install` - if not
    
### REST Interface:
- Swagger UI: localhost:9002/swagger-ui.html

### Actuator endpoints:
- Health: localhost:9002/actuator/health
- Beans: localhost:9002/actuator/beans
- Status: localhost:9002/actuator/status
