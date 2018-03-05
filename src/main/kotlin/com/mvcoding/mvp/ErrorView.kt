package com.mvcoding.mvp

import io.reactivex.Observable

interface ErrorView : Presenter.View {
    fun showError(throwable: Throwable)
}

fun <T, VIEW : ErrorView> O<T>.showErrorAndComplete(view: VIEW): O<T> =
        doOnError { view.showError(it) }
                .onErrorResumeNext(Observable.empty<T>())