package ui.data

import androidx.compose.runtime.mutableStateOf
import base.data.ViewData

class MainData : ViewData<MainData.FinalMainData>() {
    var mPackageName = mutableStateOf("")
    var mResumedActivity = mutableStateOf("")
    var mLastPausedActivity = mutableStateOf("")
    var mActivityRecordList = mutableStateOf("")

    override fun finalData() = FinalMainData(
        mPackageName.value,
        mResumedActivity.value,
        mLastPausedActivity.value,
        mActivityRecordList.value,
    )

    class FinalMainData(
        val mPackageName: String,
        val mResumedActivity: String,
        val mLastPausedActivity: String,
        val mActivityRecordList: String,
    ) : FinalViewData()
}