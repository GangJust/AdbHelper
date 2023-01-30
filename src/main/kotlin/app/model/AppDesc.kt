package app.model

data class AppDesc(
    var packageName: String = "unknown",
    var versionName: String = "unknown",
    var versionCode: String = "unknown",
    var minSdk: String = "unknown",
    var targetSdk: String = "unknown",
    var isSystemApp: Boolean = false,
    var firstInstallTime: String = "",
    var lastUpdateTime: String = "",
    var apkSigningVersion: String = "unknown",
    var installedPath: String = "unknown",
    var length: Int = 0,
)