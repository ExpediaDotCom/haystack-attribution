.PHONY: all clean attributor attribution-persistence release

export DOCKER_ORG := expediadotcom

PWD := $(shell pwd)

clean:
	./mvnw clean

build: clean
	./mvnw package

all: clean attributor attribution-persistence report-coverage

report-coverage:
	./mvnw scoverage:report-only

attributor:
	./mvnw package -DfinalName=haystack-attributor -pl attributor -am

attribution-persistence:
	cd attribution-persistence && $(MAKE) all

release: clean attributor attribution-persistence
	cd attribution-persistence && $(MAKE) release
	cd attributor && $(MAKE) release
	./.travis/deploy.sh
