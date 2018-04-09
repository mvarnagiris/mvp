package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.trampolines
import io.reactivex.Single
import org.junit.Test

class InitializationBehaviorTest {

    @Test
    fun behavior() {
        testInitializationBehavior(createPresenter())
    }

    @Test
    fun `displays initialized when initialization succeeds`() {
        testDisplaysInitializedWhenInitializationSucceeds(createPresenter())
    }

    @Test
    fun `displays not initialized when initialization fails`() {
        testDisplaysNotInitializedWhenInitializationFails(createPresenter())
    }

    @Test
    fun `proceeds if error is resolved`() {
        testProceedsIfErrorIsResolved(createPresenter())
    }

    @Test
    fun `shows error if error is not resolved`() {
        testShowsErrorIfErrorIsNotResolved(createPresenter())
    }

    private fun createPresenter(): (() -> Single<Any>, (Any) -> Boolean, (Any) -> Any, (Any) -> Any, (Throwable) -> Throwable) -> InitializationBehavior<Any, Any, Any, Throwable, InitializationBehavior.View<Any, Any, Throwable>> {
        return { initialize, isSuccess, getSuccess, getFailure, mapError -> InitializationBehavior(initialize, isSuccess, getSuccess, getFailure, mapError, trampolines) }
    }
}