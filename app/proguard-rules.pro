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
-keep class tvi.webrtc.** { *; }
-keep class com.twilio.video.** { *; }
# Add your specific ProGuard rules here
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
    @androidx.databinding.* <methods>;
}
-keep class com.fatron.network_module.models.response.** { *; }
-keep class com.fatron.network_module.models.request.** { *; }
-keep class your.package.name.UserResponse.** { *; }
-keep class com.com.homemedics.app.AutoValue_* { *; }
-keep class com.fatron.network_module.models.response.** { *; }
-keepclassmembers class com.fatron.network_module.models.response.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;

}
-keepattributes *Annotation*
-keepattributes Signature
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
    @androidx.databinding.* <methods>;
}
-keep class com.squareup.moshi.**   { *; }
-keep class com.squareup.moshi.kotlin.reflect.** { *; }

# Keep all classes in your package
-keep class com.homemedics.app.firebase.** { *; }

# Keep all fields in classes annotated with @Json
-keepclassmembers class * {
    @com.squareup.moshi.Json.** <fields>;
}
-keep class com.squareup.moshi.internal.**

# Keep all fields in TabString and GenericString classes
-keepclassmembers class com.homemedics.app.firebase.TabString.** {
    <fields>;
}

-keepclassmembers class com.homemedics.app.firebase.GenericString.** {
    <fields>;
}
# Keep the NullSafeJsonAdapter class
-keep class com.squareup.moshi.internal.NullSafeJsonAdapter.** { *; }

# Keep all fields and methods in the NullSafeJsonAdapter class
-keepclassmembers class com.squareup.moshi.internal.NullSafeJsonAdapter.** {
    *;
}
# Keep all classes in the Moshi library
-keep class com.squareup.moshi.** { *; }




# Keep all methods in classes annotated with @Json
-keepclassmembers class * {
    @com.squareup.moshi.Json.* <methods>;
}


# Keep all methods in classes annotated with @JsonQualifier
-keepclassmembers class * {
    @com.squareup.moshi.JsonQualifier.* <methods>;
}
# Keep all classes in the kotlin.reflect package
-keep class kotlin.reflect.** { *; }

-keepclassmembers class com.fatron.network_module.models.request.user.UserLocation.** {
    <init>(...);
}
-keepattributes InnerClasses