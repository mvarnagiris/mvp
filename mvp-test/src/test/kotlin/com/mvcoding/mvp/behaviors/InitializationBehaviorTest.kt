package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines
import io.reactivex.Single
import org.junit.Test

class InitializationBehaviorTest {

    @Test
    fun `initialization behavior test`() {
        val loggedInAppUser = AppUser("token")
        val notLoggedInAppUser = AppUser("")

        testInitializationBehavior(
                successResult = loggedInAppUser,
                failureResult = notLoggedInAppUser,
                getSuccess = { it.token },
                getFailure = { "no token" },
                mapError = { it }) { SplashPresenter(it, trampolines) }
    }

    private data class AppUser(val token: String) {
        val isLoggedIn = token.isNotEmpty()
    }

    private class SplashPresenter(
            getAppUser: () -> Single<AppUser>,
            schedulers: RxSchedulers) : Presenter<SplashPresenter.View>(
            InitializationBehavior(
                    getAppUser,
                    isSuccess = { it.isLoggedIn },
                    getSuccess = { it.token },
                    getFailure = { "no token" },
                    mapError = { it },
                    schedulers = schedulers)) {

        interface View : InitializationBehavior.View<String, String, Throwable>
    }
}