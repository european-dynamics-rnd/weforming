FROM openjdk:17-slim  
#FROM openjdk:17-alpine

EXPOSE 15588

ARG JAR_FILE=edc_handler-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar

ARG DB_IP
ARG DB_PORT
ARG DB_NAME
ARG DB_USERNAME
ARG DB_PASSWORD

ENV DB_IP_ENV=${DB_IP}
ENV DB_PORT_ENV=${DB_PORT}
ENV DB_NAME_ENV=${DB_NAME}
ENV DB_USERNAME_ENV=${DB_USERNAME}
ENV DB_PASSWORD_ENV=${DB_PASSWORD}

RUN touch ./entrypoint.sh
RUN echo "java -jar ./app.jar --db.ip=${DB_IP_ENV} --db.port=${DB_PORT_ENV} --db.name=${DB_NAME_ENV} --db.username=${DB_USERNAME_ENV} --db.password=${DB_PASSWORD_ENV} --sofia.uri=https://weforming-mid.eurodyn.com/api --connector.url=http://provider-edc-handler " > ./entrypoint.sh
RUN chmod +x ./entrypoint.sh
CMD ./entrypoint.sh
