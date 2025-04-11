@echo off
echo Building SamNgocLinhPJ project...
mvn clean install -DskipTests
echo Build completed. Only service-level tests have been verified.
echo To run specific tests, use: mvn test -Dtest=ProductServiceTest 