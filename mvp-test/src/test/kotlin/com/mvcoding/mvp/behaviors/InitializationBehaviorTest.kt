package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.trampolines
import io.reactivex.Single
import org.junit.Test

private val success = 2
private val failure = 3
private val error = Throwable()

class InitializationBehaviorTest {

    @Test
    fun behavior() {
        testInitializationBehavior({ Pres(it) }, success, failure, error) { appUser: AppUser -> appUser.isLoggedIn() }
    }

    @Test
    fun `displays initialized when initialization succeeds`() {
        testDisplaysInitializedWhenInitializationSucceeds(success, { Pres(it) }) { appUser: AppUser -> appUser.isLoggedIn() }
    }

    @Test
    fun `displays not initialized when initialization fails`() {
        testDisplaysNotInitializedWhenInitializationFails(failure, { Pres(it) }) { appUser: AppUser -> appUser.isLoggedIn() }
    }

    @Test
    fun `proceeds if error is resolved`() {
        testProceedsIfErrorIsResolved(success, { Pres(it) }) { appUser: AppUser -> appUser.isLoggedIn() }
    }

    @Test
    fun `shows error if error is not resolved`() {
        testShowsErrorIfErrorIsNotResolved(error) { it: () -> Single<AppUser> -> Pres(it) }
    }

    class Pres(getAppUser: () -> Single<AppUser>) : Presenter<Pres.View>(
            InitializationBehavior(getAppUser, { it.isLoggedIn() }, { success }, { failure }, { error }, trampolines)) {

        interface View : InitializationBehavior.View<Int, Int, Throwable>
    }

    interface AppUser {
        fun isLoggedIn(): Boolean
        fun isLoggedOut(): Boolean
    }
}