package app.state.pages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import app.logic.pages.AppManagerLogic
import app.model.AppDesc
import app.state.HomeState
import app.view.HomeUI
import base.mvvm.AbstractState
import base.mvvm.StateManager
import compose.ComposeLoading
import compose.ComposeOverlay
import compose.ComposeToast
import extensions.regexFind
import extensions.right
import kotlinx.coroutines.async
import utils.*
import java.io.File
import javax.swing.filechooser.FileSystemView

class AppManagerState : AbstractState<AppManagerLogic>() {
    override fun createLogic() = AppManagerLogic()

    private val homeState = StateManager.findState<HomeState>(HomeUI::class.java)
    private val device = homeState?.currentDevice?.value ?: ""

    val currentTabIndex = mutableStateOf(0)

    private val allAppLazyListState = LazyListState()
    private val systemAppLazyListState = LazyListState()
    private val userAppLazyListState = LazyListState()
    private val filterAppLazyListState = LazyListState()

    var loadingFinished = false
    val allAppList = mutableStateListOf<AppDesc>()
    val systemAppList = mutableStateListOf<AppDesc>()
    val userAppList = mutableStateListOf<AppDesc>()
    val filterAppList = mutableStateListOf<AppDesc>()

    val filterOverlayController = ComposeOverlay()
    val filterKeyWords = mutableStateOf("")

    val showApkInstallDialog = mutableStateOf(false)
    val apkInstallMessage = mutableStateOf("")

    val currentAppList: MutableList<AppDesc>
        get() = when (currentTabIndex.value) {
            0 -> {
                allAppList
            }

            1 -> {
                systemAppList
            }

            2 -> {
                userAppList
            }

            else -> {
                filterAppList
            }
        }
    val currentLazyListState: LazyListState
        get() = when (currentTabIndex.value) {
            0 -> {
                allAppLazyListState
            }

            1 -> {
                systemAppLazyListState
            }

            2 -> {
                userAppLazyListState
            }

            else -> {
                filterAppLazyListState
            }
        }

    init {
        loadAppList()
    }

    ///通过包名获取App实际安装路径
    private suspend fun getAppInstallPath(packageName: String): String {
        val command = "adb shell pm path $packageName".formatAdbCommand(device)

        var appPath = ""
        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            appPath = success.trim().right("package:")
        }

        return appPath
    }

    ///通过包名获取App基本信息
    private suspend fun getAppBasicDesc(packageName: String, appDesc: AppDesc) {
        val command = "adb shell dumpsys package $packageName".formatAdbCommand(device)

        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            appDesc.firstInstallTime = success.regexFind(Regex("firstInstallTime=(.*)\\s"), -1)
            appDesc.lastUpdateTime = success.regexFind(Regex("lastUpdateTime=(.*)\\s"), -1)
            appDesc.apkSigningVersion = success.regexFind(Regex("apkSigningVersion=(.*)\\s"), -1)
            appDesc.versionName = success.regexFind(Regex("versionName=(.*)\\s"), -1)
            //注意正则逻辑
            appDesc.versionCode = success.regexFind(Regex("versionCode=(.*?)\\s"), -1)
            appDesc.minSdk = success.regexFind(Regex("minSdk=(.*?)\\s"), -1)
            appDesc.targetSdk = success.regexFind(Regex("targetSdk=(.*?)\\s"), -1)
        }
    }

    ///获取某App文件大小
    private suspend fun getAppLength(installedPath: String): String {
        val command = "adb shell stat -c '%s' $installedPath".formatAdbCommand(device)

        var appLength = ""
        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            appLength = success.trim()
        }

        return appLength
    }

    ///构建App详情实体
    private suspend fun buildAppDesc(packageName: String, isSystemApp: Boolean): AppDesc {
        val appDesc = AppDesc()
        appDesc.packageName = packageName
        appDesc.isSystemApp = isSystemApp
        appDesc.installedPath = getAppInstallPath(packageName)
        appDesc.length = getAppLength(appDesc.installedPath).toIntOrNull() ?: 0
        getAppBasicDesc(packageName, appDesc)

        return appDesc
    }

    ///加载包名列表
    // type:0 所有应用, type:1 系统应用, type:2 用户应用
    private suspend fun loadPackageNames(type: Int = 0): List<String> {
        val command = when (type) {
            0 -> {
                "adb shell pm list packages"
            }

            1 -> {
                "adb shell pm list packages -s"
            }

            else -> {
                "adb shell pm list packages -3"
            }
        }.formatAdbCommand(device)

        val packageNameList = mutableListOf<String>()

        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            success.split("\n").forEach {
                if (it.trim().isNotEmpty()) {
                    packageNameList.add(it.trim().right("package:"))
                }
            }
        }

        return packageNameList
    }

    ///加载系统App
    private suspend fun loadSystemApp() {
        val packageNames = loadPackageNames(1)
        packageNames.forEach {
            val appDesc = buildAppDesc(it, true)

            systemAppList.add(appDesc)
            allAppList.add(appDesc)
            loadingFinished = false
        }
        loadingFinished = true
    }

    ///加载用户app
    private suspend fun loadUserApp() {
        val packageNames = loadPackageNames(2)
        packageNames.forEach {
            val appDesc = buildAppDesc(it, false)

            userAppList.add(appDesc)
            allAppList.add(appDesc)
            loadingFinished = false
        }
        loadingFinished = true
    }

    ///加载App列表
    private fun loadAppList() {
        launch {
            val a1 = async { loadSystemApp() }
            val a2 = async { loadUserApp() }
        }
    }

    ///重新App列表(包名增量)
    fun reloadAppList() {
        if (!loadingFinished) {
            ComposeToast.show("请等待加载完成!")
            return
        }

        launch {
            var increaseCount = 0
            var reduceCount = 0

            //系统应用出现增加
            val systemPackageNames = loadPackageNames(1)
            if (systemPackageNames.size > systemAppList.size) {
                val mutableSystemPackageNames = mutableListOf(*systemPackageNames.toTypedArray())
                val systemAppIterator = systemAppList.iterator()
                while (systemAppIterator.hasNext()) {
                    val appDesc = systemAppIterator.next()
                    if (mutableSystemPackageNames.contains(appDesc.packageName)) {
                        mutableSystemPackageNames.remove(appDesc.packageName)
                    }
                }
                if (mutableSystemPackageNames.isNotEmpty()) {
                    mutableSystemPackageNames.forEach {
                        val appDesc = buildAppDesc(it, true)
                        systemAppList.add(appDesc)
                        allAppList.add(appDesc)
                    }
                    increaseCount += 1
                }
            }

            //用户应用出现增加
            val userPackageNames = loadPackageNames(2)
            if (userPackageNames.size > userAppList.size) {
                val mutableUserPackageNames = mutableListOf(*userPackageNames.toTypedArray())
                val userAppIterator = userAppList.iterator()
                while (userAppIterator.hasNext()) {
                    val appDesc = userAppIterator.next()
                    if (mutableUserPackageNames.contains(appDesc.packageName)) {
                        mutableUserPackageNames.remove(appDesc.packageName)
                    }
                }
                if (mutableUserPackageNames.isNotEmpty()) {
                    mutableUserPackageNames.forEach {
                        val appDesc = buildAppDesc(it, false)
                        userAppList.add(appDesc)
                        allAppList.add(appDesc)
                    }
                    increaseCount += 1
                }
            }

            //应用出现减少
            val packageNames = loadPackageNames(0)
            if (packageNames.size < allAppList.size) {
                val appIterator = allAppList.iterator()
                val nonExistentApp = mutableListOf<AppDesc>()
                while (appIterator.hasNext()) {
                    val appDesc = appIterator.next()
                    if (!packageNames.contains(appDesc.packageName)) {
                        nonExistentApp.add(appDesc)
                    }
                }
                if (nonExistentApp.isNotEmpty()) {
                    nonExistentApp.forEach {
                        if (it.isSystemApp) {
                            systemAppList.remove(it)
                        } else {
                            userAppList.remove(it)
                        }
                        allAppList.remove(it)
                        reduceCount += 1
                    }
                }
            }

            if (increaseCount > 0 && reduceCount > 0) {
                ComposeToast.show("新增${increaseCount}个应用, 减少${reduceCount}个应用!")
            } else if (increaseCount > 0) {
                ComposeToast.show("新增${increaseCount}个应用!")
            } else if (reduceCount > 0) {
                ComposeToast.show("减少${reduceCount}个应用!")
            } else {
                ComposeToast.show("未发现新安装应用!")
            }
        }
    }

    ///搜索应用
    fun filterApp(keywords: String) {
        filterAppList.clear()

        allAppList.forEach {
            if (it.packageName.contains(keywords)) {
                filterAppList.add(it)
            }
        }
    }

    ///安装Apk
    fun installApk(path: String) {
        val command = "adb install -r \"$path\"".formatAdbCommand(device)
        apkInstallMessage.value = "正在安装, 请注意设备安装弹窗.."
        launch {
            ShellUtils.shell(command) { success, error ->
                if (error.isNotEmpty()) {
                    apkInstallMessage.value = "安装失败!\n$error"
                    return@shell
                }
                if (success.contains("Success")) {
                    apkInstallMessage.value = "安装成功!"
                }
            }
        }
    }

    ///导出Apk
    fun exportNotes(appDesc: AppDesc) {
        launch {
            ComposeLoading.show("正在导出, 请稍后..")

            val desktop = FileSystemView.getFileSystemView().homeDirectory
            val exportApkName = File(desktop, appDesc.packageName.plus(".apk"))
            val command = "adb pull ${appDesc.installedPath} ${exportApkName.absolutePath}".formatAdbCommand(device)
            ShellUtils.shell(command) { success, error ->
                if (error.isNotBlank()) {
                    ComposeToast.show("导出失败!")
                    return@shell
                }

                val speed = success.regexFind(Regex("skipped\\.\\s(.*?)/s"), -1)
                val time = success.regexFind(Regex("in (.*?)s"), -1)
                ComposeToast.show("导出成功! 速度:${speed}/s, 耗时:${time}s")
            }

            ComposeLoading.hide()
        }
    }

    ///卸载App
    fun uninstallApp(appDesc: AppDesc) {
        if (appDesc.isSystemApp) return

        launch {
            val command = "adb uninstall ${appDesc.packageName}".formatAdbCommand(device)
            ShellUtils.shell(command) { success, error ->
                if (error.isNotBlank()) {
                    ComposeToast.show("卸载失败!")
                    return@shell
                }

                if (success.contains("Success")) {
                    allAppList.remove(appDesc)
                    userAppList.remove(appDesc)
                    filterAppList.remove(appDesc)
                    ComposeToast.show("卸载成功!")
                }
            }
        }
    }
}