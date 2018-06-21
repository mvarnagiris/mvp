package com.mvcoding.mvp.views

import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines
import io.reactivex.Observable

interface RefreshableView {
    fun refreshes(): Observable<Unit>
}

fun <T, VIEW : RefreshableView> Observable<T>.refreshable(view: VIEW, schedulers: RxSchedulers = trampolines): Observable<T> =
        view.refreshes()
                .subscribeOn(schedulers.main)
                .startWith(Unit)
                .switchMap { this }