#!/usr/bin/env bash

mvn clean package -DskipTests -f "./micros-chess-parent/pom.xml" &&
docker-compose up --build &&
docker-compose rm -f
