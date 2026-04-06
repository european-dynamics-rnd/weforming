#!/bin/bash

set -e

./gradlew connector:build
./gradlew identity-hub:build

docker build --tag weforming-connector .
