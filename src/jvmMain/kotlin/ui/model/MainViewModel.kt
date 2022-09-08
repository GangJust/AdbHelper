package ui.model

import base.viewmodel.ViewModel
import ui.data.MainData
import utils.GTextUtils
import utils.ShellUtils
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader

class MainViewModel : ViewModel<MainData, MainData.FinalMainData>() {
    private var charsetName = ShellUtils.CHAR_SET_GBK

    override fun setViewData(): MainData = MainData()

    override fun getFinalData(): MainData.FinalMainData = viewData.finalData()

    fun setCharsetName(charsetName: String) {
        this.charsetName = charsetName
    }

    fun onRefresh() {
        getCurrentPackageName()
        getResumedActivity()
        getLastPausedActivity()
        getActivityRecordList()
    }

    /**
     * 检查是否存在 adb 设备
     */
    fun hasDevices() {
        val process = ShellUtils.adbShell(null, "adb devices")
        println(ShellUtils.getResult(process, charsetName))
    }

    /**
     * 获取当前 App 的 packagename
     */
    private fun getCurrentPackageName() {
        val process = ShellUtils.adbShell(null, "dumpsys activity activities | grep packageName")
        viewData.mPackageName.value = getCommonResult(process, "=", " ")
    }

    /**
     * 获取当前 activity
     */
    private fun getResumedActivity() {
        val process = ShellUtils.adbShell(null, "dumpsys activity activities | grep mResumedActivity")
        viewData.mResumedActivity.value = getCommonResult(process, "u0 ", " t")
    }

    /**
     * 获取上一个 activity
     */
    private fun getLastPausedActivity() {
        val process = ShellUtils.adbShell(null, "dumpsys activity activities | grep mLastPausedActivity")
        viewData.mLastPausedActivity.value = getCommonResult(process, "u0 ", " t")
    }

    /**
     * 通过当前包名获取 ActivityRecord(活动栈)
     */
    private fun getActivityRecordList() {
        val process = ShellUtils.adbShell(null, "dumpsys activity | grep -i run | grep ${viewData.mPackageName.value}")
        val contents = getCommonResultAll(process)
        if (contents.contains("获取失败")) {
            viewData.mActivityRecordList.value = listOf(GTextUtils.to(contents))
            return
        }
        val stringList = contents.split("\n")
        val contentList: MutableList<String> = mutableListOf()
        for (s in stringList) {
            val s1 = s.substring(s.indexOf("u0") + 2, s.lastIndexOf(" t"))
            contentList.add(GTextUtils.to(s1))
        }
        viewData.mActivityRecordList.value = contentList
    }

    /**
     * 通用获取
     */
    private fun getCommonResult(process: Process, startText: String, endText: String): String {
        val resultContents = GTextUtils.to(ShellUtils.getResultFastLine(process, charsetName))
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

    /**
     * 通用获取
     */
    private fun getCommonResultAll(process: Process): String {
        val resultContents = GTextUtils.to(ShellUtils.getResult(process, charsetName))
        if (GTextUtils.isEmpty(resultContents)) {
            return "获取失败，可能存在以下情况（如：锁屏、黑屏等）"
        }
        process.destroy()
        return resultContents
    }
}