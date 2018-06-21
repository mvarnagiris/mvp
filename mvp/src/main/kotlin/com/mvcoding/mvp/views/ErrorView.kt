package com.mvcoding.mvp.views

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines
import io.reactivex.Observable

interface ErrorView<in ERROR> : Presenter.View {
    fun showError(error: ERROR)
}

fun <T, ERROR, VIEW : ErrorView<ERROR>> Observable<T>.showErrorAndComplete(view: VIEW, mapError: (Throwable) -> ERROR, schedulers: RxSchedulers = trampolines): Observable<T> =
        onErrorResumeNext { throwable: Throwable ->
            Observable.just(throwable).observeOn(schedulers.main).doOnNext { view.showError(mapError(it)) }.flatMap { (Observable.empty<T>()) }
        }

fun <T, VIEW : ErrorView<Throwable>> Observable<T>.showErrorAndComplete(view: VIEW, schedulers: RxSchedulers = trampolines): Observable<T> = showErrorAndComplete(view, { it }, schedulers)