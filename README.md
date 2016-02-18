# weatherbus-android

### Running the app:
- Install android sdk using brew update && brew install android
- Run 'android' and select these packages on top of the default selected
  packages
  - Android 5.0.1 (API 21).
  - Extras->Google Play Services
  - Extras->Google Repository
  - Extras->Android Support Repository
  - Delete the Android Wear system images
  - If you wish to test on an emulator: Extras->Intel x86 Emulator Accelerator
    (HAXM Installer) and then use the installer to install it
    - Go to the HAXM folder (See 'brew info android' for path, probably
      something like /usr/local/Cellar/android-sdk/<android SDK
      version>/extras/intel/Hardware_Accelerated_Execution_Manager)
    - Run ./HAXM installation and enter credentials
- Select the liceneses to accept, and accept them by clicking the 'Accept License' radio button. 
- Install IntelliJ if your workstation does not have it already.
  - On a first run of IntelliJ, `JAVA_HOME` needs to be set from Configure ->
    Project Defaults -> Project Structure. We want the language level to be set
    to 8 for usual Java development, but this will be overridden to 7 in the
    Android project in particular.
- Run IntelliJ and open the build.gradle in the root of the project.
  - If it complains about the android sdk directory not being set, create a local.properties file in the root of the project.
    - The `ANDROID_HOME` directory can be found using 'brew info android'.
    - Set it in local.properties in this format: sdk.dir=<path-to-android-sdk>
- Sync Gradle
- Tell compiler that lombok is a thing 
  - In Preferences -> Plugins, search for Lombok in "Browse Repositories"
  - Install lombok plugin
- Wait for gradle to finish building.
- Run the app.
- If it complains about ANDROID_SDK not being selected, press continue anyway, and add a new Android SDK. 
  - In the Project Structure window that pops up, under Modules->Module SDK press New->Android SDK.
  - Select the same android SDK folder that you added to local.properties
  - Wait for indexing to finish.
  - Run the app again.
- Get the API key into your workstation
  - Option 1: Get your own API Key and modify the AndroidManifest appropiately.
  - Option 2: Get the shared .debug_keystore in the shared seattle beach google drive. Copy this into your ~/.android folder. 
- If there are no emulators installed, click on the '...' button and Create a virtual device.
  - Select any phone device and accept
  - Close the Android Virtual Device Manager
  - Select the device and hit OK

### Running the tests:
- Under built variants, set Test Artifact as Unit Tests (or else your tests won't run!)
- Re-sync gradle and rebuild project
- Run all tests

### Pitfalls
- If you had an old instance of IntelliJ running, or possibly a different clone
  of the app opened at some point, you may need to close IntelliJ, do
  `./gradlew clean`, then reopen the project in IntelliJ.
