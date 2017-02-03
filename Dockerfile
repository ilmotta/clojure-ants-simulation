FROM clojure:lein-2.7.1-alpine

WORKDIR /usr/src/app

COPY project.clj .
RUN lein deps

COPY src/ ./src/
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
