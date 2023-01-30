package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object ShellUtils {

    //工作目录, 即是在某个目录下进行命令执行
    var workDirectory: File? = null

    suspend fun shell(command: String): String {
        val command = command.split(" ").toTypedArray()
        return shell(*command)
    }

    suspend fun shell(vararg commands: String): String {
        if (commands.isEmpty()) return ""
        val process = withContext(Dispatchers.IO) {
            Runtime.getRuntime().exec(commands, null, workDirectory)
        }
        val readText = process.inputReader().use { it.readText() }
        process.destroy()
        return readText.trim()
    }

    suspend fun shell(command: String, block: (success: String, error: String) -> Unit) {
        val command = command.split(" ").toTypedArray()
        shell(*command, block = block)
    }

    suspend fun shell(vararg commands: String, block: (success: String, error: String) -> Unit) {
        if (commands.isEmpty()) return

        val process = withContext(Dispatchers.IO) {
            Runtime.getRuntime().exec(commands, null, workDirectory)
        }

        val successText = process.inputReader(Charsets.UTF_8).use { it.readText() }
        val errorText = process.errorReader(Charsets.UTF_8).use { it.readText() }
        process.destroy()
        block.invoke(successText, errorText)
    }
}