[Official Documentation](https://docs.appnomix.app/docs/commerce-android#/)

## Generating a PlayStore video for the Accessibility feature of your App

### Step 1: Adding your application information to the project
1. Replace the `ic_launcher_foreground` and `ic_launcher_background` with your application icon in the `app/src/main/res/drawable` folder.
2. Replace the `app_name` in the `app/src/main/res/values/strings.xml` file with your application name.

### Step 2 : Setting up the environment
1. Install Java 21 or higher and make sure it is in your path. Run `java --version` to check the installed version
2. Install Android SDK and set ANDROID_SDK environment variable pointing to the SDK location. - e.g. `export ANDROID_SDK=/Users/username/Library/Android/sdk` or set in Windows environment variables `ANDROID_SDK=C://Users/username/Local/AppData/Android/sdk`.

### Step 3: Generating the video
1. Navigate to the project root directory and run the following script: 
   a. `playstore_video.sh` - Linux or MacOS
   b. `playstore_video.bat` - Windows
2. The script will generate a video in the project folder with the name `accessibility_permission.mp4`

Caveats:
- The script will take a few minutes to run. Please be patient and observe that the emulator starts and navigates through the app correctly.
- The emulator requires access to the internet. Please ensure that your computer is connected to the internet.
- Sometimes the Chrome browser will report a crash on the emulator, causing the video to not be generated. In this case, you can try running the script again.
- Sometimes if the emulator does not start, the video will not be generated. In this case, you can try running the script again.