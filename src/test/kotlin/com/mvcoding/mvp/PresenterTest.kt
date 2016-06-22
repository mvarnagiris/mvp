package com.mvcoding.mvp

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import rx.Observable
import rx.Observable.just
import kotlin.test.assertFalse

class PresenterTest {
    val view = mock<Presenter.View>()
    val presenter: Presenter<Presenter.View> = object : Presenter<Presenter.View>() {
    }

    @Test(expected = IllegalStateException::class)
    fun throwsIllegalStateExceptionWhenViewIsAlreadyAttached() {
        presenter.attach(view)
        presenter.attach(view)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsIllegalStateExceptionWhenViewWasNotAttached() {
        presenter.detach(view)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsIllegalStateExceptionWhenViewWasAlreadyDetached() {
        presenter.attach(view)
        presenter.detach(view)
        presenter.detach(view)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsIllegalStateExceptionWhenTryingToDetachDifferentView() {
        presenter.attach(view)
        presenter.detach(mock<Presenter.View>())
    }

    @Test
    fun subscribeUntilDetachUnsubscribesAfterDetach() {
        val presenter = PresenterForTest()
        val view = mock<ViewForTest>()
        var isSubscribed = false
        val events = just(Unit).doOnSubscribe { isSubscribed = true }.doOnUnsubscribe { isSubscribed = false }
        whenever(view.events()).thenReturn(events)

        presenter.attach(view)
        presenter.detach(view)

        assertFalse { isSubscribed }
    }

    interface ViewForTest : Presenter.View {
        fun events(): Observable<Unit>
    }

    class PresenterForTest : Presenter<ViewForTest>() {
        override fun onViewAttached(view: ViewForTest) {
            super.onViewAttached(view)
            view.events().subscribeUntilDetached { }
        }
    }
}