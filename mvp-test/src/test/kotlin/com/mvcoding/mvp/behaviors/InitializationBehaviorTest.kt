package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.trampolines
import io.reactivex.Single
import org.junit.Test

class InitializationBehaviorTest {

    private val success = "success"
    private val failure = "failure"

    @Test
    fun `displays initialized when initialization succeeds`() {
        testDisplaysInitializedWhenInitializationSucceeds(success, isSuccess(), createPresenter())
    }

    @Test
    fun `displays not initialized when initialization fails`() {
        testDisplaysNotInitializedWhenInitializationFails(failure, isSuccess(), createPresenter())
    }

//    @Test
//    fun `proceeds if error is resolved`() {
//        testProceedsIfErrorIsResolved(success, { Pres(it) }) { appUser: AppUser -> appUser.isLoggedIn() }
//    }
//
//    @Test
//    fun `shows error if error is not resolved`() {
//        testShowsErrorIfErrorIsNotResolved(error) { it: () -> Single<AppUser> -> Pres(it) }
//    }
//
//    class Pres(getAppUser: () -> Single<AppUser>) : Presenter<Pres.View>(
//            InitializationBehavior(getAppUser, { it.isLoggedIn() }, { success }, { failure }, { error }, trampolines)) {
//
//        interface View : InitializationBehavior.View<Success, Int, Throwable>
//    }

    @Test
    fun `real life example`() {
        val token = Token("token")
        val throwable = Throwable()
        // When user is logged in, then success value will be Token taken out of AppUser. This is failing now
        testInitializationBehavior(token, Unit, throwable, { appUser: AppUser -> appUser.isLoggedIn() }, { SplashPresenter(it) })
    }

    private fun createPresenter(): (() -> Single<Result>) -> Presenter<InitializationBehavior.View<String, String, Throwable>> =
            { InitializationBehavior(it, isSuccess(), { success }, { failure }, { it }, trampolines) }

    private fun isSuccess() = { result: Result -> result.isSuccess() }

    interface Result {
        fun isSuccess(): Boolean
    }

    data class Token(val value: String)
    data class AppUser(val token: Token) {
        fun isLoggedIn(): Boolean = token.value.isNotEmpty()
    }

    private class SplashPresenter(initialize: () -> Single<AppUser>) : Presenter<SplashPresenter.View>(
            InitializationBehavior(initialize, { it.isLoggedIn() }, { it.token }, { Unit }, { it }, trampolines)) {
        interface View : Presenter.View, InitializationBehavior.View<Token, Unit, Throwable>
    }
}