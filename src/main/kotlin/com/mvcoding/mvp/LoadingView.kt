package com.mvcoding.mvp

import io.reactivex.Observable

interface LoadingView : Presenter.View {
    fun showLoading()
    fun hideLoading()
}

fun <T, VIEW : LoadingView> O<T>.showHideLoading(view: VIEW): O<T> =
        Observable.just(Unit).doOnNext { view.showLoading() }
                .switchMap { this }
                .doOnNext { view.hideLoading() }
                .doOnError { view.hideLoading() }