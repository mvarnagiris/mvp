package com.mvcoding.mvp.views

import com.mvcoding.mvp.O
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines

interface ErrorView<in ERROR> : Presenter.View {
    fun showError(error: ERROR)
}

fun <T, ERROR, VIEW : ErrorView<ERROR>> O<T>.showErrorAndComplete(view: VIEW, mapError: (Throwable) -> ERROR, schedulers: RxSchedulers = trampolines): O<T> =
        onErrorResumeNext { throwable: Throwable ->
            O.just(throwable).observeOn(schedulers.main).doOnNext { view.showError(mapError(it)) }.flatMap { (O.empty<T>()) }
        }

fun <T, VIEW : ErrorView<Throwable>> O<T>.showErrorAndComplete(view: VIEW, schedulers: RxSchedulers = trampolines): O<T> = showErrorAndComplete(view, { it }, schedulers)