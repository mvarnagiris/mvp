package com.mvcoding.mvp.views

import com.mvcoding.mvp.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test

class DataViewTest {

    @Test
    fun `loadData shows data from observable`() {
        val view = mock<DataView<Int>>()
        val dataSource = mock<(Int) -> O<Int>>()
        whenever(dataSource(any())).thenReturn(O.just(2))
        Observable.just(1).loadData(view, dataSource).subscribe()

        verify(dataSource).invoke(1)
        verify(view).showData(2)
    }

    val dataSource = threadMock<DataSource<List<String>>>()

    val presenter = PresenterForTest(dataSource(), testSchedulers)
    val assertion by LoadDataBehaviourAssertion(presenter, dataSource.proxy)

    @Test
    fun `presenter has loadData behavior`() {
        assertion.verifyLoadDataBehavior()
    }

    class PresenterForTest(val dataSource: DataSource<List<String>>, val testSchedulers: RxSchedulers) : Presenter<DataView<List<String>>>() {
        override fun onViewAttached(view: DataView<List<String>>) {
            super.onViewAttached(view)
            Observable.just("").loadData(view, {
                dataSource.data()
            }, testSchedulers).subscribe()
        }
    }
}
