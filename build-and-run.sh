#!/usr/bin/env bash

docker run -it --rm -v "$(pwd)"/micros-chess-frontend:/app -w /app node:18-alpine npm run build &&
rm -r ./micros-chess-web-service/src/main/resources/static/* && cp -r ./micros-chess-frontend/dist/* ./micros-chess-web-service/src/main/resources/static/ &&
docker run -it --rm -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.9.4-eclipse-temurin-17-alpine mvn clean package -DskipTests -f "./micros-chess-parent/pom.xml" &&
docker-compose up --build &&
docker-compose rm -f
