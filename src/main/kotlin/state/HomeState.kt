package state

import androidx.compose.runtime.mutableStateOf
import config.AppConfig
import utils.CommandLineUtils
import utils.GTextUtils
import utils.ParseVariableUtils
import utils.PlatformUtils
import java.io.File

/**
 * @Author: Gang
 * @Date: 2022/10/7 00:51
 * @Description:
 */
class HomeState(private val state: AppState) {
    val config = state.config

    //当前包名, 默认加载一次
    var currentPackage = mutableStateOf(loadCurrentPackage())

    //当前活动名, 默认加载一次
    var currentActivity = mutableStateOf(loadCurrentActivity())

    //上次活动名, 默认加载一次
    var lastActivity = mutableStateOf(loadLastActivity())

    //活动栈信息, 默认加载一次
    var historyActivity = mutableStateOf(loadStackActivity())

    /// 重新加载
    fun reloadDetail() {
        currentPackage.value = loadCurrentPackage()
        currentActivity.value = loadCurrentActivity()
        lastActivity.value = loadLastActivity()
        historyActivity.value = loadStackActivity()
    }

    /// 当前包名
    private fun loadCurrentPackage(): String {
        val currentPackage = config.commandLineConfig.currentPackage
        val process = CommandLineUtils.winExec(state.adbPath, formatCommand(currentPackage))
        return getCommonResultFastLine(process, "=", " ")
    }

    /// 当前顶部活动
    private fun loadCurrentActivity(): String {
        val currentActivity = config.commandLineConfig.currentActivity
        val process = CommandLineUtils.winExec(state.adbPath, formatCommand(currentActivity))
        return getCommonResultFastLine(process, "u0 ", " t")
    }

    /// 上一次活动
    private fun loadLastActivity(): String {
        val currentActivity = config.commandLineConfig.lastActivity
        val process = CommandLineUtils.winExec(state.adbPath, formatCommand(currentActivity))
        return getCommonResultFastLine(process, "u0 ", " t")
    }

    /// 活动栈信息
    private fun loadStackActivity(): List<String> {
        val currentActivity = ParseVariableUtils.parse(
            config.commandLineConfig.stackActivity,
            arrayOf("currentPackage"),
            arrayOf(currentPackage.value),
        ) //替换当前包名
        val process = CommandLineUtils.winExec(state.adbPath, formatCommand(currentActivity))
        val contents = getCommonResult(process)
        if (contents.contains("获取失败")) {
            return listOf(GTextUtils.to(contents))
        }
        val stringList = contents.split("\n")
        val contentList: MutableList<String> = mutableListOf()
        for (s in stringList) {
            if (s.contains("u0") && s.contains(" t")) {
                val s1 = s.substring(s.indexOf("u0") + 2, s.lastIndexOf(" t"))
                contentList.add(GTextUtils.to(s1))
            } else {
                contentList.add(GTextUtils.to(s))
            }
        }
        return contentList
    }

    /// 格式化命令
    private fun formatCommand(command: String): String {
        var command = command.replace("adb shell", "adb -s ${state.currentDevice.value} shell")
        if (PlatformUtils.getPlatform() == PlatformUtils.Platform.Windows) {
            command = command.replace(" grep ", " findstr ")
        }
        return command
    }

    /// 通用获取
    private fun getCommonResultFastLine(process: Process, startText: String, endText: String): String {
        val resultContents = GTextUtils.to(CommandLineUtils.getResultFastLine(process, config.settingConfig.encodingCharsetName))
        if (GTextUtils.isEmpty(resultContents)) {
            return "获取失败，可能存在以下情况（如：锁屏、黑屏等）"
        }

        val resultContent = resultContents.substring(
            resultContents.indexOf(startText) + startText.length,
            resultContents.lastIndexOf(endText)
        )
        process.destroy()
        return resultContent
    }

    /// 通用获取
    private fun getCommonResult(process: Process): String {
        val resultContents =
            GTextUtils.to(CommandLineUtils.getResult(process, config.settingConfig.encodingCharsetName))
        if (GTextUtils.isEmpty(resultContents)) {
            return "获取失败，可能存在以下情况（如：锁屏、黑屏等）"
        }
        process.destroy()
        return resultContents
    }
}