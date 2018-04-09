package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.trampolines
import io.reactivex.Single
import org.junit.Test

class InitializationBehaviorTest {

    private val result = 1
    private val success = 2
    private val failure = 3
    private val error = Throwable()

    @Test
    fun behavior() {
        testInitializationBehavior(result, success, failure, error, createPresenter())
    }

    @Test
    fun `displays initialized when initialization succeeds`() {
        testDisplaysInitializedWhenInitializationSucceeds(result, success, createPresenter())
    }

    @Test
    fun `displays not initialized when initialization fails`() {
        testDisplaysNotInitializedWhenInitializationFails(result, failure, createPresenter())
    }

    @Test
    fun `proceeds if error is resolved`() {
        testProceedsIfErrorIsResolved(result, success, error, createPresenter())
    }

    @Test
    fun `shows error if error is not resolved`() {
        testShowsErrorIfErrorIsNotResolved(error, createPresenter())
    }

    private fun createPresenter(): (() -> Single<Int>, (Int) -> Boolean, (Int) -> Int, (Int) -> Int, (Throwable) -> Throwable) -> InitializationBehavior<Int, Int, Int, Throwable, InitializationBehavior.View<Int, Int, Throwable>> {
        return { initialize, isSuccess, getSuccess, getFailure, mapError -> InitializationBehavior(initialize, isSuccess, getSuccess, getFailure, mapError, trampolines) }
    }
}