# Add project specific ProGuard rules here.

# Hilt, Room, ML Kit, Compose, and Coroutines ship their own consumer rules,
# so only app-specific keeps and diagnostics are needed here.

# Keep source file + line numbers for readable crash reports, hide the original name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room entities: guard against renaming that could break column mapping.
-keep class com.meruvakirankumar.memora.data.local.entity.** { *; }

# BroadcastReceivers referenced from the manifest (guard injected fields).
-keep class com.meruvakirankumar.memora.platform.scheduling.ReminderReceiver { *; }
-keep class com.meruvakirankumar.memora.platform.scheduling.BootReceiver { *; }

# ML Kit text recognition (belt-and-suspenders; ML Kit ships its own rules).
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
