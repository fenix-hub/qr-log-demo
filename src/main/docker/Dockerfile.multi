## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY --chown=quarkus:quarkus mvnw /code/mvnw
# make /code/mvnw executable by quarkus user
RUN chmod +x /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
USER quarkus
WORKDIR /code
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline -q
COPY src /code/src
USER root
# Make target dir and subdirs writable by quarkus user
RUN mkdir -p /code/target && chown -R quarkus:quarkus /code/target
USER quarkus
RUN ./mvnw package -Dnative -q

## Stage 2 : create the docker final image
FROM quay.io/quarkus/quarkus-micro-image:2.0

COPY --from=build /code/target/*-runner /work/application

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]