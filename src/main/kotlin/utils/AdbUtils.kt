package utils

object AdbUtils {
    fun formatCommand(adbCommand: String, device: String): String {
        if (device.isNotBlank()) {
            return adbCommand.replace("adb", "adb -s $device")
        }
        return adbCommand
    }
}

fun String.formatAdbCommand(device: String): String {
    return AdbUtils.formatCommand(this, device)
}