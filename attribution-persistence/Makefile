.PHONY: all build_email build_s3 release

PWD := $(shell pwd)

all: build_email build_s3

build_email:
	cd ../ && ./mvnw package -DfinalName=haystack-attribution-persistence-email -pl attribution-persistence/email -am

build_s3:
	cd ../ && ./mvnw package -DfinalName=haystack-attribution-persistence-s3 -pl attribution-persistence/s3 -am

release: build_email build_s3
	cd email && $(MAKE) docker_build && $(MAKE) release
	cd s3 && $(MAKE) docker_build && $(MAKE) release