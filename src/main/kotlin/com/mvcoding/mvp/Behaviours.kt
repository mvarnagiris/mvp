package com.mvcoding.mvp

import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers.trampoline

typealias O<T> = Observable<T>

fun <INPUT, DATA, VIEW : DataView<DATA>> O<INPUT>.loadData(view: VIEW,
                                                           dataSource: DataSource<INPUT, DATA>,
                                                           schedulers: RxSchedulers = trampolines): O<DATA> =
        switchMap { dataSource.data(it).subscribeOn(schedulers.io) }
                .observeOn(schedulers.main)
                .doOnNext { view.showData(it) }

fun <T, VIEW : ErrorView> O<T>.recoverFromErrors(view: VIEW): O<T> =
        this.doOnError { view.showError() }
                .onErrorResumeNext(Observable.empty<T>())

fun <T, VIEW : LoadingView> O<T>.showHideLoading(view: VIEW): O<T> =
        just(Unit).doOnNext { view.showLoading() }
                .switchMap { this }
                .doOnNext { view.hideLoading() }
                .doOnError { view.hideLoading() }

fun <T, VIEW : RefreshableView> O<T>.refreshable(view: VIEW, schedulers: RxSchedulers = trampolines): O<T> =
        view.refreshes()
                .subscribeOn(schedulers.main)
                .startWith(Unit)
                .switchMap { this }

private val trampolines = RxSchedulers(trampoline(), trampoline(), trampoline())

data class RxSchedulers(val io: Scheduler, val main: Scheduler, val computation: Scheduler)
