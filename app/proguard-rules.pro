# ProGuard rules for Jarvis AI
# Currently unused because minifyEnabled is false for release builds.
# Keep this file to avoid build errors when configuring ProGuard/R8.

# Add any rules below if you enable code shrinking/obfuscation in the future.

# Keep classes used by Android components
-keep class com.fullsend.jarvis.** { *; }

# Keep ML Kit and TFLite classes and metadata
-keep class com.google.mlkit.** { *; }
-keep class org.tensorflow.lite.** { *; }

# Retrofit/OkHttp/Gson
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
