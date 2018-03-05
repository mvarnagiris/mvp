package com.mvcoding.mvp

import io.reactivex.Observable

interface ErrorView : Presenter.View {
    fun showError()
}

fun <T, VIEW : ErrorView> O<T>.recoverFromErrors(view: VIEW): O<T> =
        doOnError { view.showError() }
                .onErrorResumeNext(Observable.empty<T>())