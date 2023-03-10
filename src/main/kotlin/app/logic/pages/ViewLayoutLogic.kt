package app.logic.pages

import base.mvvm.AbstractLogic
import utils.ShellUtils
import utils.formatAdbCommand
import extensions.middle

class ViewLayoutLogic : AbstractLogic() {
    override fun dispose() {

    }

    /**
     * 获取当前页面的布局
     * 可能存在问题: 比如 MiUi 会报 FileNotFoundException “/data/system/theme_config/theme_compatibility.xml”
     *             但是仍然能够取到布局信息
     */
    suspend fun uiautomatorDump(device: String, block: (success: String, fail: String) -> Unit): String {
        val command = "adb exec-out uiautomator dump /dev/tty".formatAdbCommand(device)

        var layoutXml = ""
        ShellUtils.shell(command = command) { success, error ->
            if (error.isNotBlank() && !success.contains("<?xml")) {
                block.invoke("", "布局读取失败!")
                return@shell
            }
            //截取
            val middle = success.middle("<?xml", "UI hierchary", true)
            layoutXml = "<?xml$middle"
        }
        return layoutXml
    }
}