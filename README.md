# weatherbus-android

### Runnig the app:
- Install android sdk using brew update && brew install android
- Run 'android' and select these packages on top of the default selected packages
  - Android 5.0.1 (API 21).
  - Extras->Google Play Services
  - If you wish to test on an emulator: Extras->HAXM Installer and then use the installer to install it
    - Go to the HAXM folder (See 'brew info android' for path, probably something like /usr/local/Cellar/android-sdk/<android SDK version>/extras/intel/Hardware_Accelerated_Execution_Manager)
    - Run ./HAXM installation and enter credentials
- Select the liceneses to accept, and accept them by clicking the 'Accept License' radio button. 
- Install IntelliJ if your workstation does not have it already.
- Run IntelliJ and open the build.gradle in the root of the project.
- Tell compiler that lombok is a thing 
  - Preferences -> Plugins -> Install
  - Install lombok plugin
- If it complains about the android sdk directory not being set, create a local.properties file in the root of the project.
  - The directory can be found using 'brew info android'.
  - Set it in local.properties in this format: sdk.dir=<path-to-android-sdk>
- Wait for gradle to finish building.
- Run the app.
- If it complains about ANDROID_SDK not being selected, press continue anyway, and add a new Android SDK. 
  - In the Project Structure window that pops up, under Modules->Module SDK press New->Android SDK.
  - Select the same android SDK folder that you added to local.properties
  - Wait for indexing to finish.
  - Run the app again.
- If there are no emulators installed, click on the '...' button and Create a virtual device.
  - Select any phone device and accept
  - Close the Android Virtual Device Manager
  - Select the device and hit OK

### Running the tests:
- Under built variants, set Test Artifact as Unit Tests (or else your tests won't run!)

### Pitfalls
- If you had an old instance of IntelliJ running, or possibly a different clone of the app opened at some point, you may need to close IntelliJ, do './gradlew clean', then reopen the project in IntelliJ.
