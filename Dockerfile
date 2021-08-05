FROM clojure:tools-deps-slim-buster as builder

WORKDIR /usr/src/app

COPY deps.edn .
# Force download dependencies.
RUN clojure -P -A:uberjar -Stree

COPY Makefile .
RUN make check

COPY src/ /usr/src/app/src
RUN make build

FROM openjdk:11-slim-buster

# Runtime dependencies to run Swing apps.
RUN apt-get update && apt-get install -y \
    libfreetype6-dev \
    libxext-dev \
    libxrender-dev \
    libxtst-dev

COPY --from=builder /usr/src/app/target/app.jar /usr/src/app/app.jar
