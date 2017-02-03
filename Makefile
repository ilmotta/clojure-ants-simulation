REPO := ilmotta/ants-simulation
TAG := 0.0.1
CONTAINER_NAME := ants-simulation
DISPLAY := $(shell ifconfig en0 | grep inet | awk '$$1=="inet" {print $$2}')

.PHONY: all start docker/build docker/start docker/clean

all: start

start:
	@lein run

docker/build:
	@docker build --tag $(REPO):$(TAG) --rm .

# Run to add IP to access control list
#   $ xhost + $(ifconfig en0 | grep inet | awk '$1=="inet" {print $2}')
docker/start:
	@docker run -it --rm \
		--name $(CONTAINER_NAME) \
		-e DISPLAY=$(DISPLAY):0 \
		-v /tmp/.X11-unix:/tmp/.X11-unix \
		$(REPO):$(TAG) java -jar app-standalone.jar

docker/clean:
	@docker rm -f $(CONTAINER_NAME); \
		docker rmi -f $(REPO):$(TAG)
