package com.mvcoding.mvp.views

import com.mvcoding.mvp.*

interface LoadingView : Presenter.View {
    fun showLoading()
    fun hideLoading()
}

fun <T, VIEW : LoadingView> O<T>.showHideLoading(view: VIEW, schedulers: RxSchedulers = trampolines): O<T> =
        O.just(Unit)
                .observeOn(schedulers.main)
                .doOnNext { view.showLoading() }
                .switchMap { this }
                .observeOn(schedulers.main)
                .doOnNext { view.hideLoading() }
                .doOnError { view.hideLoading() }

fun <T, VIEW : LoadingView> S<T>.showHideLoading(view: VIEW, schedulers: RxSchedulers = trampolines): S<T> =
        S.just(Unit)
                .observeOn(schedulers.main)
                .doOnSuccess { view.showLoading() }
                .flatMap { this }
                .observeOn(schedulers.main)
                .doOnSuccess { view.hideLoading() }
                .doOnError { view.hideLoading() }