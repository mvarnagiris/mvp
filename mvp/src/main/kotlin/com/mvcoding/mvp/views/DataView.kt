package com.mvcoding.mvp.views

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines
import io.reactivex.Observable


interface DataView<in T> : Presenter.View {
    fun showData(data: T)
}

fun <INPUT, DATA, VIEW : DataView<DATA>> Observable<INPUT>.loadData(
        view: VIEW,
        dataSource: (INPUT) -> Observable<DATA>,
        schedulers: RxSchedulers = trampolines): Observable<DATA> =
        observeOn(schedulers.io)
                .switchMap { dataSource(it) }
                .observeOn(schedulers.main)
                .doOnNext { view.showData(it) }
