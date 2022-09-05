package base.viewmodel

import base.data.ViewData

// 逻辑层
abstract class ViewModel<T : ViewData<*>, M : ViewData.FinalViewData> {
    protected var viewData = this.setViewData()

    protected abstract fun setViewData(): T

    abstract fun getFinalData(): M
}
