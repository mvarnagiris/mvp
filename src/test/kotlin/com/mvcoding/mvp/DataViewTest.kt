package com.mvcoding.mvp

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
}
