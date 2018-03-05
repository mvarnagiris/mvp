package com.mvcoding.mvp


interface DataView<in T> : Presenter.View {
    fun showData(data: T)
}

fun <INPUT, DATA, VIEW : DataView<DATA>> O<INPUT>.loadData(view: VIEW,
                                                           dataSource: (INPUT) -> O<DATA>,
                                                           schedulers: RxSchedulers = trampolines): O<DATA> =
        switchMap { dataSource(it).subscribeOn(schedulers.io) }
                .observeOn(schedulers.main)
                .doOnNext { view.showData(it) }