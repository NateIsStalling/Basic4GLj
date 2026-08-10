#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'

MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH='embedded.provisionprofile'
MAC_SIGNING_KEY_USER_NAME='Configure CI/CD Variable'
MAC_SIGNING_TEAM_ID='Configure CI/CD Variable'
MAC_SIGNING_BUNDLE_ID='com.basic4glj.editor'
MAC_SIGNING_DEVELOPER_ID_INSTALLER_NAME='Developer ID Installer: Nathaniel Nielsen'

SUBMISSION_ID="$1"

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

if [ -z "$SUBMISSION_ID" ]; then
  SUBMISSION_ID="$MAC_SIGNING_NOTARIZATION_SUBMISSION_ID"
fi

if [ -z "$SUBMISSION_ID" ]; then
  echo "Usage: $0 NOTARIZATION_SUBMISSION_ID"
  echo "Or set MAC_SIGNING_NOTARIZATION_SUBMISSION_ID in .env"
  exit 1
fi

if [ -z "$MAC_SIGNING_NOTARIZATION_USER_NAME" ]; then
  echo "MAC_SIGNING_NOTARIZATION_USER_NAME is required"
  exit 1
fi

if [ -z "$MAC_SIGNING_TEAM_ID" ]; then
  echo "MAC_SIGNING_TEAM_ID is required"
  exit 1
fi

if [ -z "$MAC_SIGNING_NOTARIZATION_PASSWORD" ]; then
  echo "MAC_SIGNING_NOTARIZATION_PASSWORD is required"
  exit 1
fi

xcrun notarytool log "$SUBMISSION_ID" \
  --apple-id "$MAC_SIGNING_NOTARIZATION_USER_NAME" \
  --team-id "$MAC_SIGNING_TEAM_ID" \
  --password "$MAC_SIGNING_NOTARIZATION_PASSWORD"
