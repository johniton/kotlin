#!/bin/bash
set -e  # Exit on error
shopt -s globstar

# =========================
# CONFIGURATION - old project info
OLD_PROJECT_NAME="My Application"
OLD_PACKAGE="com.example.myapplication"
SOURCE_FOLDER="java"       # "java" or "kotlin"
ROOT_DOMAIN="com.example"
APP_MODULE="app"
# =========================

# -----------------------------
# Step 0: Ask user for new project name
read -p "Enter the new project name (display name): " INPUT_NAME

# Trim leading/trailing spaces
INPUT_NAME=$(echo "$INPUT_NAME" | sed -E 's/^[[:space:]]+//;s/[[:space:]]+$//')

# Make valid project name and package suffix
NEW_PROJECT_NAME="$INPUT_NAME"
PACKAGE_SUFFIX=$(echo "$NEW_PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9')
NEW_PACKAGE="${ROOT_DOMAIN}.${PACKAGE_SUFFIX}"

echo "✅ New project name: $NEW_PROJECT_NAME"
echo "✅ New package name: $NEW_PACKAGE"

# -----------------------------
# Step 1: Rename project folder if needed
CURRENT_DIR=$(basename "$PWD")
if [ "$CURRENT_DIR" = "$OLD_PROJECT_NAME" ]; then
    cd ..
    mv "$OLD_PROJECT_NAME" "$NEW_PROJECT_NAME"
    cd "$NEW_PROJECT_NAME"
    echo "✅ Project folder renamed"
fi

# -----------------------------
# Step 2: Update settings.gradle(.kts)
for SETTINGS in settings.gradle settings.gradle.kts; do
    if [ -f "$SETTINGS" ]; then
        sed -i "s|rootProject.name *= *\"$OLD_PROJECT_NAME\"|rootProject.name = \"$NEW_PROJECT_NAME\"|" "$SETTINGS"
        echo "✅ $SETTINGS updated"
    fi
done

# -----------------------------
# Step 3: Update app display name in strings.xml
STRINGS_FILE="$APP_MODULE/src/main/res/values/strings.xml"
if [ -f "$STRINGS_FILE" ]; then
    sed -i "s|<string name=\"app_name\">$OLD_PROJECT_NAME</string>|<string name=\"app_name\">$NEW_PROJECT_NAME</string>|" "$STRINGS_FILE"
    echo "✅ App display name updated"
fi

# -----------------------------
# Step 4: Replace old package name everywhere (only in regular files with relevant extensions)
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" \) -print0 | while IFS= read -r -d '' f; do
    sed -i "s|$OLD_PACKAGE|$NEW_PACKAGE|g" "$f"
done
echo "✅ All package references updated"

# -----------------------------
# Step 5: Replace all occurrences of old project name (only in regular files with relevant extensions)
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" \) -print0 | while IFS= read -r -d '' f; do
    sed -i "s|$OLD_PROJECT_NAME|$NEW_PROJECT_NAME|g" "$f"
done
echo "✅ All project name references updated"

# -----------------------------
# Step 6: Rename main source folders and test folders
PACKAGE_PATH_OLD=$(echo $OLD_PACKAGE | tr '.' '/')
PACKAGE_PATH_NEW=$(echo $NEW_PACKAGE | tr '.' '/')

for TARGET in main test androidTest; do
    SRC="$APP_MODULE/src/$TARGET/$SOURCE_FOLDER/$PACKAGE_PATH_OLD"
    DST="$APP_MODULE/src/$TARGET/$SOURCE_FOLDER/$PACKAGE_PATH_NEW"
    if [ -d "$SRC" ]; then
        mkdir -p "$DST"
        mv "$SRC/"* "$DST/" 2>/dev/null || true
        # Remove old empty folders
        DIR="$SRC"
        while [ "$DIR" != "$APP_MODULE/src/$TARGET/$SOURCE_FOLDER" ]; do
            rmdir "$DIR" 2>/dev/null || true
            DIR=$(dirname "$DIR")
        done
        echo "✅ $TARGET folders renamed"
    fi
done

# -----------------------------
# Step 7: Update AndroidManifest.xml
MANIFEST_FILE="$APP_MODULE/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST_FILE" ]; then
    # Update Theme.MyApplication to Theme.NewProjectName (replace spaces with underscores)
    OLD_THEME=$(echo "$OLD_PROJECT_NAME" | tr ' ' '_')
    NEW_THEME=$(echo "$NEW_PROJECT_NAME" | tr ' ' '_')
    sed -i "s|Theme.$OLD_THEME|Theme.$NEW_THEME|g" "$MANIFEST_FILE"

    # Add package attribute if missing
    if ! grep -q 'package=' "$MANIFEST_FILE"; then
        sed -i "s|<manifest |<manifest package=\"$NEW_PACKAGE\" |" "$MANIFEST_FILE"
    fi
    echo "✅ AndroidManifest.xml updated"
fi

# -----------------------------
# -----------------------------
# Step 8: Update themes in res/values XML files
# Prepare old/new theme strings
OLD_THEME=$(echo "$OLD_PROJECT_NAME" | tr -d '[:space:]')
NEW_THEME=$(echo "$NEW_PROJECT_NAME" | tr -d '[:space:]')

# Update AndroidManifest.xml
MANIFEST_FILE="$APP_MODULE/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST_FILE" ]; then
    sed -i "s|Theme.$OLD_THEME|Theme.$NEW_THEME|g" "$MANIFEST_FILE"
    # Add package attribute if missing
    if ! grep -q 'package=' "$MANIFEST_FILE"; then
        sed -i "s|<manifest |<manifest package=\"$NEW_PACKAGE\" |" "$MANIFEST_FILE"
    fi
    echo "✅ AndroidManifest.xml updated"
fi

# Update all theme references in res/values XML
find "$APP_MODULE/src/main/res" -type f -name "*.xml" -print0 | while IFS= read -r -d '' f; do
    sed -i "s|Theme.$OLD_THEME|Theme.$NEW_THEME|g" "$f"
done
echo "✅ All theme references in res/values XML updated"

# Step 9: Clean Gradle
./gradlew clean
echo "✅ Gradle cleaned"

echo "🎉 Project rename complete!"
echo "Project display name: $NEW_PROJECT_NAME"
echo "Package name: $NEW_PACKAGE"

