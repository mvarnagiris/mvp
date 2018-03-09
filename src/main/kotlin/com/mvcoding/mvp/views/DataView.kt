package com.mvcoding.mvp.views

import com.mvcoding.mvp.O
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.trampolines


interface DataView<in T> : Presenter.View {
    fun showData(data: T)
}

fun <INPUT, DATA, VIEW : DataView<DATA>> O<INPUT>.loadData(view: VIEW,
                                                                                                   dataSource: (INPUT) -> O<DATA>,
                                                                                                   schedulers: RxSchedulers = trampolines): O<DATA> =
        switchMap { dataSource(it).subscribeOn(schedulers.io) }
                .observeOn(schedulers.main)
                .doOnNext { view.showData(it) }