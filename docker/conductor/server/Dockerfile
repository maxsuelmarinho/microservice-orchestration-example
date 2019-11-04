# 0. Builder stage
FROM openjdk:8-jdk AS builder

ARG conductor_version=v2.18.0
# Clone the project directly onto the image
RUN git clone https://github.com/Netflix/conductor.git
WORKDIR /conductor
RUN git checkout ${conductor_version}

# Build the server on run
RUN ./gradlew build -x test

# 1. Bin stage
FROM openjdk:8-jre-alpine

# Make app folders
RUN mkdir -p /app/config /app/logs /app/libs

# Copy the project directly onto the image
COPY --from=builder /conductor/docker/server/bin /app
COPY --from=builder /conductor/docker/server/config /app/config
COPY --from=builder /conductor/server/build/libs/conductor-server-*-all.jar /app/libs

# Copy the files for the server into the app folders
RUN chmod +x /app/startup.sh

EXPOSE 8080
EXPOSE 8090

CMD [ "/app/startup.sh" ]
ENTRYPOINT [ "/bin/sh"]
