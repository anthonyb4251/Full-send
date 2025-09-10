# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all classes in the main package
-keep class com.fullsend.jarvis.** { *; }

# Keep USB Serial library classes
-keep class com.hoho.android.usbserial.** { *; }

# Keep OBD library classes
-keep class com.github.pires.obd.** { *; }

# Keep Retrofit and Gson classes
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }

# Keep TensorFlow Lite classes
-keep class org.tensorflow.lite.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Lottie animation classes
-keep class com.airbnb.lottie.** { *; }

# Keep AndroidX classes
-keep class androidx.** { *; }

# Keep Material Design classes
-keep class com.google.android.material.** { *; }
