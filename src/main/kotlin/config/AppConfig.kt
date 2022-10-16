package config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import org.ini4j.Wini
import utils.ColorUtils
import utils.PlatformUtils
import utils.UnitUtils
import java.io.File

class AppConfig {
    var settingConfig: SettingConfig = initSettingConfig()
        private set
    var themeConfig: ThemeConfig = initThemeConfig()
        private set
    var commandLineConfig: CommandLineConfig = initCommandLineConfig()
        private set

    // 配置文件夹
    private fun getConfigDir(): String {
        val configPath = File("./", "config")
        if (!configPath.exists()) configPath.mkdirs()
        return configPath.absolutePath
    }

    /// [设置]配置文件
    private fun initSettingConfig(): SettingConfig {
        // 初始化[设置]配置文件
        val settingFile = File(getConfigDir(), "setting.ini")
        if (!settingFile.exists()) {
            if (settingFile.createNewFile()) {
                saveSettingConfig(
                    SettingConfig(
                        "",
                        "",
                        "GBK",
                    )
                )
            }
        }
        // 加载[设置]配置文件
        return readSettingConfig()
    }

    /// [主题]配置文件
    private fun initThemeConfig(): ThemeConfig {
        // 初始化[主题]配置文件
        val themeFile = File(getConfigDir(), "theme.ini")
        if (!themeFile.exists()) {
            if (themeFile.createNewFile()) {
                saveThemeConfig(
                    ThemeConfig(
                        ColorUtils.parseColor("#00CD66"),
                        ColorUtils.parseColor("#409EFF"),
                        ColorUtils.parseColor("#E6A23C"),
                        UnitUtils.parseSp("16sp"),
                    )
                )
            }
        }
        // 加载[主题]配置文件
        return readThemeConfig()
    }

    /// [命令行]配置文件
    private fun initCommandLineConfig(): CommandLineConfig {
        // 初始化[命令行]配置文件
        val commandLineFile = File(getConfigDir(), "commandLine.ini")
        if (!commandLineFile.exists()) {
            if (commandLineFile.createNewFile()) {
                //Windows 平台下使用`findstr`
                if (PlatformUtils.getPlatform() == PlatformUtils.Platform.Windows) {
                    saveCommandLineConfig(
                        CommandLineConfig(
                            "adb shell dumpsys activity activities | findstr packageName",
                            "adb shell dumpsys activity activities | findstr mResumedActivity",
                            "adb shell dumpsys activity activities | findstr mLastPausedActivity",
                            "adb shell dumpsys activity | findstr Run | findstr ActivityRecord | findstr \${currentPackage}",
                        )
                    )
                } else {
                    saveCommandLineConfig(
                        CommandLineConfig(
                            "adb shell dumpsys activity activities | grep packageName",
                            "adb shell dumpsys activity activities | grep mResumedActivity",
                            "adb shell dumpsys activity activities | grep mLastPausedActivity",
                            "adb shell dumpsys activity | grep -i run | grep \${currentPackage}",
                        )
                    )
                }
            }
        }

        // 加载[命令行]配置文件
        return readCommandLineConfig()
    }

    /// 保存[设置]配置
    fun saveSettingConfig(config: SettingConfig) {
        val settingFile = File(getConfigDir(), "setting.ini")
        val wini = Wini(settingFile)
        wini.put("setting", "androidSdkPath", config.androidSdkPath) //AndroidSdk路径
        wini.put("setting", "androidAdbPath", config.androidAdbPath) //Adb路径
        wini.put("setting", "encodingCharsetName", config.encodingCharsetName) //编码格式
        wini.store()
    }

    /// 读取[设置]配置
    fun readSettingConfig(): SettingConfig {
        val settingFile = File(getConfigDir(), "setting.ini")
        val wini = Wini(settingFile)
        val androidSdkPath = wini.get("setting", "androidSdkPath", String::class.java)
        val androidAdbPath = wini.get("setting", "androidAdbPath", String::class.java)
        val encodingCharsetName = wini.get("setting", "encodingCharsetName", String::class.java)

        return SettingConfig(
            androidSdkPath,
            androidAdbPath,
            encodingCharsetName,
        )
    }

    /// 保存[主题]配置
    fun saveThemeConfig(config: ThemeConfig) {
        val themeFile = File(getConfigDir(), "theme.ini")
        val wini = Wini(themeFile)
        wini.put("theme/color", "devicesButtonColor", ColorUtils.toHexColor(config.devicesButtonColor)) //选择设备按钮颜色
        wini.put("theme/color", "refreshButtonColor", ColorUtils.toHexColor(config.refreshButtonColor)) //刷新按钮颜色
        wini.put("theme/color", "menuButtonColor", ColorUtils.toHexColor(config.menuButtonColor)) //菜单按钮颜色
        wini.put("theme/size", "textSize", UnitUtils.toStringSp(config.textSize)) //全局文字大小
        wini.store()
    }

    /// 读取[主题]配置
    fun readThemeConfig(): ThemeConfig {
        val themeFile = File(getConfigDir(), "theme.ini")
        val wini = Wini(themeFile)
        val devicesButtonColor = ColorUtils.parseColor(wini.get("theme/color", "devicesButtonColor", String::class.java))
        val refreshButtonColor = ColorUtils.parseColor(wini.get("theme/color", "refreshButtonColor", String::class.java))
        val menuButtonColor = ColorUtils.parseColor(wini.get("theme/color", "menuButtonColor", String::class.java))
        val textSize = UnitUtils.parseSp(wini.get("theme/size", "textSize", String::class.java))

        return ThemeConfig(
            devicesButtonColor,
            refreshButtonColor,
            menuButtonColor,
            textSize,
        )
    }

    /// 保存[命令行]配置
    fun saveCommandLineConfig(config: CommandLineConfig) {
        val commandLineFile = File(getConfigDir(), "commandLine.ini")
        val wini = Wini(commandLineFile)
        wini.put("commandLine", "currentPackage", config.currentPackage) //获取当前包名
        wini.put("commandLine", "currentActivity", config.currentActivity) //获取当前活动
        wini.put("commandLine", "lastActivity", config.lastActivity) //获取上次活动
        wini.put("commandLine", "stackActivity", config.stackActivity) //获取活动堆栈信息
        wini.store()
    }

    /// 读取[命令行]配置
    fun readCommandLineConfig(): CommandLineConfig {
        val commandLineFile = File(getConfigDir(), "commandLine.ini")
        val wini = Wini(commandLineFile)
        val currentPackage = wini.get("commandLine", "currentPackage", String::class.java)
        val currentActivity = wini.get("commandLine", "currentActivity", String::class.java)
        val lastActivity = wini.get("commandLine", "lastActivity", String::class.java)
        val historyActivity = wini.get("commandLine", "stackActivity", String::class.java)

        return CommandLineConfig(
            currentPackage,
            currentActivity,
            lastActivity,
            historyActivity,
        )
    }

    //重新加载配置文件
    fun reloadConfig() {
        settingConfig = readSettingConfig()
        themeConfig = readThemeConfig()
        commandLineConfig = readCommandLineConfig()
    }

    //[设置]
    data class SettingConfig(
        var androidSdkPath: String,  //AndroidSdk路径
        var androidAdbPath: String,  //Adb路径
        var encodingCharsetName: String,  //编码格式
    )

    //[主题]
    data class ThemeConfig(
        var devicesButtonColor: Color,
        var refreshButtonColor: Color,
        var menuButtonColor: Color,
        var textSize: TextUnit,
    )

    //[命令行]
    data class CommandLineConfig(
        var currentPackage: String,   //当前包名
        var currentActivity: String,  //当前活动
        var lastActivity: String,  //上次活动
        var stackActivity: String,  //活动堆栈信息
    )
}