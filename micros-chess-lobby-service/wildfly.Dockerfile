FROM quay.io/wildfly/wildfly:29.0.0.Final-jdk17

ADD target/micros-chess-lobby-service-*.war /opt/jboss/wildfly/standalone/deployments/
