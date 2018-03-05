package com.mvcoding.mvp

import io.reactivex.Observable

interface DataSource<in INPUT, DATA> {
    fun data(input: INPUT): Observable<DATA>
}

interface DataView<in T> : Presenter.View {
    fun showData(data: T)
}

interface LoadingView : Presenter.View {
    fun showLoading()
    fun hideLoading()
}

interface RefreshableView {
    fun refreshes(): Observable<Unit>
}

interface ErrorView : Presenter.View {
    fun showError()
}
