.PHONY: docker_build release

export DOCKER_IMAGE_NAME := haystack-attribution-persistence-email
PWD := $(shell pwd)

docker_build:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

release:
	../../deployment/scripts/publish-to-docker-hub.sh
