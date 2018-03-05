package com.mvcoding.mvp

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import ro.kreator.aRandom


class BehavioursTest {

    val view = mock<ViewForTest>()
    val dataSource = mock<DataSource<String, String>>()
    val input by aRandom<String>()
    val data by aRandom<String>()

    val refreshSubject = PublishSubject.create<Unit>()

    interface ViewForTest : DataView<String>, LoadingView, RefreshableView, ErrorView

    @Before
    fun setUp() {
        whenever(dataSource.data(any())).thenReturn(Observable.just(data))
        whenever(view.refreshes()).thenReturn(refreshSubject)
    }

    @Test
    fun `loadData loads and shows data`() {
        Observable.just(input).loadData(view, dataSource).subscribe()

        verify(dataSource).data(input)
        verify(view).showData(data)
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

    @Test
    fun `refreshable refreshes data`() {
        Observable.just(input)
                .loadData(view, dataSource)
                .refreshable(view)
                .subscribe()

        verify(dataSource).data(input)
        verify(view).showData(data)

        refreshSubject.onNext(Unit)

        verify(dataSource, times(2)).data(input)
        verify(view, times(2)).showData(data)
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

    @Test
    fun `integration`() {
        Observable.just(input)
                .loadData(view, dataSource)
                .showHideLoading(view)
                .recoverFromErrors(view)
                .refreshable(view)
                .subscribe()

        whenever(dataSource.data(any())).thenReturn(Observable.error(Throwable()))

        refreshSubject.onNext(Unit)

        whenever(dataSource.data(any())).thenReturn(Observable.just(data))

        refreshSubject.onNext(Unit)

        inOrder(view) {
            verify(view).showLoading()
            verify(view).showData(data)
            verify(view).hideLoading()

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(view).showError()

            verify(view).showLoading()
            verify(view).showData(data)
            verify(view).hideLoading()
        }
    }
}
