package state

import androidx.compose.runtime.mutableStateOf
import config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import utils.CommandLineUtils
import utils.GTextUtils
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

class AppState {
    val config = AppConfig()
    val sdkPath: File? = if (config.settingConfig.androidSdkPath.isNotBlank()) File(config.settingConfig.androidSdkPath) else null
    val adbPath: File? = if (config.settingConfig.androidAdbPath.isNotBlank()) File(config.settingConfig.androidAdbPath) else null

    //当前设备, 默认选中第一个
    var currentDevice = mutableStateOf(getDevices().first())

    /// 获取运行设备
    fun getDevices(): List<String> {
        val process = CommandLineUtils.winExec(adbPath, "adb devices")
        val result = CommandLineUtils.getResult(process, config.settingConfig.encodingCharsetName)
        //设备的状态有 3 种: device, offline, unknown
        //device: 设备正常连接
        //offline: 连接出现异常，设备无响应
        //unknown: 没有连接设备
        val split = result.split(Regex("\n")).filter { it.contains(Regex("device")) } //过滤 `正常设备`
        if (split.size == 1) return listOf("没有adb设备")

        val devices = mutableListOf<String>()
        for (i in 1 until split.size) {
            devices.add(split[i].replace("device", "").trim()) //剔除额外字符
        }

        return devices
    }

    //安装apk
    fun installApk(apkPath: String, callback: (result: Boolean, message: String) -> Unit) {
        CoroutineScope(EmptyCoroutineContext).launch {
            val process = CommandLineUtils.winExec(adbPath, "adb -s ${currentDevice.value} install -r $apkPath")
            val readText = process.inputReader().readText()
            val errorText = process.errorReader().readText()
            if (GTextUtils.isEmpty(errorText) && readText.lowercase().contains("success")) {
                callback(true, readText)
            } else {
                callback(false, errorText.substringAfterLast("Failure"))
            }
        }.start()
    }

    //打开monitor
    fun openMonitor() {
        config.reloadConfig()
        CommandLineUtils.winExec(File(config.settingConfig.androidSdkPath,"tools"), "monitor.bat")
    }

    /// children state
    //HomeUI State
    val homeState = HomeState(this)

    //Setting State
    val settingState = SettingState(this)
}