package app.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import app.logic.HomeLogic
import base.mvvm.AbstractState
import res.IconRes
import utils.ShellUtils
import utils.formatAdbCommand

class HomeState : AbstractState<HomeLogic>() {
    override fun createLogic(): HomeLogic = HomeLogic()

    val leftPaneWidth = mutableStateOf(168.dp)
    val leftMenuSelectIndex = mutableStateOf(0)

    // leftMenuIconList.size == leftMenuTitleList.size
    val leftMenuIconList = mutableStateListOf(IconRes.browseActivity, IconRes.apkDocument, IconRes.portForward)
    val leftMenuTitleList = mutableStateListOf("活动信息", "应用管理", "端口转发")

    val showDropdownMenu = mutableStateOf(false)
    val currentDevice = mutableStateOf("没有ADB设备")
    val devicesList = mutableStateListOf<String>()
    val phoneBrandMap = mutableStateMapOf<String, String>()

    /// 列出ADB设备
    suspend fun loadDevices() {
        val command = "adb devices"
        val devices = ShellUtils.shell(command).trim()
        val splitList = devices.split("\n").filter { it.trim().isNotEmpty() }

        //每次加载重置
        devicesList.clear()

        //第一行是: List of devices attached 提示文字
        if (splitList.size == 1) {
            devicesList.add("没有ADB设备")
            return
        }

        //只列出活跃(device)的设备
        for (i in 1 until splitList.size) {
            val element = splitList[i]
            if (element.contains("device")) {
                val device = element.replace("device", "").trim()
                devicesList.add(device)
                //获取品牌
                phoneBrandMap[device] = getPhoneBrand(device)
            }
        }

        //默认选中第一项
        if (currentDevice.value.contains("没有ADB设备")) {
            currentDevice.value = devicesList[0]
        }
    }

    /// 获取某个ADB设备的厂商品牌
    private suspend fun getPhoneBrand(device: String): String {
        val command = "adb shell getprop ro.product.brand".formatAdbCommand(device)
        val brand = ShellUtils.shell(command)
        if (brand.isEmpty()) return "unknown"
        return brand
    }
}