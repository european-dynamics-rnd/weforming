# Launcher Builder
FROM golang:1.25 AS launcher_builder
    RUN mkdir -p /w
    WORKDIR /w
    COPY ./launcher/ /w
    RUN go build -o launcher .

# Runner
FROM eclipse-temurin:17-jdk AS runner
    RUN mkdir -p /w
    WORKDIR /w
    COPY ./connector/build/libs/connector.jar /w/connector.jar
    COPY ./identity-hub/build/libs/identity-hub.jar /w/identity-hub.jar
    COPY --from=launcher_builder /w/launcher /w/launcher
    ENV JAVA_BINARY=/opt/java/openjdk/bin/java
    ENV CONNECTOR_JAR=/w/connector.jar
    ENV IDENTITY_HUB_JAR=/w/identity-hub.jar
    ENTRYPOINT ["/w/launcher"]
