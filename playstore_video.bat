@echo off

REM Check if ANDROID_SDK environment variable is set
if "%ANDROID_SDK%"=="" (
    echo Error: ANDROID_SDK environment variable is not set.
    exit /b 1
)

REM Check if the ANDROID_SDK directory exists
if not exist "%ANDROID_SDK%" (
    echo Error: ANDROID_SDK directory does not exist: %ANDROID_SDK%
    exit /b 1
)

REM Ensure necessary tools are available
set ADB=%ANDROID_SDK%\platform-tools\adb.exe
set AVD_MANAGER=%ANDROID_SDK%\cmdline-tools\latest\bin\avdmanager.bat
set SDK_MANAGER=%ANDROID_SDK%\cmdline-tools\latest\bin\sdkmanager.bat
set EMULATOR=%ANDROID_SDK%\emulator\emulator.exe

if not exist "%AVD_MANAGER%" (
    echo Error: AVD Manager not found. Ensure cmdline-tools are installed.
    exit /b 1
)
if not exist "%SDK_MANAGER%" (
    echo Error: SDK Manager not found. Ensure cmdline-tools are installed.
    exit /b 1
)
if not exist "%EMULATOR%" (
    echo Error: Emulator not found. Ensure emulator is installed.
    exit /b 1
)

REM Detect system architecture
set IMAGE=system-images;android-34;google_apis_playstore;x86_64

REM Create or verify system image for Pixel 8 and API 34
echo Checking for required system image: %IMAGE%
call "%SDK_MANAGER%" --install "%IMAGE%" --verbose

REM Create the emulator
set AVD_NAME=Pixel_8_API_34
set SKIN=%ANDROID_SDK%/skins/pixel_8
echo Creating AVD named %AVD_NAME% with Pixel 8 hardware profile and Play Store image...
call "%AVD_MANAGER%" create avd --name "%AVD_NAME%" --package "%IMAGE%" --device "pixel" --skin "%SKIN%" --force
if errorlevel 1 (
    echo Error: Failed to create the AVD.
    exit /b 1
)

REM Start the emulator and wait for it to boot
echo Starting the emulator for AVD %AVD_NAME%...
start "" "%EMULATOR%" -avd "%AVD_NAME%" -no-snapshot-load

set BOOT_COMPLETE=false
:wait_boot
for /f "tokens=*" %%b in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do set BOOT_COMPLETE=%%b
if not "%BOOT_COMPLETE%"=="1" (
    echo Waiting for emulator to boot...
    timeout /t 5 >nul
    goto wait_boot
)

echo Emulator %AVD_NAME% has booted successfully.

REM Configure the emulator to show taps
echo Enabling show taps on the emulator...
"%ADB%" shell settings put system show_touches 1

call gradlew :app:packageDebugAndroidTest

REM Start screen recording
set SCREEN_RECORD_FILE=/sdcard/accessibility_permission.mp4
echo Starting screen recording with 4 Mbps bitrate and 100%% resolution...
start /b %ADB% shell screenrecord --bit-rate 4000000 --size 1920x1080 "%SCREEN_RECORD_FILE%"
set SCREEN_RECORD_PID=%!

REM Start the UI Automator test using Gradle
echo Starting UI Automator test using Gradle...
timeout /t 10 >nul
call gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.accessibilityjourney.AccessibilityJourneyTest
if errorlevel 1 (
    echo Error: Failed to run the UI Automator test.
    "%ADB%" emu kill
    echo Deleting AVD %AVD_NAME%...
    call "%AVD_MANAGER%" delete avd --name "%AVD_NAME%"
    exit /b 1
)

REM Stop screen recording
echo Stopping screen recording...
for /f "tokens=2 delims=," %%a in ('wmic process where "Name='adb.exe'" get ProcessId /format:csv') do set PID=%%a
if defined PID (
    echo Stopping screen recording %PID%...
    taskkill /PID %PID% /F
    echo Recording stopped.
) else (
    echo No active screen recording found.
)

REM Wait for screen record process to write end of file
timeout /t 5 >nul

"%ADB%" pull "%SCREEN_RECORD_FILE%" .\accessibility_permission.mp4
"%ADB%" shell rm "%SCREEN_RECORD_FILE%"

REM Wait before stopping the emulator
echo Waiting 10 seconds before stopping the emulator...
timeout /t 10 >nul

REM Stop the emulator without saving state
echo Stopping the emulator without saving state...
"%ADB%" emu kill

REM Wait for the emulator to stop
:wait_stop
for /f "tokens=*" %%c in ('tasklist ^| findstr /i "%EMULATOR%"') do set EMULATOR_RUNNING=%%c
if defined EMULATOR_RUNNING (
    echo Waiting for emulator to stop...
    timeout /t 5 >nul
    goto wait_stop
)

echo Emulator has stopped.

REM Delete the emulator after use
echo Deleting AVD %AVD_NAME%...
call "%AVD_MANAGER%" delete avd --name "%AVD_NAME%"
if errorlevel 1 (
    echo Error: Failed to delete the AVD.
    exit /b 1
)

echo AVD %AVD_NAME% deleted successfully.
exit /b 0
