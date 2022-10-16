package utils


object ParseVariableUtils {

    //变量文本替换
    fun parse(source: String, variableNames: Array<String>, variableValues: Array<String>): String {
        if (variableNames.size != variableValues.size) throw IllegalArgumentException("variableNames.size != variableValues.size")
        if (variableNames.isEmpty()) throw IllegalArgumentException("variableNames.size < 0")
        var resultSource = ""
        for (i in variableNames.indices) {
            resultSource = source.replace("\${${variableNames[i]}}", variableValues[i])
        }

        return resultSource
    }
}