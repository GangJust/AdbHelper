package utils

object AdbUtils {
    fun formatCommand(adbCommand: String, device: String): String {
        var adb = adbCommand

        if (device.isNotBlank()) {
            adb = adbCommand.replace("adb", "adb -s $device")
        }

        // if windows os
        if (System.getProperty("os.name").contains("Windows")) {
            adb = adb.replace("adb", "cmd /c adb").replace("grep", "findstr")
        }
        return adb
    }
}

fun String.formatAdbCommand(device: String): String {
    return AdbUtils.formatCommand(this, device)
}