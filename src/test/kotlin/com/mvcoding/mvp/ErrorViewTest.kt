package com.mvcoding.mvp

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import ro.kreator.aRandom


class ErrorViewTest {

    val view = mock<ViewForTest>()
    val dataSource = mock<DataSource<String, String>>()
    val input by aRandom<String>()
    val data by aRandom<String>()

    val refreshSubject = PublishSubject.create<Unit>()

    interface ViewForTest : DataView<String>, RefreshableView, ErrorView

    @Before
    fun setUp() {
        whenever(dataSource.data(any())).thenReturn(Observable.just(data))
        whenever(view.refreshes()).thenReturn(refreshSubject)
    }

    @Test
    fun `recoverFromError recoversFromErrors`() {
        whenever(dataSource.data(any())).thenReturn(Observable.error(Throwable()))

        Observable.just(input)
                .loadData(view, dataSource)
                .recoverFromErrors(view)
                .refreshable(view)
                .subscribe()

        verify(dataSource).data(input)
        verify(view, never()).showData(data)
        verify(view).showError()

        whenever(dataSource.data(any())).thenReturn(Observable.just(data))

        refreshSubject.onNext(Unit)

        verify(dataSource, times(2)).data(input)
        verify(view).showData(data)
    }
}
