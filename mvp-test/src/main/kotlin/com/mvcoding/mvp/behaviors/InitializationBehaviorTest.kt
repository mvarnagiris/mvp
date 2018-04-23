package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.views.ErrorResolution
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testInitializationBehavior(
        successResult: RESULT,
        failureResult: RESULT,
        crossinline getSuccess: (RESULT) -> SUCCESS,
        crossinline getFailure: (RESULT) -> FAILURE,
        crossinline mapError: (Throwable) -> ERROR,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {
    testDisplaysInitializedWhenInitializationSucceeds(successResult, getSuccess, createPresenter)
    testDisplaysNotInitializedWhenInitializationFails(failureResult, getFailure, createPresenter)
    testProceedsIfErrorIsResolved(successResult, mapError, createPresenter)
    testShowsErrorIfErrorIsNotResolved(mapError, createPresenter)
}

inline fun <RESULT, SUCCESS, reified VIEW : InitializationBehavior.View<SUCCESS, *, *>> testDisplaysInitializedWhenInitializationSucceeds(
        result: RESULT,
        crossinline getSuccess: (RESULT) -> SUCCESS,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {

    val view = mock<VIEW>()
    val initialize = mock<() -> Single<RESULT>>()
    whenever(initialize()).thenReturn(Single.just(result))
    val presenter = createPresenter(initialize)

    presenter attach view

    verify(view).showLoading()
    verify(view).hideLoading()
    verify(view).displayInitialized(getSuccess(result))
    verify(view).close()
    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayInitialized(getSuccess(result))
        verify(view).close()
    }
}

inline fun <RESULT, FAILURE, reified VIEW : InitializationBehavior.View<*, FAILURE, *>> testDisplaysNotInitializedWhenInitializationFails(
        result: RESULT,
        crossinline getFailure: (RESULT) -> FAILURE,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {

    val view = mock<VIEW>()
    val initialize = mock<() -> Single<RESULT>>()
    whenever(initialize()).thenReturn(Single.just(result))
    val presenter = createPresenter(initialize)

    presenter attach view

    verify(view).showLoading()
    verify(view).hideLoading()
    verify(view).displayNotInitialized(getFailure(result))
    verify(view).close()
    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayNotInitialized(getFailure(result))
        verify(view).close()
    }
}

inline fun <RESULT, ERROR, reified VIEW : InitializationBehavior.View<*, *, ERROR>> testProceedsIfErrorIsResolved(
        result: RESULT,
        crossinline mapError: (Throwable) -> ERROR,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {

    val throwable = Throwable()
    val view = mock<VIEW>()
    val initialize = mock<() -> Single<RESULT>>()
    var value = -1
    val initializeResult = Single.create<RESULT> { if (value++ == -1) it.onError(throwable) else it.onSuccess(result) }
    whenever(initialize()).thenReturn(initializeResult)
    whenever(view.showResolvableError(mapError(throwable))).thenReturn(Single.just(ErrorResolution.POSITIVE))
    val presenter = createPresenter(initialize)

    presenter attach view

    verify(view).showResolvableError(mapError(throwable))
    verify(view, times(2)).showLoading()
    verify(view, times(2)).hideLoading()
    verify(view).close()
    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showResolvableError(mapError(throwable))
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).close()
    }
}

inline fun <RESULT, SUCCESS, FAILURE, ERROR, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testShowsErrorIfErrorIsNotResolved(
        crossinline mapError: (Throwable) -> ERROR,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {

    val throwable = Throwable()
    val view = mock<VIEW>()
    val initialize = mock<() -> Single<RESULT>>()
    whenever(initialize()).thenReturn(Single.error(throwable))
    whenever(view.showResolvableError(mapError(throwable))).thenReturn(Single.just(ErrorResolution.NEGATIVE))
    val presenter = createPresenter(initialize)

    presenter attach view

    verify(view).showResolvableError(mapError(throwable))
    verify(view).showError(mapError(throwable))
    verify(view).showLoading()
    verify(view).hideLoading()
    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showResolvableError(mapError(throwable))
        verify(view).showError(mapError(throwable))
    }
}