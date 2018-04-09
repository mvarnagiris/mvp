package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.views.ErrorResolution
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single

inline fun <reified RESULT : Any, reified SUCCESS : Any, reified FAILURE : Any, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testInitializationBehavior(
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {
    testDisplaysInitializedWhenInitializationSucceeds(createPresenter)
    testDisplaysNotInitializedWhenInitializationFails(createPresenter)
    testProceedsIfErrorIsResolved(createPresenter)
    testShowsErrorIfErrorIsNotResolved(createPresenter)
}

inline fun <reified RESULT : Any, reified SUCCESS : Any, reified FAILURE : Any, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testDisplaysInitializedWhenInitializationSucceeds(
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val result = mock<RESULT>()
    val success = mock<SUCCESS>()
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

inline fun <reified RESULT : Any, reified SUCCESS : Any, reified FAILURE : Any, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testDisplaysNotInitializedWhenInitializationFails(
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val result = mock<RESULT>()
    val failure = mock<FAILURE>()
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

inline fun <reified RESULT : Any, reified SUCCESS : Any, reified FAILURE : Any, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testProceedsIfErrorIsResolved(
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val result = mock<RESULT>()
    val success = mock<SUCCESS>()
    val error = mock<ERROR>()
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

inline fun <reified RESULT : Any, reified SUCCESS : Any, reified FAILURE : Any, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testShowsErrorIfErrorIsNotResolved(
        createPresenter: (() -> Single<RESULT>, (RESULT) -> Boolean, (RESULT) -> SUCCESS, (RESULT) -> FAILURE, (Throwable) -> ERROR) -> Presenter<VIEW>) {

    val error = mock<ERROR>()
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