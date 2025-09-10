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

# Check for missing color resources
echo "Checking for missing color resources..."
if [ ! -f "app/src/main/res/values/colors.xml" ]; then
    echo "⚠️  Creating missing colors.xml file..."
    mkdir -p app/src/main/res/values
    cat > app/src/main/res/values/colors.xml << 'EOF'
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
</resources>
EOF
fi

# Validate AndroidManifest.xml
echo "Validating AndroidManifest.xml..."
if ! xmllint --noout app/src/main/AndroidManifest.xml 2>/dev/null; then
    echo "❌ AndroidManifest.xml has XML syntax errors"
    exit 1
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
