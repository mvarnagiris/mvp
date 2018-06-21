package com.mvcoding.mvp.views

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines
import io.reactivex.Observable
import io.reactivex.Single

interface LoadingView : Presenter.View {
    fun showLoading()
    fun hideLoading()
}

fun <T, VIEW : LoadingView> Observable<T>.showHideLoading(view: VIEW, schedulers: RxSchedulers = trampolines): Observable<T> =
        Observable.just(Unit)
                .observeOn(schedulers.main)
                .doOnNext { view.showLoading() }
                .switchMap { this }
                .observeOn(schedulers.main)
                .doOnNext { view.hideLoading() }
                .doOnError { view.hideLoading() }

fun <T, VIEW : LoadingView> Single<T>.showHideLoading(view: VIEW, schedulers: RxSchedulers = trampolines): Single<T> =
        Single.just(Unit)
                .observeOn(schedulers.main)
                .doOnSuccess { view.showLoading() }
                .flatMap { this }
                .observeOn(schedulers.main)
                .doOnSuccess { view.hideLoading() }
                .doOnError { view.hideLoading() }