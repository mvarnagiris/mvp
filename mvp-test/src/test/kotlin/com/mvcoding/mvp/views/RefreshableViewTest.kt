package com.mvcoding.mvp.views

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Test


class RefreshableViewTest {

    @Test
    fun `refreshable resubscribes to observable again when view initiates a refresh`() {
        val view = mock<RefreshableView>()
        val refreshSubject = PublishSubject.create<Unit>()
        whenever(view.refreshes()).thenReturn(refreshSubject)
        val observable = Observable.just(1)
        val observer = TestObserver.create<Int>()

        observable.refreshable(view).subscribe(observer)
        refreshSubject.onNext(Unit)

        observer.assertValues(1, 1)
    }
}
