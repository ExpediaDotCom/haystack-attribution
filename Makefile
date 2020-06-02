.PHONY: all clean attributor attribution-persistence release

export DOCKER_ORG := expediadotcom

PWD := $(shell pwd)

clean:
	./mvnw -q clean

build: clean
	./mvnw -q package

all: clean attributor attribution-persistence report-coverage

report-coverage:
	./mvnw scoverage:report-only

attributor:
	./mvnw -q package -DfinalName=haystack-attributor -pl attributor -am

attribution-persistence:
	cd attribution-persistence && $(MAKE) all

release: clean attributor attribution-persistence
	cd attributor && $(MAKE) docker_build && $(MAKE) release
	cd attribution-persistence && $(MAKE) release
	./.travis/deploy.sh
