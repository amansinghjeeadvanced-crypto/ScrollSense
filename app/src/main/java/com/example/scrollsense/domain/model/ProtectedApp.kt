package com.example.scrollsense.domain.model

enum class ProtectedApp(
    val packageName: String,
    val displayName: String,
    val defaultLimitMinutes: Int,
    val defaultVideoTarget: Int
) {
    INSTAGRAM(
        packageName = "com.instagram.android",
        displayName = "Instagram",
        defaultLimitMinutes = 30,
        defaultVideoTarget = 50
    ),
    YOUTUBE(
        packageName = "com.google.android.youtube",
        displayName = "YouTube",
        defaultLimitMinutes = 45,
        defaultVideoTarget = 50
    );

    companion object {
        fun fromPackageName(packageName: String): ProtectedApp? {
            return entries.firstOrNull { it.packageName.equals(packageName, ignoreCase = true) }
        }

        val allPackages: List<String> = entries.map { it.packageName }
    }
}
