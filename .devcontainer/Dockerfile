FROM mcr.microsoft.com/devcontainers/base:bullseye

ADD https://github.com/wildfly/wildfly/releases/download/29.0.0.Final/wildfly-29.0.0.Final.tar.gz /tmp/
RUN mkdir /wildfly && tar xvfz /tmp/wildfly-29.0.0.Final.tar.gz --directory /wildfly && chown -R vscode:vscode /wildfly