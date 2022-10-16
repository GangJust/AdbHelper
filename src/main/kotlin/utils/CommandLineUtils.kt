package utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object CommandLineUtils {
    const val CHAR_SET_UTF_8 = "UTF-8"

    const val CHAR_SET_GBK = "GBK"

    const val CHAR_SET_GB2312 = "GB2312"

    fun winExec(vararg command: String): Process {
        println("execute command: ${command.joinToString(" ")}")
        return Runtime.getRuntime().exec(command)
    }

    fun winExec(directory: File? = null, command: String): Process {
        val commandLine = mutableListOf("cmd", "/c", command) //进入cmd, 执行命令
        println("execute command: $command")
        return ProcessBuilder()
            .directory(directory)
            .command(commandLine)
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

    fun printResult(process: Process, charsetName: String = "UTF-8") {
        println("常规内容: ${process.inputReader().readText()}")
        println("错误内容: ${process.errorReader().readText()}")
    }
}