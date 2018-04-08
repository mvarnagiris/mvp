package com.mvcoding.mvp.views

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test


class ResolvableErrorViewTest {

    @Test
    fun `resubscribes to same single if error is resolved`() {
        val throwable = Throwable()
        val view = mock<ResolvableErrorView<Throwable>>()
        var value = -1
        val single = Single.create<Int> {
            if (value++ == -1) it.onError(throwable)
            else it.onSuccess(value)
        }
        whenever(view.showResolvableError(throwable)).thenReturn(Single.just(ErrorResolution.POSITIVE))
        val observer = TestObserver.create<Int>()

        single.resolveErrorOrFail(view).subscribe(observer)

        verify(view).showResolvableError(throwable)
        observer.assertValue(1)
        observer.assertNoErrors()
        observer.assertComplete()
    }
}
