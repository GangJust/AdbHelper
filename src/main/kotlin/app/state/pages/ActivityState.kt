package app.state.pages

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import app.logic.pages.ActivityLogic
import app.state.HomeState
import app.view.HomeUI
import base.mvvm.AbstractState
import base.mvvm.StateManager
import compose.ComposeToast
import extensions.middle
import extensions.pathSeparator
import kotlinx.coroutines.*
import utils.ShellUtils
import utils.formatAdbCommand
import java.io.File

class ActivityState : AbstractState<ActivityLogic>() {
    override fun createLogic() = ActivityLogic()
    private val homeState = StateManager.findState<HomeState>(HomeUI::class.java)
    private val device = homeState?.currentDevice?.value ?: ""

    private var job: Job? = null
    val packageName = mutableStateOf("")
    val processName = mutableStateOf("")
    val launchActivity = mutableStateOf("")
    val resumedActivity = mutableStateOf("")
    val lastPausedActivity = mutableStateOf("")
    val stackActivities = mutableStateListOf("")

    val fullClassName = mutableStateOf(true)
    val singleTextLine = mutableStateOf(false)

    val wmSize = mutableStateOf(Size.Zero)
    var tempScreenshotFile: File = File("temp_screen.png")
    val showScreenshotPreviewWindowDialog = mutableStateOf(false)

    init {
        getWmSize()
        loadActivity()
    }

    /// 获取屏幕像素大小
    private fun getWmSize() {
        launch {
            val command = "adb shell wm size".formatAdbCommand(device)
            withContext(Dispatchers.IO) {
                ShellUtils.shell(command = command) { success, error ->
                    if (error.isNotBlank()) return@shell

                    val indexStr = "Physical size:"
                    val indexOf = success.indexOf(indexStr)
                    if (indexOf != -1) {
                        val sizeStr = success.substring(indexOf + indexStr.length)
                        val (w, h) = sizeStr.split("x")
                        wmSize.value = Size(w.toFloat(), h.toFloat())
                    }
                }
            }
        }
    }

    /// 屏幕截图
    fun screenshot() {
        launch {
            //val desktop = FileSystemView.getFileSystemView().homeDirectory.path.pathSeparator()
            //screenshotFile = File(desktop, "${Calendar.getInstance().timeInMillis}.png")

            ComposeToast.show("正在截取屏幕, 请稍后..")
            val command = "adb exec-out screencap -p > \"${tempScreenshotFile.canonicalPath.pathSeparator()}\"".formatAdbCommand(device)
            println(command)
            ShellUtils.shell(command = command) { success, error ->
                if (error.isNotBlank() || success.isNotBlank()) {
                    ComposeToast.show("屏幕截取失败!")
                    return@shell
                }
                ComposeToast.show("屏幕截取成功!")
                openScreenshotPreviewWindowDialog()
            }
        }
    }

    /// 截图预览
    fun openScreenshotPreviewWindowDialog() {
        showScreenshotPreviewWindowDialog.value = true
    }

    /// 关闭预览
    fun closeScreenshotPreviewWindowDialog() {
        tempScreenshotFile.delete()
        showScreenshotPreviewWindowDialog.value = false
    }

    /// 获取当前包名
    private suspend fun getPackageName() {
        val command = "adb shell dumpsys activity activities | grep packageName".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command)
        if (result.isBlank()) {
            packageName.value = "没有获取到包名, 请检查ADB设备是否已断开"
            return
        }

        val split = result.split("\n")
        val first = split.first().trim()
        val middle = first.middle("packageName=", " processName=")
        packageName.value = middle.ifBlank { "没有获取到包名" }
    }

    /// 获取当前进程
    private suspend fun getProcessName() {
        val command = "adb shell dumpsys activity activities | grep processName".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command)
        if (result.isBlank()) {
            processName.value = "没有获取到进程, 请检查ADB设备是否已断开"
            return
        }

        val split = result.split("\n")
        val first = split.first().trim()
        val middle = first.middle("processName=", "")
        processName.value = middle.ifBlank { "没有获取到进程" }
    }

    /// 获取启动活动
    private suspend fun getLaunchActivity() {
        val command = "adb shell dumpsys activity activities | grep mActivityComponent".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command)
        if (result.isBlank()) {
            launchActivity.value = "没有获取到启动活动, 请检查ADB设备是否已断开"
            return
        }

        val split = result.split("\n")
        val first = split.first().trim()
        val middle = first.middle("mActivityComponent=", "")
        launchActivity.value = buildFullClassName(middle).ifBlank { "没有获取到启动活动" }
    }

    /// 获取前台Activity
    private suspend fun getResumedActivity() {
        val command = "adb shell dumpsys activity activities | grep mResumedActivity".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command)
        if (result.isBlank()) {
            resumedActivity.value = "没有获取到前台活动, 请检查ADB设备是否锁屏或已断开"
            return
        }

        val split = result.split("\n")
        val middle = split.first().trim().middle("u0 ", " t")
        resumedActivity.value = buildFullClassName(middle).ifBlank { "没有获取到前台活动" }
    }

    /// 获取上次Activity
    private suspend fun getLastHistoryActivity() {
        val command = "adb shell dumpsys activity activities | grep mLastPausedActivity".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command)

        if (result.isBlank()) {
            lastPausedActivity.value = "没有获取到上次活动, 请检查ADB设备是否已断开"
            return
        }

        val split = result.split("\n")
        val middle = split.first().trim().middle("u0 ", " t")
        lastPausedActivity.value = buildFullClassName(middle).ifBlank { "没有获取到上次活动" }
    }

    /// 获取某个Package下的Activity的堆栈列表(一般是当前package)
    private suspend fun getStackActivities(packageName: String) {
        val command = "adb shell dumpsys activity activities | grep $packageName | grep Activities".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command)

        stackActivities.clear()
        if (result.isBlank()) {
            stackActivities.add("没有获取到堆栈列表, 请检查ADB设备是否已断开")
        }

        val activities = result.middle("[", "]").split(",")
        activities.forEach {
            val middle = it.trim().middle("u0 ", " t")
            if (middle.isNotEmpty()) {
                stackActivities.add(buildFullClassName(middle))
            }
        }
    }

    /// 构建全类名
    private fun buildFullClassName(middle: String): String {
        if (fullClassName.value && middle.isNotBlank()) {
            val subMiddle = middle.substring(middle.indexOf("/") + 1)
            if (subMiddle.indexOf(".") == 0) {
                return "${packageName.value}$subMiddle"
            }
            return subMiddle
        }
        return middle
    }

    /// 加载当前Activity
    fun loadActivity() {
        if (job?.isActive == true) {
            job?.cancel("重新获取Activity信息!")
        }
        job = launch {
            withContext(Dispatchers.IO) { getPackageName() }
            //并发
            val a1 = async(Dispatchers.IO) { getProcessName() }
            val a2 = async(Dispatchers.IO) { getLaunchActivity() }
            val a3 = async(Dispatchers.IO) { getResumedActivity() }
            val a4 = async(Dispatchers.IO) { getLastHistoryActivity() }
            val a5 = async(Dispatchers.IO) { getStackActivities(packageName.value) }

            /*try {
                a1.await()
                a2.await()
                a3.await()
                a4.await()
                a5.await()
            } catch (e: Exception) {
                throw e  //直接抛出
            }*/
        }
    }
}