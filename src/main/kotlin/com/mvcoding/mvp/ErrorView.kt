package com.mvcoding.mvp

interface ErrorView<in ERROR> : Presenter.View {
    fun showError(error: ERROR)
}

fun <T, ERROR, VIEW : ErrorView<ERROR>> O<T>.showErrorAndComplete(view: VIEW, mapError: (Throwable) -> ERROR, schedulers: RxSchedulers = trampolines): O<T> =
        onErrorResumeNext { throwable: Throwable ->
            O.just(throwable).observeOn(schedulers.main).doOnNext { view.showError(mapError(it)) }.flatMap { (O.empty<T>()) }
        }