FROM clojure:openjdk-11-lein-2.9.1 as builder
WORKDIR /tmp
COPY /.m2/ /root/.m2
COPY /project.clj /tmp/
COPY /src/  /tmp/src/
COPY /resources/  /tmp/resources/
RUN lein uberjar

FROM adoptopenjdk/openjdk11:alpine
LABEL maintainer="trevor"

RUN addgroup -g 1000 -S ttt && \
    adduser -u 1000 -S ttt -G ttt -h /usr/local/deploy
USER ttt

ARG JAR_FILE="/usr/local/deploy/app.jar"
COPY deploy "/usr/local/deploy"
COPY --from=builder /tmp/target/uberjar/tic-tac-toe-0.1.0-SNAPSHOT-standalone.jar ${JAR_FILE}

ENTRYPOINT ["/usr/local/deploy/bin/run.sh"]
