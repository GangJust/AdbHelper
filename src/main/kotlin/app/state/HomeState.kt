package app.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import app.logic.HomeLogic
import base.mvvm.AbstractState
import kotlinx.coroutines.delay
import res.IconRes
import utils.ShellUtils
import utils.formatAdbCommand

class HomeState : AbstractState<HomeLogic>() {
    override fun createLogic(): HomeLogic = HomeLogic()

    val leftPaneWidth = mutableStateOf(172.dp)
    val leftMenuSelectIndex = mutableStateOf(0)

    // leftMenuIconList.size == leftMenuTitleList.size
    val leftMenuIconList = mutableStateListOf(
        IconRes.browseActivity,
        IconRes.apkDocument,
        IconRes.fileSystem,
        IconRes.portForward,
        IconRes.viewLayout,
    )
    val leftMenuTitleList = mutableStateListOf(
        "活动信息",
        "应用管理",
        "文件管理",
        "端口转发",
        "布局分析",
    )

    val currentDevice = mutableStateOf("")
    val devicesList = mutableStateListOf<String>()
    val phoneAbiMap = mutableStateMapOf<String, String>()
    val phoneBrandMap = mutableStateMapOf<String, String>()

    val showConnectWifiDeviceView = mutableStateOf(false)

    init {
        loadDevices()
    }

    /// 列出ADB设备
    fun loadDevices() {
        launch {
            //会有一个 snapshot 异常, 可以通过 LaunchedEffect{} 缓解, 但是 LaunchedEffect 只允许在 @Composable 注解方法下运行, 这里就写200毫秒延时
            delay(200L)

            val command = "adb devices".formatAdbCommand("")
            val devices = ShellUtils.shell(command = command).trim()
            val splitList = devices.split("\n").filter { it.trim().isNotEmpty() }

            //每次加载重置
            devicesList.clear()

            //第一行是: List of devices attached 提示文字
            if (splitList.size == 1 || splitList.isEmpty()) {
                return@launch
            }

            //只列出活跃(device)的设备
            for (i in 1 until splitList.size) {
                val element = splitList[i]
                if (element.contains("device")) {
                    val device = element.replace("device", "").trim()
                    devicesList.add(device)
                    //获取Abi系统架构
                    phoneAbiMap[device] = getAbi(device)
                    phoneBrandMap[device] = getBrand(device)
                }
            }

            if (currentDevice.value.isEmpty()) {
                currentDevice.value = devicesList[0]
            }
        }
    }

    /// 获取某个ADB设备的系统架构
    private suspend fun getAbi(device: String): String {
        val command = "adb shell getprop ro.product.cpu.abi".formatAdbCommand(device)
        val abi = ShellUtils.shell(command = command)
        if (abi.isEmpty()) return "unknown"
        return abi.trim()
    }

    /// 获取某个ADB设备的系统品牌
    private suspend fun getBrand(device: String): String {
        val command = "adb shell getprop ro.product.brand".formatAdbCommand(device)
        val brand = ShellUtils.shell(command = command)
        if (brand.isEmpty()) return "unknown"
        return brand.trim()
    }

    /// 通过Wifi连接ADB设备
    fun wifiConnect(ipAndPort: String, block: (Boolean) -> Unit) {
        launch {
            val command = "adb connect $ipAndPort".formatAdbCommand("")
            ShellUtils.shell(command = command) { success, error ->
                if (error.isNotBlank() || !success.contains("connected")) {
                    block.invoke(false)
                    return@shell
                }
                block.invoke(true)
            }
        }
    }
}