#!/bin/bash

# prerequisite: depends on grealpath -> brew install coreutils

set -e

# Function to display usage
usage() {
    echo "Usage: $0 --app-location PATH --signing-identity IDENTITY --identifier-prefix PREFIX --entitlements PATH --inherited-entitlements PATH --mac-bundle-identifier IDENTIFIER --signing-keychain PATH --app-name NAME"
    exit 1
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --app-location) APP_LOCATION="$2"; shift ;;
        --signing-identity) SIGNING_IDENTITY="$2"; shift ;;
        --identifier-prefix) IDENTIFIER_PREFIX="$2"; shift ;;
        --entitlements) ENTITLEMENTS="$2"; shift ;;
        --inherited-entitlements) INHERITED_ENTITLEMENTS="$2"; shift ;;
        --mac-bundle-identifier) MAC_BUNDLE_IDENTIFIER="$2"; shift ;;
        --signing-keychain) SIGNING_KEYCHAIN="$2"; shift ;;
        --app-name) APP_NAME="$2"; shift ;;
        *) echo "Unknown parameter: $1"; usage ;;
    esac
    shift
done

# Ensure required arguments are provided
if [[ -z "$APP_LOCATION" || -z "$SIGNING_IDENTITY" || -z "$IDENTIFIER_PREFIX" || -z "$ENTITLEMENTS" || -z "$MAC_BUNDLE_IDENTIFIER" || -z "$APP_NAME" ]]; then
    usage
fi

# Optional args
if [[ -z "$INHERITED_ENTITLEMENTS" ]]; then
  echo "Using default entitlements for embedded tools"
  INHERITED_ENTITLEMENTS="$ENTITLEMENTS"
fi

# Paths
APP_EXECUTABLE="Contents/MacOS/$APP_NAME"

# Function to sign dylib previously extracted from a jar file and re-add it to the jar
sign_jar_dylib() {
    local jar_file=$1
    local dir=$2

    cd "$dir"
    find "." -type f \( -perm -u+x -o -name "*.dylib" -o -name "*.jar" \) ! -path "*/dylib.dSYM/Contents/*" ! -path "$APP_LOCATION/$APP_EXECUTABLE" | while read -r file; do
      if [[ -L "$file" ]]; then
          echo "Ignoring symlink: $file"
          continue
      fi

      # Fix permissions
      chmod u+w "$file"

      echo "Removing signature: $file"
      /usr/bin/codesign --remove-signature "$file"
      if [[ -z "$SIGNING_KEYCHAIN" ]]; then
        /usr/bin/codesign --force \
            -vvvv \
            --timestamp \
            --options runtime \
            --sign "$SIGNING_IDENTITY" \
            "$file"
      else
        /usr/bin/codesign --force \
            -vvvv \
            --timestamp \
            --options runtime \
            --sign "$SIGNING_IDENTITY" \
            --keychain "$SIGNING_KEYCHAIN" \
            "$file"
      fi

      jar -uvf "$jar_file" "$file"
    done
    cd ..
}

# Function to sign directories
sign_directory() {
    local dir=$1
    if [[ -d "$dir" ]]; then
      if [[ -z "$SIGNING_KEYCHAIN" ]]; then
        /usr/bin/codesign --force \
            -vvvv \
            --timestamp \
            --options runtime \
            --deep \
            --sign "$SIGNING_IDENTITY" \
            --entitlements "$INHERITED_ENTITLEMENTS" \
            --prefix "$IDENTIFIER_PREFIX" \
            "$dir"
      else
        /usr/bin/codesign --force \
            -vvvv \
            --timestamp \
            --options runtime \
            --deep \
            --sign "$SIGNING_IDENTITY" \
            --keychain "$SIGNING_KEYCHAIN" \
            --entitlements "$INHERITED_ENTITLEMENTS" \
            --prefix "$IDENTIFIER_PREFIX" \
            "$dir"
      fi
    fi
}

verify_macho_timestamps() {
    local missing_timestamp=0

    echo "Verify secure timestamps for signed Mach-O files"
    while IFS= read -r file; do
        if [[ -L "$file" ]]; then
            continue
        fi

        if ! /usr/bin/file "$file" | grep -q "Mach-O"; then
            continue
        fi

        local signature_info
        signature_info="$(/usr/bin/codesign --display --verbose=4 "$file" 2>&1)"
        if ! grep -q "^Timestamp=" <<< "$signature_info" || grep -q "^Timestamp=none" <<< "$signature_info"; then
            echo "Missing secure timestamp: $file"
            echo "$signature_info"
            missing_timestamp=1
        fi
    done < <(find "$APP_LOCATION" -type f \( -perm -u+x -o -name "*.dylib" \) ! -path "*/dylib.dSYM/Contents/*")

    if [[ "$missing_timestamp" -ne 0 ]]; then
        echo "One or more signed Mach-O files are missing secure timestamps"
        exit 1
    fi
}

# Walk through files and process them
find "$APP_LOCATION" -type f \( -perm -u+x -o -name "*.dylib" -o -name "*.jar" \) ! -path "*/dylib.dSYM/Contents/*" ! -path "$APP_LOCATION/$APP_EXECUTABLE" | while read -r file; do
    if [[ -L "$file" ]]; then
        echo "Ignoring symlink: $file"
        continue
    fi

    # Fix permissions
    chmod u+w "$file"

    # Remove existing signature
    echo "Removing signature: $file"
    /usr/bin/codesign --remove-signature "$file"

    # Sign dylib embedded in jar files
    if [[ "$file" = *.jar ]]; then
      echo "Sign embedded dylib"
      rm -rf ./temp-dylib
      unzip "$file" '*.dylib' -d './temp-dylib' || true
      sign_jar_dylib "$(grealpath "$file")" './temp-dylib'
      rm -rf ./temp-dylib

    # Sign file
    elif [[ -x "$file" ]]; then
      if [[ -z "$SIGNING_KEYCHAIN" ]]; then
        /usr/bin/codesign --force --timestamp \
            -vvvv \
            --options runtime \
            --sign "$SIGNING_IDENTITY" \
            --entitlements "$INHERITED_ENTITLEMENTS" \
            --prefix "$IDENTIFIER_PREFIX" \
            "$file"
      else
        /usr/bin/codesign --force --timestamp \
            -vvvv \
            --options runtime \
            --sign "$SIGNING_IDENTITY" \
            --keychain "$SIGNING_KEYCHAIN" \
            --entitlements "$INHERITED_ENTITLEMENTS" \
            --prefix "$IDENTIFIER_PREFIX" \
            "$file"
      fi
    else
      if [[ -z "$SIGNING_KEYCHAIN" ]]; then
        /usr/bin/codesign --force --timestamp \
            -vvvv \
            --options runtime \
            --sign "$SIGNING_IDENTITY" \
            --entitlements "$INHERITED_ENTITLEMENTS" \
            --prefix "$IDENTIFIER_PREFIX" \
            "$file"
      else
        /usr/bin/codesign --force --timestamp \
            -vvvv \
            --options runtime \
            --sign "$SIGNING_IDENTITY" \
            --keychain "$SIGNING_KEYCHAIN" \
            --entitlements "$INHERITED_ENTITLEMENTS" \
            --prefix "$IDENTIFIER_PREFIX" \
            "$file"
      fi
    fi
done


# Sign runtime and frameworks
sign_directory "$APP_LOCATION/Contents/runtime"
sign_directory "$APP_LOCATION/Contents/Frameworks"

# Sign the app bundle
if [[ -z "$SIGNING_KEYCHAIN" ]]; then
  /usr/bin/codesign --force \
      --timestamp \
      -vvvv \
      --options runtime \
      --sign "$SIGNING_IDENTITY" \
      --entitlements "$ENTITLEMENTS" \
      --prefix "$IDENTIFIER_PREFIX" \
      "$APP_LOCATION"
else
  /usr/bin/codesign --force \
      --timestamp \
      -vvvv \
      --options runtime \
      --sign "$SIGNING_IDENTITY" \
      --keychain "$SIGNING_KEYCHAIN" \
      --entitlements "$ENTITLEMENTS" \
      --prefix "$IDENTIFIER_PREFIX" \
      "$APP_LOCATION"
fi

verify_macho_timestamps
