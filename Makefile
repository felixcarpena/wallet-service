MAVEN = docker run --rm -v "$(CURDIR)":/app -v wallet-service-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B

.PHONY: test
test:
	$(MAVEN) test
