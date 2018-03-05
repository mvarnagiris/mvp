package com.mvcoding.mvp

import io.reactivex.Observable

interface ErrorView<in ERROR> : Presenter.View {
    fun showError(error: ERROR)
}

fun <T, ERROR, VIEW : ErrorView<ERROR>> O<T>.showErrorAndComplete(view: VIEW, mapError: (Throwable) -> ERROR): O<T> =
        doOnError { view.showError(mapError(it)) }
                .onErrorResumeNext(Observable.empty<T>())