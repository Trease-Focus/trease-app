-dontwarn java.awt.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
-dontwarn kotlinx.coroutines.**

# OkHttp checks for Android classes at runtime. They don't exist on desktop.
-dontwarn okhttp3.internal.platform.**
-dontwarn android.net.**
-dontwarn android.os.**
-dontwarn android.security.**
-dontwarn android.util.**
-dontwarn android.net.http.**

# These libraries are optional dependencies for OkHttp
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Suppress the hundreds of "duplicate class" notes about JNA
-dontnote com.sun.jna.**
