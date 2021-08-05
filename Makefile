REPO := ilmotta/ants-simulation
TAG := 1.0.0
CONTAINER_NAME := ants-simulation
DOCKER_JVM_OPTS := -XX:+UseContainerSupport -XX:MaxRAMPercentage=85 -XX:+UnlockExperimentalVMOptions
JVM_OPTS := -XX:MaxRAMPercentage=85 -XX:+UnlockExperimentalVMOptions

.PHONY: all test

all: run

check:
	clojure -M --eval :ok

run:
	clojure -M:run

run/release:
	java $(JVM_OPTS) -jar target/app.jar

build:
	clojure -X:uberjar

test:
	clojure -M:test

clean:
	rm -rf target/ .cpcache/

docker/build:
	@docker build --tag $(REPO):$(TAG) --rm .

# Run to add IP to access control list.
#   $ xhost +local:docker
docker/run/release:
	@docker run -it --rm \
		--name $(CONTAINER_NAME) \
		--network=host \
		-e DISPLAY=$(DISPLAY) \
		-v /tmp/.X11-unix:/tmp/.X11-unix \
		$(REPO):$(TAG) java $(JVM_OPTS) -jar /usr/src/app/app.jar

docker/shell:
	@docker run -it --rm \
		--name $(CONTAINER_NAME) \
		$(REPO):$(TAG) /bin/sh

docker/clean:
	@docker rm -f $(CONTAINER_NAME); \
		docker rmi -f $(REPO):$(TAG)
