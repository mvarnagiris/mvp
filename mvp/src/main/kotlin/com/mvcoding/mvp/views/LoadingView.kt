package com.mvcoding.mvp.views

import com.mvcoding.mvp.O
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines

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