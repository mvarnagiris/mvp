package com.mvcoding.mvp

import io.reactivex.Observable

interface RefreshableView {
    fun refreshes(): Observable<Unit>
}

fun <T, VIEW : RefreshableView> O<T>.refreshable(view: VIEW, schedulers: RxSchedulers = trampolines): O<T> =
        view.refreshes()
                .subscribeOn(schedulers.main)
                .startWith(Unit)
                .switchMap { this }