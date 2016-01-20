# weatherbus-android

### Runnig the app:
- Install android sdk using brew update && brew install android
- Run app.

### Running the tests:
- Under built variants, set Test Artifact as Unit Tests (or else your tests won't run!)
- If it complains about the android sdk directory not being set, set it in local.properties. The directory can be found using 'brew info android', similar to:
sdk.dir=/usr/local/opt/android-sdk

### Pitfalls
- If you had an old instance of IntelliJ running, or possibly a different clone of the app opened at some point, you may need to close IntelliJ, do './gradlew clean', then reopen the project in IntelliJ.
