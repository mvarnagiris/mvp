package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Presenter
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single

inline fun <
        reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>,
        reified RESULT : Any,
        SUCCESS,
        FAILURE,
        ERROR> testInitializationBehavior(
        success: SUCCESS,
        failure: FAILURE,
        error: ERROR,
        isSuccess: (RESULT) -> Boolean,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {
    testDisplaysInitializedWhenInitializationSucceeds(success, isSuccess, createPresenter)
    testDisplaysNotInitializedWhenInitializationFails(failure, isSuccess, createPresenter)
//    testProceedsIfErrorIsResolved(success, createPresenter, stub)
//    testShowsErrorIfErrorIsNotResolved(error, createPresenter)
}

inline fun <reified RESULT : Any, SUCCESS, reified VIEW : InitializationBehavior.View<SUCCESS, *, *>> testDisplaysInitializedWhenInitializationSucceeds(
        success: SUCCESS,
        isSuccess: (RESULT) -> Boolean,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {

    val result = mock<RESULT>()
    whenever(isSuccess(result)).thenReturn(true)
    val presenter = createPresenter { Single.just(result) }
    val view = mock<VIEW>()

    presenter attach view

    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayInitialized(success)
        verify(view).close()
    }
}

inline fun <reified RESULT : Any, FAILURE, reified VIEW : InitializationBehavior.View<*, FAILURE, *>> testDisplaysNotInitializedWhenInitializationFails(
        failure: FAILURE,
        isSuccess: (RESULT) -> Boolean,
        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {

    val result = mock<RESULT>()
    whenever(isSuccess(result)).thenReturn(false)
    val presenter = createPresenter { Single.just(result) }
    val view = mock<VIEW>()

    presenter attach view

    inOrder(view) {
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).displayNotInitialized(failure)
        verify(view).close()
    }
}

//inline fun <reified RESULT : Any, SUCCESS, FAILURE, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testProceedsIfErrorIsResolved(
//        success: SUCCESS,
//        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>,
//        stub: (RESULT) -> Boolean
//) {
//
//    val throwable = Throwable()
//    val result = mock<RESULT>()
//    whenever(stub(result)).thenReturn(true)
//    val view = mock<VIEW>()
//    var value = -1
//    val single = Single.create<RESULT> {
//        if (value++ == -1) it.onError(throwable)
//        else it.onSuccess(result)
//    }
//    whenever(view.showResolvableError(any())).thenReturn(Single.just(ErrorResolution.POSITIVE))
//
//    val presenter = createPresenter({ single })
//
//    presenter attach view
//
//    inOrder(view) {
//        verify(view).showLoading()
//        verify(view).hideLoading()
//        verify(view).showResolvableError(any())
//        verify(view).showLoading()
//        verify(view).hideLoading()
//        verify(view).displayInitialized(success)
//        verify(view).close()
//    }
//}
//
//inline fun <reified RESULT : Any, SUCCESS, FAILURE, reified ERROR : Any, reified VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>> testShowsErrorIfErrorIsNotResolved(
//        error: ERROR,
//        createPresenter: (() -> Single<RESULT>) -> Presenter<VIEW>) {
//
//    val throwable = Throwable()
//    val view = mock<VIEW>()
//    whenever(view.showResolvableError(error)).thenReturn(Single.just(ErrorResolution.NEGATIVE))
//
//    val presenter = createPresenter({ Single.error(throwable) })
//
//    presenter attach view
//
//    inOrder(view) {
//        verify(view).showLoading()
//        verify(view).hideLoading()
//        verify(view).showResolvableError(error)
//        verify(view).showError(any())
//    }
//}