#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

./gradlew -v
./gradlew clean build copyJarsForJPackage

echo "Create app-image"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image.cfg" \
  --icon "icons/icon.png" \
  --verbose

echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-linux.cfg" \
  --verbose