package utils

/**
 * @Author: Gang
 * @Date: 2022/10/8 19:14
 * @Description: 系统平台判断
 */
object PlatformUtils {

    enum class Platform {
        Windows,
        Linux,
        Mac,
        Unknown,
    }

    fun getPlatform(): Platform {
        val osName = System.getProperty("os.name").lowercase()

        return if (osName.contains(Regex("windows"))) {
            Platform.Windows
        } else if (osName.contains(Regex("linux"))) {
            Platform.Linux
        } else if (osName.contains(Regex("mac"))) {
            Platform.Mac
        } else {
            Platform.Unknown
        }
    }
}