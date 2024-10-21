#!/bin/sh

# Load environment variables from .env file
export $(grep -v '^#' .env | xargs)

# Start the Java application with environment variables
java -jar /app/connector.jar \
  -Dedc.runtime.name=$CONNECTOR_NAME \
  -Dedc.participant.id=$PARTICIPANT_ID \
  -Dedc.api.auth.key=$API_AUTH_KEY \
  -Dedc.logprefix=$LOG_PREFIX \
  -Dweb.http.port=$HTTP_PORT \
  -Dweb.http.public.port=$HTTP_PUBLIC_PORT \
  -Dweb.http.management.port=$HTTP_MANAGEMENT_PORT \
  -Dweb.http.protocol.port=$HTTP_PROTOCOL_PORT \
  -Dweb.http.control.port=$HTTP_CONTROL_PORT \
  -Dedc.dsp.callback.address=$DSP_CALLBACK_ADDRESS \
  -Dedc.receiver.http.endpoint=$RECEIVER_HTTP_ENDPOINT \
  -Dedc.dataplane.token.validation.endpoint=$TOKEN_VALIDATION_ENDPOINT \
  -Dedc.datasource.asset.url=$POSTGRES_URL \
  -Dedc.datasource.asset.user=$POSTGRES_USER \
  -Dedc.datasource.asset.password=$POSTGRES_PASSWORD \
  -Dedc.datasource.policy.url=$POSTGRES_URL \
  -Dedc.datasource.policy.user=$POSTGRES_USER \
  -Dedc.datasource.policy.password=$POSTGRES_PASSWORD \
  -Dedc.datasource.contractdefinition.url=$POSTGRES_URL \
  -Dedc.datasource.contractdefinition.user=$POSTGRES_USER \
  -Dedc.datasource.contractdefinition.password=$POSTGRES_PASSWORD \
  -Dedc.datasource.contractnegotiation.url=$POSTGRES_URL \
  -Dedc.datasource.contractnegotiation.user=$POSTGRES_USER \
  -Dedc.datasource.contractnegotiation.password=$POSTGRES_PASSWORD \
  -Dedc.datasource.transferprocess.url=$POSTGRES_URL \
  -Dedc.datasource.transferprocess.user=$POSTGRES_USER \
  -Dedc.datasource.transferprocess.password=$POSTGRES_PASSWORD
