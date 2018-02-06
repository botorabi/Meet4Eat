#!/bin/sh

SONAR_LOGIN=cc6b66b86a9e4dd9ea10cb333dc1a9be6e8268b6

mvn clean test

mvn sonar:sonar -e  -Dsonar.host.url=http://localhost:9000 -Dsonar.login=$SONAR_LOGIN -DskipTests=true -Dsonar.language=java -Dsonar.report.export.path=sonar-report.json
 
