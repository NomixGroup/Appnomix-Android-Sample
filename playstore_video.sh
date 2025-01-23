#!/bin/bash

# Check if ANDROID_SDK environment variable is set
if [ -z "$ANDROID_SDK" ]; then
    echo "Error: ANDROID_SDK environment variable is not set."
    exit 1
fi

# Check if the ANDROID_SDK directory exists
if [ ! -d "$ANDROID_SDK" ]; then
    echo "Error: ANDROID_SDK directory does not exist: $ANDROID_SDK"
    exit 1
fi

# Ensure necessary tools are available
AVD_MANAGER="$ANDROID_SDK/cmdline-tools/latest/bin/avdmanager"
SDK_MANAGER="$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager"
EMULATOR="$ANDROID_SDK/emulator/emulator"

if [ ! -f "$AVD_MANAGER" ] || [ ! -f "$SDK_MANAGER" ] || [ ! -f "$EMULATOR" ]; then
    echo "Error: Required SDK tools are missing. Ensure cmdline-tools and emulator are installed."
    exit 1
fi

# Detect system architecture
ARCH=$(uname -m)
if [ "$ARCH" == "x86_64" ]; then
    IMAGE="system-images;android-34;google_apis_playstore;x86_64"
elif [ "$ARCH" == "arm64" ] || [ "$ARCH" == "aarch64" ]; then
    IMAGE="system-images;android-34;google_apis_playstore;arm64-v8a"
else
    echo "Error: Unsupported system architecture: $ARCH"
    exit 1
fi

# Create or verify system image for Pixel 8 and API 34
echo "Checking for required system image: $IMAGE"
$SDK_MANAGER --install "$IMAGE" --verbose

# Create the emulator
AVD_NAME="Pixel_8_API_34"

# Delete the emulator after use
echo "Deleting AVD $AVD_NAME..."
$AVD_MANAGER delete avd --name "$AVD_NAME"

if [ $? -ne 0 ]; then
    echo "Error: Failed to delete the AVD."
fi

SKIN="$ANDROID_SDK/skins/pixel_8"
echo "Creating AVD named $AVD_NAME with Pixel 8 hardware profile and Play Store image..."
$AVD_MANAGER create avd --name "$AVD_NAME" --package "$IMAGE" --device "pixel" --skin "$SKIN" --force

if [ $? -ne 0 ]; then
    echo "Error: Failed to create the AVD."
    exit 1
fi

# Start the emulator and wait for it to boot
echo "Starting the emulator for AVD $AVD_NAME..."
$EMULATOR -avd "$AVD_NAME" -no-snapshot-load &

BOOT_COMPLETE=false
while [ "$BOOT_COMPLETE" != "1" ]; do
    BOOT_COMPLETE=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
    if [ "$BOOT_COMPLETE" != "1" ]; then
        echo "Waiting for emulator to boot..."
        sleep 5
    fi
done

echo "Emulator $AVD_NAME has booted successfully."

./gradlew :app:packageDebugAndroidTest

# Start screen recording
SCREEN_RECORD_FILE="/sdcard/accessibility_permission.mp4"
echo "Starting screen recording with 4 Mbps bitrate and 100% resolution..."
adb shell screenrecord --bit-rate 4000000 --size 1920x1080 "$SCREEN_RECORD_FILE" &
SCREEN_RECORD_PID=$!

# Start the UI Automator test using Gradle
echo "Starting UI Automator test using Gradle..."
./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.accessibilityjourney.AccessibilityJourneyTest

if [ $? -ne 0 ]; then
    echo "Error: Failed to run the UI Automator test."
    adb emu kill
    exit 1
fi

# Stop screen recording
echo "Stopping screen recording..."
kill $SCREEN_RECORD_PID
# Wait a bit for screen record to write file end
sleep 5
adb pull "$SCREEN_RECORD_FILE" ./accessibility_permission.mp4

# Wait before stopping the emulator
echo "Waiting 10 seconds before stopping the emulator..."
sleep 10

# Stop the emulator without saving state
echo "Stopping the emulator without saving state..."
adb emu kill

# Wait for the emulator to stop
while pgrep -f "$EMULATOR" > /dev/null; do
    echo "Waiting for emulator to stop..."
    sleep 5

done

echo "Emulator has stopped."

sleep 10
# Delete the emulator after use
echo "Deleting AVD $AVD_NAME..."
$AVD_MANAGER delete avd --name "$AVD_NAME"

if [ $? -ne 0 ]; then
    echo "Error: Failed to delete the AVD."
    exit 1
fi

echo "AVD $AVD_NAME deleted successfully."
