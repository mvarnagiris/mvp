package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.trampolines
import com.mvcoding.mvp.views.ErrorResolution
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testInitializationBehavior(
        result: RESULT,
        success: SUCCESS,
        failure: FAILURE,
        error: ERROR,
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {
    testDisplaysInitializedWhenInitializationSucceeds(result, success, createPresenter)
    testDisplaysNotInitializedWhenInitializationFails(result, failure, createPresenter)
    testProceedsIfErrorIsResolved(result, success, error, createPresenter)
    testShowsErrorIfErrorIsNotResolved(error, createPresenter)
}

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testDisplaysInitializedWhenInitializationSucceeds(
        result: RESULT,
        success: SUCCESS,
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val initialize = mock<() -> Single<RESULT>>()
    val isSuccess = mock<(RESULT) -> Boolean>()
    val getSuccess = mock<(RESULT) -> SUCCESS>()
    whenever(initialize()).thenReturn(Single.just(result))
    whenever(isSuccess(result)).thenReturn(true)
    whenever(getSuccess(result)).thenReturn(success)

    val presenter = createPresenter(initialize, isSuccess, getSuccess, mock(), mock())
    val view = mock<VIEW>()

    presenter attach view

    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayInitialized(success)
        verify(view).close()
    }
}

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testDisplaysNotInitializedWhenInitializationFails(
        result: RESULT,
        failure: FAILURE,
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val initialize = mock<() -> Single<RESULT>>()
    val isSuccess = mock<(RESULT) -> Boolean>()
    val getFailure = mock<(RESULT) -> FAILURE>()
    whenever(initialize()).thenReturn(Single.just(result))
    whenever(isSuccess(result)).thenReturn(false)
    whenever(getFailure(result)).thenReturn(failure)

    val presenter = createPresenter(initialize, isSuccess, mock(), getFailure, mock())
    val view = mock<VIEW>()

    presenter attach view

    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayNotInitialized(failure)
        verify(view).close()
    }
}

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testProceedsIfErrorIsResolved(
        result: RESULT,
        success: SUCCESS,
        error: ERROR,
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val throwable = Throwable()
    val initialize = mock<() -> Single<RESULT>>()
    val isSuccess = mock<(RESULT) -> Boolean>()
    val getSuccess = mock<(RESULT) -> SUCCESS>()
    val mapError = mock<(Throwable) -> ERROR>()
    val view = mock<VIEW>()
    var value = -1
    val single = Single.create<RESULT> {
        if (value++ == -1) it.onError(throwable)
        else it.onSuccess(result)
    }
    whenever(isSuccess(result)).thenReturn(true)
    whenever(getSuccess(result)).thenReturn(success)
    whenever(initialize()).thenReturn(single)
    whenever(mapError(throwable)).thenReturn(error)
    whenever(view.showResolvableError(error)).thenReturn(Single.just(ErrorResolution.POSITIVE))

    val presenter = createPresenter(initialize, isSuccess, getSuccess, mock(), mapError)

    presenter attach view

    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showResolvableError(error)
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayInitialized(success)
        verify(view).close()
    }
}

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testShowsErrorIfErrorIsNotResolved(
        error: ERROR,
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val throwable = Throwable()
    val initialize = mock<() -> Single<RESULT>>()
    val mapError = mock<(Throwable) -> ERROR>()
    val view = mock<VIEW>()
    whenever(initialize()).thenReturn(Single.error(throwable))
    whenever(mapError(throwable)).thenReturn(error)
    whenever(view.showResolvableError(error)).thenReturn(Single.just(ErrorResolution.NEGATIVE))

    val presenter = createPresenter(initialize, mock(), mock(), mock(), mapError)

    presenter attach view

    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showResolvableError(error)
        verify(view).showError(error)
    }
}