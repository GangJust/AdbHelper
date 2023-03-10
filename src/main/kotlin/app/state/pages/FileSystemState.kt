package app.state.pages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import app.logic.pages.FileSystemLogic
import app.model.FileModel
import app.model.FileType
import app.state.HomeState
import app.view.HomeUI
import base.mvvm.AbstractState
import base.mvvm.StateManager
import compose.ComposeDialog
import compose.ComposeLoading
import compose.ComposeToast
import extensions.pathFormat
import extensions.regexFind
import extensions.pathSeparator
import extensions.toFileLength
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import utils.ShellUtils
import utils.formatAdbCommand
import java.io.File
import javax.swing.filechooser.FileSystemView

class FileSystemState : AbstractState<FileSystemLogic>() {
    override fun createLogic() = FileSystemLogic()
    private val homeState = StateManager.findState<HomeState>(HomeUI::class.java)
    private val device = homeState?.currentDevice?.value ?: ""

    private var job: Job? = null
    val pathListScroller = LazyListState()
    val pathList = mutableStateListOf<FileModel>()

    //当前路径
    val currentPath = mutableStateOf("/")

    //当前查看文件(夹)的大小
    private var currentFileSizeJob: Job? = null
    val currentFileSize = mutableStateOf("")

    //文件导入弹窗
    val showPushDialog = mutableStateOf(false)

    //导入的文件名, 做高亮显示判断
    val pushFileName = mutableStateOf("")

    //动态弹层内容: 1(默认)=文件(夹)详情, 2=删除文件(夹)提示, 3=文件(夹)权限修改
    val dialogContentType = mutableStateOf(1)

    //权限数组
    val permGroupValue = mutableStateListOf(
        false, false, false, false,
        false, true, true, true,
        false, true, true, true,
        false, true, true, true,
    )

    init {
        loadPath()
    }

    //加载指定路径下的文件
    fun loadPath(file: File = File("/")) {
        if (job?.isActive == true) {
            job?.cancel("重新加载文件列表!")
        }
        job = launch {
            val command = "adb shell ls -la \"${file.path.pathSeparator().pathFormat()}\"".formatAdbCommand(device)
            val result = ShellUtils.shell(command = command).trim()

            //清空旧数据
            pathList.clear()

            //默认增加一个返回上一级
            pathList.add(FileModel(name = "..", path = file.path.pathSeparator(), type = FileType.DIR))

            //创建时间
            val createTimeRegx = "\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2} ".toRegex()

            //遍历该文件夹
            val fileList = result.split("\n")
            for (i in fileList.indices) {
                // example: drwxrwx---   7 system cache      4096 2023-02-07 08:33 cache
                val item = fileList[i]

                //匹配创建时间
                val range2 = createTimeRegx.find(item)?.range ?: continue //匹配失败, 没有时间, 跳过
                if (item.length < range2.last) continue
                val lastModifyTime = item.substring(range2.first, range2.last)

                //按时间匹配分割文本, 时间前面的文本做格式化, 保留一个空格, 做拆分
                val preContent = item
                    .substring(0, range2.first)
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .split(" ")

                if (preContent.size < 5) continue //长度不足, 匹配失败, 跳过

                //文件名(在创建时间之后)
                val name = item.substring(range2.last + 1).trim()
                if (name == ".") continue //跳过当前路径
                if (name == "..") continue //跳过上级目录(前面已经增加了)

                //增加文件选项
                val fModel = FileModel(
                    name = name,
                    path = file.path.pathSeparator(),
                    typePerm = preContent[0],
                    ownerName = preContent[2],
                    groupName = preContent[3],
                    lastModifyTime = lastModifyTime,
                    type = when (item.substring(0, 1)) {
                        "-" -> FileType.FILE
                        "d" -> FileType.DIR
                        "l" -> FileType.LINK_DIR
                        else -> FileType.UNKNOWN
                    }
                )

                //link链接, 单独做设置
                if (fModel.type == FileType.LINK_DIR) checkLinkType(fModel)

                pathList.add(fModel)
            }
        }
    }

    //判断链接所指向的是文件夹还是文件
    private suspend fun checkLinkType(fModel: FileModel) {
        val path = File(getLinkPah(fModel)).path.pathSeparator().pathFormat()
        val command = "adb shell ls -la \"${path}\"".formatAdbCommand(device)
        val result = ShellUtils.shell(command = command).trim()

        val split = result.split("\n")
        fModel.type = if (split.size > 1) {
            FileType.LINK_DIR
        } else if (split.isEmpty() || split[0].isEmpty()) {
            FileType.LINK_FILE
        } else {
            when (split[0].substring(0, 1)) {
                "d", "l" -> FileType.LINK_DIR
                else -> FileType.LINK_FILE
            }
        }
    }

    /// 获取链接指向的真实路径
    private fun getLinkPah(fModel: FileModel): String {
        val linkStr = "->"
        val indexOf = fModel.name.indexOf(linkStr)
        if (indexOf == -1) return "/"
        val linkName = fModel.name.substring(0, indexOf).trim()
        val linkPath = fModel.name.substring(indexOf + linkStr.length).trim()
        //如果包含路径地址符号, 则返回该路径, 否则是相对路径(本路径)下的文件
        return if (linkPath.contains("/|\\\\".toRegex())) {
            File(linkPath).path
        } else {
            File(fModel.path, linkPath).path
        }
    }

    /// 跳转到指定路径
    fun toTargetPath(path: String) {
        if (!path.contains("/|\\\\".toRegex())) {
            ComposeToast.show("请输入正确的路径!")
            return
        }
        val file = File(path)
        currentPath.value = file.path.pathSeparator()
        loadPath(file)
    }

    /// 获取某个文件(夹)大小
    fun getFileSize(fModel: FileModel) {
        if (currentFileSizeJob?.isActive == true) {
            currentFileSizeJob?.cancel("重新获取某个文件(夹)大小!")
        }

        currentFileSizeJob = launch {
            currentFileSize.value = "正在获取"
            val path = File(fModel.path, fModel.name).path.pathSeparator().pathFormat()
            val command = "adb shell du \"${path}\"".formatAdbCommand(device)
            ShellUtils.shell(command = command) { success, error ->
                if (error.isNotBlank()) {
                    currentFileSize.value = "获取失败"
                    return@shell
                }

                val list = success.trim().split("\n")
                val last = list.last()
                if (success.contains("Permission denied")) {
                    currentFileSize.value = "权限不足"
                    return@shell
                }
                if (success.contains("error")) {
                    currentFileSize.value = "获取失败"
                    return@shell
                }

                val indexOf = last.indexOf("\t")
                if (indexOf == -1) {
                    currentFileSize.value = "获取失败"
                    return@shell
                }
                val size = last.substring(0, indexOf)
                val b = (size.toLongOrNull() ?: 0) * 1024
                currentFileSize.value = "${b.toFileLength()} ($b)"
            }
        }
    }

    /// 获取某个文件(夹)权限数组
    fun getPermGroup(fModel: FileModel) {
        /*
         * d rwx rwx rwx
         * 0 123 456 789
         *  ^
         *  |
         *  v
         * rwx   123
         * rwx   456
         * rwx   789
         */
        val perm = fModel.typePerm.split("").filter { it.isNotEmpty() }

        permGroupValue.clear()
        permGroupValue.addAll(
            arrayOf(
                false, false, false, false,
                false, perm[1] == "r", perm[2] == "w", perm[3] == "x",  //owner
                false, perm[4] == "r", perm[5] == "w", perm[6] == "x",  //group
                false, perm[7] == "r", perm[8] == "w", perm[9] == "x",  //others
                /////  r               w               x
            )
        )
    }

    /// 导出文件
    fun onPullFile(fModel: FileModel) {
        launch {
            ComposeLoading.show("正在提取`${fModel.name}`, 请稍后..")
            val job = launch {
                delay(1000 * 30)
                ComposeLoading.show("文件较大, 可能耗时较久..")
                delay(1000 * 30)
                ComposeLoading.show("文件很大, 自己选的, 等着吧..")
                delay(1000 * 30)
                var i = 0
                while (true) {
                    if (i++ % 2 != 0) {
                        ComposeLoading.show("文件巨大, 都过去这么久了, 只是提醒你程序并未卡死.")
                    } else {
                        ComposeLoading.show("文件巨大, 都过去这么久了, 只是提醒你程序并未卡死..")
                    }
                    delay(1000L)
                }
            }

            val formFilePath = File(fModel.path, fModel.name).path.pathSeparator()
            val toFilePath = File(FileSystemView.getFileSystemView().homeDirectory, fModel.name).path.pathSeparator()
            val command = "adb pull \"${formFilePath}\" \"${toFilePath}\"".formatAdbCommand(device)
            ShellUtils.shell(command = command) { success, error ->
                if (error.isNotBlank()) {
                    ComposeToast.show("`${fModel.name}`提取失败, 文件可能含有shell命令不支持的特殊字符!")
                    return@shell
                }

                if (success.contains("Permission denied")) {
                    ComposeToast.show("`${fModel.name}`提取失败, 权限不足!")
                    return@shell
                }

                if (success.contains("error")) {
                    ComposeToast.show("提取失败, 文件可能含有shell命令不支持的特殊字符!")
                    return@shell
                }

                val speed = success.regexFind(Regex("skipped\\.\\s(.*?)/s"), -1)
                val time = success.regexFind(Regex("in (.*?)s"), -1)
                ComposeToast.show("提取成功! 速度:${speed}/s, 耗时:${time}s")
            }

            ComposeLoading.hide()

            if (job.isActive) {
                job.cancel()
            }
        }
    }

    /// 导入文件
    fun onPushFile(filePath: File) {
        launch {
            val formFilePath = filePath.absolutePath.pathSeparator() //注意这里的路径
            val toFilePath = File(currentPath.value, filePath.name).path.pathSeparator()
            val command = "adb push \"${formFilePath}\" \"${toFilePath}\"".formatAdbCommand(device)
            ShellUtils.shell(command = command) { success, error ->
                ComposeDialog.hide()

                if (error.isNotBlank()) {
                    ComposeToast.show("导入失败, 文件可能含有shell命令不支持的特殊字符!")
                    return@shell
                }

                if (success.contains("Read-only")) {
                    ComposeToast.show("导入失败, 该目录只读!")
                    return@shell
                }

                if (success.contains("Permission denied")) {
                    ComposeToast.show("导入失败, 权限不足!")
                    return@shell
                }

                if (success.contains("error")) {
                    ComposeToast.show("导入失败, 文件可能含有shell命令不支持的特殊字符!")
                    return@shell
                }

                val speed = success.regexFind(Regex("skipped\\.\\s(.*?)/s"), -1)
                val time = success.regexFind(Regex("in (.*?)s"), -1)
                ComposeToast.show("成功! 速度:${speed}/s, 耗时:${time}s")
                pushFileName.value = filePath.name
                toTargetPath(currentPath.value) //重新加载
            }
        }
    }

    /// 删除文件
    fun onDropFile(fModel: FileModel) {
        launch {
            val filePath = File(fModel.path, fModel.name).path.pathSeparator().pathFormat()
            val command = "adb shell rm -fr \"$filePath\"".formatAdbCommand(device)
            ShellUtils.shell(command = command) { success, error ->
                if (error.contains("Read-only")) {
                    ComposeToast.show("删除失败, 该项只读!")
                    dialogContentType.value = 1
                    return@shell
                }

                if (error.contains("Permission denied")) {
                    ComposeToast.show("删除失败, 权限不足!")
                    dialogContentType.value = 1
                    return@shell
                }

                if (error.contains("No such")) {
                    ComposeToast.show("删除失败, 操作对象不存在!")
                    dialogContentType.value = 1
                    return@shell
                }

                ComposeToast.show("删除成功!")
                ComposeDialog.hide()
                toTargetPath(currentPath.value) //重新加载
            }
        }
    }

    /// 修改权限
    fun onPermChange(fModel: FileModel) {
        launch {
            //获取权限组数据
            /*
            *   arrayOf(
            *      false, false, false, false,
            *      false, perm[5] == "r",   perm[6] == "w",   perm[7] == "x",  //owner
            *      false, perm[9] == "r",   perm[10] == "w",  perm[11] == "x",  //group
            *      false, perm[13] == "r",  perm[14] == "w",  perm[15] == "x",  //others
            *      /////  r                 w                 x
            *   )
            *   r=4, w=2, x=1, -=0; rwx=4+2+1=7; -wx=0+2+1=3
            */

            //owner
            var owner = 0
            owner += if (permGroupValue[5]) 4 else 0
            owner += if (permGroupValue[6]) 2 else 0
            owner += if (permGroupValue[7]) 1 else 0

            //group
            var group = 0
            group += if (permGroupValue[9]) 4 else 0
            group += if (permGroupValue[10]) 2 else 0
            group += if (permGroupValue[11]) 1 else 0

            //others
            var others = 0
            others += if (permGroupValue[13]) 4 else 0
            others += if (permGroupValue[14]) 2 else 0
            others += if (permGroupValue[15]) 1 else 0


            val filePath = File(fModel.path, fModel.name).path.pathSeparator().pathFormat()
            val command = "adb shell chmod -R ${owner}${group}${others} \"$filePath\"".formatAdbCommand(device)
            ShellUtils.shell(command = command) { success, error ->
                //同时为空
                if (success.trim() == error.trim()) {
                    ComposeToast.show("操作成功, 请自行查看结果!")
                    ComposeDialog.hide()
                    toTargetPath(currentPath.value) //重新加载
                } else {
                    ComposeToast.show("操作失败: ${error.trim().ifEmpty { success }}")
                }
            }
        }
    }

    /// 鼠标左键点击
    fun onItemPrimaryClick(item: FileModel, value: String) {
        if (item.name == ".." && value != "/") { //上级目录
            toTargetPath(File(item.path).parent)
        } else if (item.name != ".." && item.type == FileType.DIR) { //文件夹
            toTargetPath(File(item.path, item.name).path)
        } else if (item.type == FileType.LINK_DIR) { //链接文件夹
            toTargetPath(getLinkPah(item))
        }
    }

    /// 鼠标右键点击
    fun onItemSecondaryClick(item: FileModel, value: String, callback: (model: FileModel) -> Unit) {
        if (item.name == "..") return //上级目录

        if (item.type == FileType.LINK_FILE || item.type == FileType.LINK_DIR) { //链接
            ComposeToast.show("无法直接操作link链接!")
        } else {
            callback.invoke(item)
        }
    }
}