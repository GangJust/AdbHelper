package base.mvvm

object StateManager {
    private val stateMaps = mutableMapOf<Class<*>, IState>()

    fun <S : IState> addState(view: Class<*>, state: S) {
        if (stateMaps.containsKey(view)) {
            stateMaps[view] = stateMaps[view] ?: state
        } else {
            stateMaps[view] = state
        }
    }

    fun <S : IState> findState(view: Class<*>): S? {
        if (!stateMaps.containsKey(view)) return null

        return stateMaps[view] as S
    }

    fun getStateMaps(): Map<Class<*>, IState> {
        return stateMaps
    }

    fun clearStateMaps() {
        stateMaps.clear()
    }
}