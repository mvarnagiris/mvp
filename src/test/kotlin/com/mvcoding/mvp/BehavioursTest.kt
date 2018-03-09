package com.mvcoding.mvp

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import ro.kreator.aRandom


class BehavioursTest {

    val view = mock<ViewForTest>()
    val dataSource = mock<(String) -> O<String>>()
    val input by aRandom<String>()
    val data by aRandom<String>()
    val mapError = { error: Throwable -> error}

    val refreshSubject = PublishSubject.create<Unit>()

    interface ViewForTest : DataView<String>, LoadingView, RefreshableView, ErrorView<Throwable>

    @Before
    fun setUp() {
        whenever(dataSource(any())).thenReturn(Observable.just(data))
        whenever(view.refreshes()).thenReturn(refreshSubject)
    }

    @Test
    fun `integration`() {
        Observable.just(input)
                .loadData(view, dataSource)
                .showHideLoading(view)
                .showErrorAndComplete(view, mapError)
                .refreshable(view)
                .subscribe()

        val throwable = Throwable()
        whenever(dataSource(any())).thenReturn(Observable.error(throwable))

        refreshSubject.onNext(Unit)

        whenever(dataSource(any())).thenReturn(Observable.just(data))

        refreshSubject.onNext(Unit)

        inOrder(view) {
            verify(view).showLoading()
            verify(view).showData(data)
            verify(view).hideLoading()

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(view).showError(throwable)

            verify(view).showLoading()
            verify(view).showData(data)
            verify(view).hideLoading()
        }
    }
}
