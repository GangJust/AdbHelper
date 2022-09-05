package utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object ShellUtils {
    const val CHAR_SET_UTF_8 = "UTF-8"

    const val CHAR_SET_GBK = "GBK"

    const val CHAR_SET_GB2312 = "GB2312"

    fun exec(vararg command: String): Process {
        return Runtime.getRuntime().exec(command)
    }

    fun adbShell(directory: File? = null, vararg command: String): Process {
        val commandList = mutableListOf("cmd", "/c", "adb", "shell") //进入 adb shell 模式
        commandList.addAll(command)
        return ProcessBuilder()
            .directory(directory)
            .command(commandList)
            .start()
    }

    fun getResult(process: Process, charsetName: String = "UTF-8"): String {
        val streamReader = BufferedReader(InputStreamReader(process.inputStream, charsetName))
        val result = streamReader.readText()
        streamReader.close()
        return result
    }

    fun getResultFastLine(process: Process, charsetName: String = "UTF-8"): String? {
        val streamReader = BufferedReader(InputStreamReader(process.inputStream, charsetName))
        val result = streamReader.readLine()
        streamReader.close()
        return result
    }
}