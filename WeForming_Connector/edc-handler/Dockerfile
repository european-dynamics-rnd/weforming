FROM eclipse-temurin:21-jdk-noble

EXPOSE 15588

ARG JAR_FILE=edc_handler-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar

ARG DB_IP
ARG DB_PORT
ARG DB_NAME
ARG DB_USERNAME
ARG DB_PASSWORD
ARG CONNECTOR_URI
ARG IDENTITYHUB_URI

# ✅ νέα ARGs
ARG INDY_URL
ARG REGISTER_PARTICIPANT_X_API_KEY
ARG REGISTER_PARTICIPANT_ID
ARG REGISTER_PARTICIPANT_PARTICIPANT_ID
ARG REGISTER_PARTICIPANT_PARTICIPANT_DID
ARG WALLET_PRIVATE_KEY

ENV DB_IP_ENV=${DB_IP}
ENV DB_PORT_ENV=${DB_PORT}
ENV DB_NAME_ENV=${DB_NAME}
ENV DB_USERNAME_ENV=${DB_USERNAME}
ENV DB_PASSWORD_ENV=${DB_PASSWORD}
ENV CONNECTOR_URI_ENV=${CONNECTOR_URI}
ENV IDENTITYHUB_URI_ENV=${IDENTITYHUB_URI}
ENV WALLET_PRIVATE_KEY_ENV=${WALLET_PRIVATE_KEY}

# ✅ νέα ENV
ENV INDY_URL_ENV=${INDY_URL}
ENV REGISTER_PARTICIPANT_X_API_KEY_ENV=${REGISTER_PARTICIPANT_X_API_KEY}
ENV REGISTER_PARTICIPANT_ID_ENV=${REGISTER_PARTICIPANT_ID}
ENV REGISTER_PARTICIPANT_PARTICIPANT_ID_ENV=${REGISTER_PARTICIPANT_PARTICIPANT_ID}
ENV REGISTER_PARTICIPANT_PARTICIPANT_DID_ENV=${REGISTER_PARTICIPANT_PARTICIPANT_DID}

RUN echo '#!/bin/sh
PUBLIC_KEY_PEM=$(cat /app/test-keys/connector-public.pem | tr "\n" "\\n")

RUN touch ./entrypoint.sh
java -jar ./app.jar \
  --logging.level.org.springframework.web.client.RestTemplate=DEBUG \
  --logging.level.org.springframework.http.client=DEBUG \
  --logging.level.org.springframework.web=DEBUG \
  --db.ip=${DB_IP_ENV} \
  --db.port=${DB_PORT_ENV} \
  --db.name=${DB_NAME_ENV} \
  --db.username=${DB_USERNAME_ENV} \
  --db.password=${DB_PASSWORD_ENV} \
  --sofia.uri=https://weforming-mid.eurodyn.com/api \
  --connector.url=${CONNECTOR_URI_ENV} \
  --identityhub.url=${IDENTITYHUB_URI_ENV} \
  --indy.url=${INDY_URL_ENV} \
  --registerParticipant.x-api-key=${REGISTER_PARTICIPANT_X_API_KEY_ENV} \
  --registerParticipant.id=${REGISTER_PARTICIPANT_ID_ENV} \
  --registerParticipant.participantId=${REGISTER_PARTICIPANT_PARTICIPANT_ID_ENV} \
  --registerParticipant.participantDid=${REGISTER_PARTICIPANT_PARTICIPANT_DID_ENV} \
  --registerParticipant.privateKey=${WALLET_PRIVATE_KEY_ENV} \
  --registerParticipant.publicKeyPem="$PUBLIC_KEY_PEM"
' > ./entrypoint.sh

RUN chmod +x ./entrypoint.sh
CMD ./entrypoint.sh
