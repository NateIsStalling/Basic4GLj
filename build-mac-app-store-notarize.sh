#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'

MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH='embedded.provisionprofile'
MAC_SIGNING_KEY_USER_NAME='Configure CI/CD Variable'
MAC_SIGNING_BUNDLE_ID='com.basic4glj.editor'
MAC_SIGNING_DEVELOPER_ID_INSTALLER_NAME='Developer ID Installer: Nathaniel Nielsen'

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

productsign --sign "$MAC_SIGNING_DEVELOPER_ID_INSTALLER_NAME" \
 "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.pkg" \
 "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}_signed.pkg"

pkgutil --check-signature "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}_signed.pkg"

xcrun notarytool submit "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}_signed.pkg" \
 --apple-id "$MAC_SIGNING_NOTARIZATION_USER_NAME" \
 --team-id "$MAC_SIGNING_TEAM_ID" \
 --password "$MAC_SIGNING_NOTARIZATION_PASSWORD" \
 --wait

xcrun stapler staple "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}_signed.pkg"