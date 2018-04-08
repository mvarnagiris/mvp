package com.mvcoding.mvp.views

import com.mvcoding.mvp.O
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.observers.TestObserver
import org.junit.Test


class ErrorViewTest {

    @Test
    fun `showErrorAndComplete shows error and completes observable`() {
        val throwable = Throwable()
        val view = mock<ErrorView<Throwable>>()
        val observable = O.error<Int>(throwable)
        val observer = TestObserver.create<Int>()

        observable.showErrorAndComplete(view).subscribe(observer)

        verify(view).showError(throwable)
        observer.assertNoValues()
        observer.assertNoErrors()
        observer.assertComplete()
    }
}
