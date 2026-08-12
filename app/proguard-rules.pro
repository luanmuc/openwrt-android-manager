# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ========== 通用规则 ==========
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ========== Jetpack Compose ==========
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.animation.** { *; }

# ========== Kotlin ==========
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ========== Retrofit & OkHttp ==========
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit接口
-keep interface * {
    @retrofit2.http.* <methods>;
}

# ========== Gson ==========
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 数据类不混淆
-keep class com.luanmuc.openwrtmanager.data.model.** { *; }

# ========== Room ==========
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Room实体类
-keep class com.luanmuc.openwrtmanager.data.entity.** { *; }

# ========== DataStore ==========
-keep class androidx.datastore.** { *; }

# ========== Navigation ==========
-keep class androidx.navigation.** { *; }

# ========== Lifecycle ==========
-keep class androidx.lifecycle.** { *; }

# ========== 项目代码保护 ==========
# 保持ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# 保持Repository
-keep class com.luanmuc.openwrtmanager.data.repository.** { *; }

# 保持服务
-keep class com.luanmuc.openwrtmanager.service.** { *; }

# 保持Widget
-keep class com.luanmuc.openwrtmanager.widget.** { *; }

# ========== 调试信息 ==========
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========== 忽略警告 ==========
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement