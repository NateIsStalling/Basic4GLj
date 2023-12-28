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

# TODO version should not be hardcoded here
productsign --sign "$MAC_SIGNING_DEVELOPER_ID_INSTALLER_NAME" \
 ./build/distributions/Basic4GLj-1.0.3.pkg \
 ./build/distributions/Basic4GLj-1.0.3_signed.pkg

pkgutil --check-signature ./build/distributions/Basic4GLj-1.0.3_signed.pkg

xcrun altool --notarize-app --primary-bundle-id "$MAC_SIGNING_BUNDLE_ID" \
 --username "$MAC_SIGNING_NOTARIZATION_USER_NAME" \
 --password "$MAC_SIGNING_NOTARIZATION_PASSWORD" \
 --file "./build/distributions/Basic4GLj-1.0.3_signed.pkg"

xcrun stapler staple "./build/distributions/Basic4GLj-1.0.3_signed.pkg"