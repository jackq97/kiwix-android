#!/usr/bin/env bash

# Enable Wi-Fi on the emulator
adb shell svc wifi enable
adb logcat -c
# Check if the stylus_handwriting_enabled setting exists before disabling
if adb shell settings list secure | grep -q "stylus_handwriting_enabled"; then
  adb shell settings put secure stylus_handwriting_enabled 0
fi
# shellcheck disable=SC2035
adb logcat *:E -v color &

PACKAGE_NAME="org.kiwix.kiwixmobile"
TEST_PACKAGE_NAME="${PACKAGE_NAME}.test"
TEST_SERVICES_PACKAGE="androidx.test.services"
TEST_ORCHESTRATOR_PACKAGE="androidx.test.orchestrator"
# Function to check if the application is installed
is_app_installed() {
  adb shell pm list packages | grep -q "$1"
}

if is_app_installed "$PACKAGE_NAME"; then
  adb uninstall "${PACKAGE_NAME}"
fi

if is_app_installed "$TEST_PACKAGE_NAME"; then
  adb uninstall "${TEST_PACKAGE_NAME}"
fi

if is_app_installed "$TEST_SERVICES_PACKAGE"; then
  adb uninstall "${TEST_SERVICES_PACKAGE}"
fi

if is_app_installed "$TEST_ORCHESTRATOR_PACKAGE"; then
  adb uninstall "${TEST_ORCHESTRATOR_PACKAGE}"
fi

retry=0
while [ $retry -le 3 ]; do
  if ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.kiwix.kiwixmobile.localLibrary.OpeningFilesFromStorageTest "-Dorg.gradle.jvmargs=-Xmx16G -XX:+UseParallelGC" -Dfile.encoding=UTF-8; then
    echo "connectedDebugAndroidTest for file opening in tablet succeeded" >&2
    break
  else
    adb kill-server
    adb start-server
    # Enable Wi-Fi on the emulator
    adb shell svc wifi enable
    adb logcat -c
    # Check if the stylus_handwriting_enabled setting exists before disabling
    if adb shell settings list secure | grep -q "stylus_handwriting_enabled"; then
      adb shell settings put secure stylus_handwriting_enabled 0
    fi
    # shellcheck disable=SC2035
    adb logcat *:E -v color &

    if is_app_installed "$PACKAGE_NAME"; then
      adb uninstall "${PACKAGE_NAME}"
    fi
    if is_app_installed "$TEST_PACKAGE_NAME"; then
      adb uninstall "${TEST_PACKAGE_NAME}"
    fi
    if is_app_installed "$TEST_SERVICES_PACKAGE"; then
      adb uninstall "${TEST_SERVICES_PACKAGE}"
    fi

    if is_app_installed "$TEST_ORCHESTRATOR_PACKAGE"; then
      adb uninstall "${TEST_ORCHESTRATOR_PACKAGE}"
    fi
    ./gradlew clean
    retry=$(( retry + 1 ))
    if [ $retry -eq 3 ]; then
      adb exec-out screencap -p >screencap.png
      exit 1
    fi
  fi
done
