package base.data


// 数据层
abstract class ViewData<T : ViewData.FinalViewData> {

    abstract fun finalData(): T

    abstract class FinalViewData()
}