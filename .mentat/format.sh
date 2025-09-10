#!/bin/bash

# Android resource validation and formatting script
# This script checks for common Android resource issues

echo "🔧 Running Android resource validation..."

# Check for invalid attributes in layout files
echo "Checking layout files for invalid attributes..."
find app/src/main/res/layout -name "*.xml" -exec grep -l "android:lineSpacing" {} \; | while read file; do
    echo "⚠️  Found invalid android:lineSpacing in $file - should be android:lineSpacingMultiplier"
    sed -i 's/android:lineSpacing=/android:lineSpacingMultiplier=/g' "$file"
done

# Check for missing color resources and ensure all required colors exist
echo "Checking for missing color resources..."
if [ ! -f "app/src/main/res/values/colors.xml" ]; then
    echo "⚠️  Creating missing colors.xml file..."
    mkdir -p app/src/main/res/values
fi

# Check for missing color definitions and add them
COLORS_FILE="app/src/main/res/values/colors.xml"
REQUIRED_COLORS=("orange" "red" "light_blue" "dark_blue" "primary" "primary_dark" "accent" "neon_green" "cyan" "dark_background" "black" "white" "gray")

for color in "${REQUIRED_COLORS[@]}"; do
    if ! grep -q "name=\"$color\"" "$COLORS_FILE" 2>/dev/null; then
        echo "⚠️  Adding missing color: $color"
        # Add the missing color based on its name
        case $color in
            "orange") echo "    <color name=\"orange\">#FFA500</color>" >> "$COLORS_FILE.tmp" ;;
            "red") echo "    <color name=\"red\">#FF0000</color>" >> "$COLORS_FILE.tmp" ;;
            "light_blue") echo "    <color name=\"light_blue\">#ADD8E6</color>" >> "$COLORS_FILE.tmp" ;;
            "dark_blue") echo "    <color name=\"dark_blue\">#0000A0</color>" >> "$COLORS_FILE.tmp" ;;
            "primary") echo "    <color name=\"primary\">#1E88E5</color>" >> "$COLORS_FILE.tmp" ;;
            "primary_dark") echo "    <color name=\"primary_dark\">#0D47A1</color>" >> "$COLORS_FILE.tmp" ;;
            "accent") echo "    <color name=\"accent\">#00E676</color>" >> "$COLORS_FILE.tmp" ;;
            "neon_green") echo "    <color name=\"neon_green\">#00FF41</color>" >> "$COLORS_FILE.tmp" ;;
            "cyan") echo "    <color name=\"cyan\">#00BCD4</color>" >> "$COLORS_FILE.tmp" ;;
            "dark_background") echo "    <color name=\"dark_background\">#121212</color>" >> "$COLORS_FILE.tmp" ;;
            "black") echo "    <color name=\"black\">#000000</color>" >> "$COLORS_FILE.tmp" ;;
            "white") echo "    <color name=\"white\">#FFFFFF</color>" >> "$COLORS_FILE.tmp" ;;
            "gray") echo "    <color name=\"gray\">#757575</color>" >> "$COLORS_FILE.tmp" ;;
        esac
    fi
done

# If we have missing colors, rebuild the colors.xml file
if [ -f "$COLORS_FILE.tmp" ]; then
    echo "⚠️  Rebuilding colors.xml with missing colors..."
    cat > "$COLORS_FILE" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- J.A.R.V.I.S Theme Colors -->
    <color name="primary">#1E88E5</color>
    <color name="primary_dark">#0D47A1</color>
    <color name="accent">#00E676</color>
    <color name="neon_green">#00FF41</color>
    <color name="cyan">#00BCD4</color>
    
    <!-- Background Colors -->
    <color name="dark_background">#121212</color>
    <color name="black">#000000</color>
    <color name="white">#FFFFFF</color>
    <color name="gray">#757575</color>
    
    <!-- Additional Colors for Compatibility -->
    <color name="orange">#FFA500</color>
    <color name="red">#FF0000</color>
    <color name="light_blue">#ADD8E6</color>
    <color name="dark_blue">#0000A0</color>
</resources>
EOF
    rm -f "$COLORS_FILE.tmp"
fi

# Validate AndroidManifest.xml (skip if xmllint not available)
echo "Validating AndroidManifest.xml..."
if command -v xmllint >/dev/null 2>&1; then
    if ! xmllint --noout app/src/main/AndroidManifest.xml 2>/dev/null; then
        echo "❌ AndroidManifest.xml has XML syntax errors"
        exit 1
    fi
else
    echo "ℹ️  xmllint not available, skipping XML validation"
fi

# Check for missing drawable resources
echo "Checking for missing drawable resources..."
if [ ! -f "app/src/main/res/drawable/ic_launcher.xml" ] && [ ! -f "app/src/main/res/mipmap-hdpi/ic_launcher.png" ]; then
    echo "⚠️  Creating missing launcher icon..."
    mkdir -p app/src/main/res/drawable
    cat > app/src/main/res/drawable/ic_launcher.xml << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/neon_green"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2L11,7h2v2z"/>
</vector>
EOF
fi

echo "✅ Android resource validation complete!"
