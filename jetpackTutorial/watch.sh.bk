#!/bin/bash
# Watch Kotlin files, build, install, and launch app automatically

# Get project name from argument
NEW_PROJECT_NAME="$1"
if [ -z "$NEW_PROJECT_NAME" ]; then
    echo "Usage: ./watch.sh <NewProjectName>"
    exit 1
fi

# Compute package name
PACKAGE_SUFFIX=$(echo "$NEW_PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9')
PACKAGE_NAME="com.example.$PACKAGE_SUFFIX"

echo "Watching project: $NEW_PROJECT_NAME"
echo "Package: $PACKAGE_NAME"

# Watch Kotlin files and rebuild/install on change
find app/src/main/java -name "*.kt" | entr -r bash -c "
    ./gradlew installDebug &&
    adb shell am start -n $PACKAGE_NAME/.MainActivity
"

