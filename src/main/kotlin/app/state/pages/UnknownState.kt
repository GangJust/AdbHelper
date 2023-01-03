package app.state.pages

import app.logic.pages.UnknownLogic
import base.mvvm.AbstractState

class UnknownState : AbstractState<UnknownLogic>() {
    override fun createLogic() = UnknownLogic()
}