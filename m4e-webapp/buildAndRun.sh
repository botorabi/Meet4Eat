#!/bin/sh
mvn clean package && docker build -t org.m4e/m4e .
docker rm -f m4e || true && docker run -it -p 8080:8080 -p 4848:4848 --name m4e org.m4e/m4e
