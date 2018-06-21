package com.mvcoding.mvp.views

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Test


class LoadingViewTest {

    @Test
    fun `showHideLoading shows loading before observable is called and hides loading when observable emits value`() {
        val view = mock<LoadingView>()
        val publishSubject = PublishSubject.create<Int>()
        val observer = TestObserver.create<Int>()

        publishSubject.showHideLoading(view).subscribe(observer)
        verify(view).showLoading()
        verifyNoMoreInteractions(view)

        publishSubject.onNext(1)
        verify(view).hideLoading()
    }

    @Test
    fun `showHideLoading hides loading when observable returns error`() {
        val view = mock<LoadingView>()
        val observer = TestObserver.create<Int>()

        Observable.error<Int>(Throwable()).showHideLoading(view).subscribe(observer)
        inOrder(view) {
            verify(view).showLoading()
            verify(view).hideLoading()
        }
    }
}
