DOCKER = docker run --rm -v "$(CURDIR)":/app -v wallet-service-m2:/root/.m2 -w /app
MAVEN_IMAGE = maven:3.9-eclipse-temurin-17

.PHONY: test run
test:
	$(DOCKER) $(MAVEN_IMAGE) mvn -B test

run:
	$(DOCKER) -it -p 8090:8090 $(MAVEN_IMAGE) mvn -B spring-boot:run
