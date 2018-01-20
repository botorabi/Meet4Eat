#!/bin/sh
mvn clean package -DskipTests && docker build -t org.m4e/m4e .
docker rm -f m4e || true && docker run -it -p 8080:8080 -p 4848:4848 -v $(pwd)/target/logs:/var/logs  --name m4e org.m4e/m4e
