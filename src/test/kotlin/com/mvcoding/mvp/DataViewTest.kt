package com.mvcoding.mvp

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import ro.kreator.aRandom


class DataViewTest {

    val view = mock<ViewForTest>()
    val dataSource = mock<DataSource<String, String>>()
    val input by aRandom<String>()
    val data by aRandom<String>()

    interface ViewForTest : DataView<String>

    @Before
    fun setUp() {
        whenever(dataSource.data(any())).thenReturn(Observable.just(data))
    }

    @Test
    fun `loadData loads and shows data`() {
        Observable.just(input).loadData(view, dataSource).subscribe()

        verify(dataSource).data(input)
        verify(view).showData(data)
    }
}
