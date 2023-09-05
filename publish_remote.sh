#!/bin/bash

if [[ `git status --porcelain` ]]; then
  echo "The baseline profile has changed. Commit the changes before publishing."
else
    # Build and upload the artifacts to 'mavenCentral'.
    ./gradlew publish
fi
