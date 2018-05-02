package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.DataCache
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.data.dataCache
import com.mvcoding.mvp.trampolines
import org.junit.Test

class ToggleBehaviorTest {

    @Test
    fun `toggle behavior test`() {
        testToggleBehavior { get, set -> SettingPresenter(get.dataCache(set), trampolines) }
    }

    private class SettingPresenter(
            settingSelectedDataCache: DataCache<Boolean>,
            schedulers: RxSchedulers) : Presenter<SettingPresenter.View>(
            ToggleBehavior(settingSelectedDataCache, schedulers)) {

        interface View : ToggleBehavior.View
    }
}