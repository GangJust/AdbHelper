package app.state.pages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import app.logic.pages.AppManagerLogic
import app.model.AppDescModel
import app.state.HomeState
import app.view.HomeUI
import base.mvvm.AbstractState
import base.mvvm.StateManager
import compose.ComposeLoading
import compose.ComposeToast
import compose.WindowDialogController
import kotlinx.coroutines.async
import utils.*
import java.io.File
import java.nio.charset.Charset
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

    val allAppList = mutableStateListOf<AppDescModel>()
    val systemAppList = mutableStateListOf<AppDescModel>()
    val userAppList = mutableStateListOf<AppDescModel>()
    val filterAppList = mutableStateListOf<AppDescModel>()

    var filterKeyWords = mutableStateOf("")
    var loadingFinished = false
    var showApkInstallDialog = mutableStateOf(false)

    val currentAppList: MutableList<AppDescModel>
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

    ///??????????????????App??????????????????
    private suspend fun getAppInstallPath(packageName: String): String {
        val command = "adb shell pm path $packageName".formatAdbCommand(device)

        var appPath = ""
        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            appPath = success.trim().right("package:")
        }

        return appPath
    }

    ///??????????????????App????????????
    private suspend fun getAppBasicDesc(packageName: String, appDesc: AppDescModel) {
        val command = "adb shell dumpsys package $packageName".formatAdbCommand(device)

        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            appDesc.firstInstallTime = success.regexFind(Regex("firstInstallTime=(.*)\\s"), -1)
            appDesc.lastUpdateTime = success.regexFind(Regex("lastUpdateTime=(.*)\\s"), -1)
            appDesc.apkSigningVersion = success.regexFind(Regex("apkSigningVersion=(.*)\\s"), -1)
            appDesc.versionName = success.regexFind(Regex("versionName=(.*)\\s"), -1)
            //??????????????????
            appDesc.versionCode = success.regexFind(Regex("versionCode=(.*?)\\s"), -1)
            appDesc.minSdk = success.regexFind(Regex("minSdk=(.*?)\\s"), -1)
            appDesc.targetSdk = success.regexFind(Regex("targetSdk=(.*?)\\s"), -1)
        }
    }

    ///?????????App????????????
    private suspend fun getAppLength(installedPath: String): String {
        val command = "adb shell stat -c '%s' $installedPath".formatAdbCommand(device)

        var appLength = ""
        ShellUtils.shell(command) { success, error ->
            if (error.isNotEmpty()) return@shell
            appLength = success.trim()
        }

        return appLength
    }

    ///??????App????????????
    private suspend fun buildAppDesc(packageName: String, isSystemApp: Boolean): AppDescModel {
        val appDesc = AppDescModel()
        appDesc.packageName = packageName
        appDesc.isSystemApp = isSystemApp
        appDesc.installedPath = getAppInstallPath(packageName)
        appDesc.length = getAppLength(appDesc.installedPath).toIntOrNull() ?: 0
        getAppBasicDesc(packageName, appDesc)

        return appDesc
    }

    ///??????????????????
    // type:0 ????????????, type:1 ????????????, type:2 ????????????
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

    ///????????????App
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

    ///????????????app
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

    ///??????App??????
    private fun loadAppList() {
        launch {
            val a1 = async { loadSystemApp() }
            val a2 = async { loadUserApp() }
        }
    }

    ///??????App??????(????????????)
    fun reloadAppList() {
        if (!loadingFinished) {
            ComposeToast.show("?????????????????????!")
            return
        }

        launch {
            var increaseCount = 0
            var reduceCount = 0

            //????????????????????????
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

            //????????????????????????
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

            //??????????????????
            val packageNames = loadPackageNames(0)
            if (packageNames.size < allAppList.size) {
                val appIterator = allAppList.iterator()
                val nonExistentApp = mutableListOf<AppDescModel>()
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
                ComposeToast.show("??????${increaseCount}?????????, ??????${reduceCount}?????????!")
            } else if (increaseCount > 0) {
                ComposeToast.show("??????${increaseCount}?????????!")
            } else if (reduceCount > 0) {
                ComposeToast.show("??????${reduceCount}?????????!")
            } else {
                ComposeToast.show("????????????????????????!")
            }
        }
    }

    ///??????Apk
    fun installApk(path: String, resultMsg: (String) -> Unit) {
        val command = "adb install -r $path".formatAdbCommand(device)

        resultMsg.invoke("????????????, ???????????????????????????..")

        launch {
            ShellUtils.shell(command) { success, error ->
                if (error.isNotEmpty()) {
                    resultMsg.invoke("????????????!\n\n$error")
                    return@shell
                }
                if (success.contains("Success")) {
                    resultMsg.invoke("????????????!")
                }
            }
        }
    }

    ///??????Apk
    fun exportNotes(appDesc: AppDescModel) {
        launch {
            ComposeLoading.show("????????????, ?????????..")

            val desktop = FileSystemView.getFileSystemView().homeDirectory
            val exportApkName = File(desktop, appDesc.packageName.plus(".apk"))
            val command = "adb pull ${appDesc.installedPath} ${exportApkName.absolutePath}".formatAdbCommand(device)
            ShellUtils.shell(command) { success, error ->
                if (error.isNotBlank()) {
                    ComposeToast.show("????????????!")
                    return@shell
                }

                val speed = success.regexFind(Regex("skipped\\.\\s(.*?)/s"), -1)
                val time = success.regexFind(Regex("in (.*?)s"), -1)
                ComposeToast.show("????????????! ??????:${speed}/s, ??????:${time}s")
            }

            ComposeLoading.hide()
        }
    }

    ///??????App
    fun uninstallApp(appDesc: AppDescModel) {
        if (appDesc.isSystemApp) return

        launch {
            val command = "adb uninstall ${appDesc.packageName}".formatAdbCommand(device)
            ShellUtils.shell(command) { success, error ->
                if (error.isNotBlank()) {
                    ComposeToast.show("????????????!")
                    return@shell
                }

                if (success.contains("Success")) {
                    allAppList.remove(appDesc)
                    userAppList.remove(appDesc)
                    ComposeToast.show("????????????!")
                }
            }
        }
    }
}