#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'

MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH='embedded.provisionprofile'
MAC_SIGNING_KEY_USER_NAME='Configure CI/CD Variable'
MAC_SIGNING_NOTARIZATION_USER_NAME='Configure CI/CD Variable'
MAC_SIGNING_NOTARIZATION_PASSWORD='Configure CI/CD Variable'
MAC_SIGNING_PACKAGE_SIGNING_PREFIX='com.basic4glj.desktop.'

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

 xcrun altool --notarization-info "$MAC_SIGNING_NOTARIZATION_UUID" \
  --username "$MAC_SIGNING_NOTARIZATION_USER_NAME" \
  --password "$MAC_SIGNING_NOTARIZATION_PASSWORD"