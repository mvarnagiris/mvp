package com.mvcoding.mvp

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import ro.kreator.aRandom


class LoadingViewTest {

    val view = mock<ViewForTest>()
    val dataSource = mock<DataSource<String, String>>()
    val input by aRandom<String>()
    val data by aRandom<String>()

    interface ViewForTest : DataView<String>, LoadingView

    @Before
    fun setUp() {
        whenever(dataSource.data(any())).thenReturn(Observable.just(data))
    }

    @Test
    fun `showHideLoading shows loading before loading data and hides loading after`() {
        Observable.just(input)
                .loadData(view, dataSource)
                .showHideLoading(view)
                .subscribe()

        verify(dataSource).data(input)

        inOrder(view) {
            verify(view).showLoading()
            verify(view).showData(data)
            verify(view).hideLoading()
        }
    }
}
