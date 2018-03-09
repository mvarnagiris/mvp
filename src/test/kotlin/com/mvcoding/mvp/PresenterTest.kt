package com.mvcoding.mvp

import com.memoizr.assertk.expect
import com.memoizr.assertk.isInstance
import com.memoizr.assertk.of
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.*
import org.junit.Before
import org.junit.Test
import javax.xml.stream.FactoryConfigurationError
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PresenterTest {
    private val view = mock<Presenter.View>()
    private val presenter: Presenter<Presenter.View> = object : Presenter<Presenter.View>() {}
    private val viewForTest = mock<ViewForTest>()

    @Before
    fun setUp() {
        whenever(viewForTest.observable()).thenReturn(Observable.never())
        whenever(viewForTest.observableOnNext()).thenReturn(Observable.never())
        whenever(viewForTest.observableOnNextOnError()).thenReturn(Observable.never())
        whenever(viewForTest.observableOnNextOnErrorOnComplete()).thenReturn(Observable.never())
        whenever(viewForTest.flowable()).thenReturn(Flowable.never())
        whenever(viewForTest.flowableOnNext()).thenReturn(Flowable.never())
        whenever(viewForTest.flowableOnNextOnError()).thenReturn(Flowable.never())
        whenever(viewForTest.flowableOnNextOnErrorOnComplete()).thenReturn(Flowable.never())
        whenever(viewForTest.single()).thenReturn(Single.never())
        whenever(viewForTest.singleOnSuccess()).thenReturn(Single.never())
        whenever(viewForTest.singleOnSuccessOnError()).thenReturn(Single.never())
        whenever(viewForTest.maybe()).thenReturn(Maybe.never())
        whenever(viewForTest.maybeOnSuccess()).thenReturn(Maybe.never())
        whenever(viewForTest.maybeOnSuccessOnError()).thenReturn(Maybe.never())
        whenever(viewForTest.maybeOnSuccessOnErrorOnComplete()).thenReturn(Maybe.never())
    }

    @Test
    fun `throws error when view is already attached`() {
        expect thatThrownBy {
            presenter attach view
            presenter attach view
        } hasMessageContaining "already attached" isInstance of<IllegalStateException>()
    }

    @Test
    fun `throws error when view was not attached`() {
        expect thatThrownBy {
            presenter detach view
        } hasMessageContaining "View is already detached." isInstance of<IllegalStateException>()
    }

    @Test
    fun `throws error when view was already detached`() {
        expect thatThrownBy {
            presenter attach view
            presenter detach view
            presenter detach view
        } hasMessageContaining "View is already detached." isInstance of<IllegalStateException>()
    }

    @Test
    fun `throws error when trying to detach different view`() {
        expect thatThrownBy {
            presenter attach view
            presenter detach mock()
        } hasMessageContaining "Trying to detach different view." isInstance of<IllegalStateException>()
    }

    @Test
    fun `subscribe until detach disposes observable after detach`() {
        val presenter = PresenterForTest()

        var observableIsDisposed = true
        var observableOnNextIsDisposed = true
        var observableOnNextOnErrorIsDisposed = true
        var observableOnNextOnErrorOnCompleteIsDisposed = true

        val observable = Observable.create<Unit> { }.doOnSubscribe { observableIsDisposed = false }.doFinally { observableIsDisposed = true }
        val observableOnNext = Observable.create<Unit> { }.doOnSubscribe { observableOnNextIsDisposed = false }.doFinally { observableOnNextIsDisposed = true }
        val observableOnNextOnError = Observable.create<Unit> { }.doOnSubscribe { observableOnNextOnErrorIsDisposed = false }.doFinally { observableOnNextOnErrorIsDisposed = true }
        val observableOnNextOnErrorOnComplete = Observable.create<Unit> { }.doOnSubscribe { observableOnNextOnErrorOnCompleteIsDisposed = false }.doFinally { observableOnNextOnErrorOnCompleteIsDisposed = true }

        whenever(viewForTest.observable()).thenReturn(observable)
        whenever(viewForTest.observableOnNext()).thenReturn(observableOnNext)
        whenever(viewForTest.observableOnNextOnError()).thenReturn(observableOnNextOnError)
        whenever(viewForTest.observableOnNextOnErrorOnComplete()).thenReturn(observableOnNextOnErrorOnComplete)

        presenter attach viewForTest
        assertFalse { observableIsDisposed }
        assertFalse { observableOnNextIsDisposed }
        assertFalse { observableOnNextOnErrorIsDisposed }
        assertFalse { observableOnNextOnErrorOnCompleteIsDisposed }

        presenter detach viewForTest
        assertTrue { observableIsDisposed }
        assertTrue { observableOnNextIsDisposed }
        assertTrue { observableOnNextOnErrorIsDisposed }
        assertTrue { observableOnNextOnErrorOnCompleteIsDisposed }
    }

    @Test
    fun `subscribe until detach disposes flowable after detach`() {
        val presenter = PresenterForTest()

        var flowableIsDisposed = true
        var flowableOnNextIsDisposed = true
        var flowableOnNextOnErrorIsDisposed = true
        var flowableOnNextOnErrorOnCompleteIsDisposed = true

        val flowable = Flowable.create<Unit>({}, BackpressureStrategy.DROP).doOnSubscribe { flowableIsDisposed = false }.doFinally { flowableIsDisposed = true }
        val flowableOnNext = Flowable.create<Unit>({}, BackpressureStrategy.DROP).doOnSubscribe { flowableOnNextIsDisposed = false }.doFinally { flowableOnNextIsDisposed = true }
        val flowableOnNextOnError = Flowable.create<Unit>({}, BackpressureStrategy.DROP).doOnSubscribe { flowableOnNextOnErrorIsDisposed = false }.doFinally { flowableOnNextOnErrorIsDisposed = true }
        val flowableOnNextOnErrorOnComplete = Flowable.create<Unit>({}, BackpressureStrategy.DROP).doOnSubscribe { flowableOnNextOnErrorOnCompleteIsDisposed = false }.doFinally { flowableOnNextOnErrorOnCompleteIsDisposed = true }

        whenever(viewForTest.flowable()).thenReturn(flowable)
        whenever(viewForTest.flowableOnNext()).thenReturn(flowableOnNext)
        whenever(viewForTest.flowableOnNextOnError()).thenReturn(flowableOnNextOnError)
        whenever(viewForTest.flowableOnNextOnErrorOnComplete()).thenReturn(flowableOnNextOnErrorOnComplete)

        presenter attach viewForTest
        assertFalse { flowableIsDisposed }
        assertFalse { flowableOnNextIsDisposed }
        assertFalse { flowableOnNextOnErrorIsDisposed }
        assertFalse { flowableOnNextOnErrorOnCompleteIsDisposed }

        presenter detach viewForTest
        assertTrue { flowableIsDisposed }
        assertTrue { flowableOnNextIsDisposed }
        assertTrue { flowableOnNextOnErrorIsDisposed }
        assertTrue { flowableOnNextOnErrorOnCompleteIsDisposed }
    }

    @Test
    fun `subscribe until detach disposes single after detach`() {
        val presenter = PresenterForTest()

        var singleIsDisposed = true
        var singleOnNextIsDisposed = true
        var singleOnNextOnErrorIsDisposed = true

        val single = Single.create<Unit> { }.doOnSubscribe { singleIsDisposed = false }.doFinally { singleIsDisposed = true }
        val singleOnNext = Single.create<Unit> { }.doOnSubscribe { singleOnNextIsDisposed = false }.doFinally { singleOnNextIsDisposed = true }
        val singleOnNextOnError = Single.create<Unit> { }.doOnSubscribe { singleOnNextOnErrorIsDisposed = false }.doFinally { singleOnNextOnErrorIsDisposed = true }

        whenever(viewForTest.single()).thenReturn(single)
        whenever(viewForTest.singleOnSuccess()).thenReturn(singleOnNext)
        whenever(viewForTest.singleOnSuccessOnError()).thenReturn(singleOnNextOnError)

        presenter attach viewForTest
        assertFalse { singleIsDisposed }
        assertFalse { singleOnNextIsDisposed }
        assertFalse { singleOnNextOnErrorIsDisposed }

        presenter detach viewForTest
        assertTrue { singleIsDisposed }
        assertTrue { singleOnNextIsDisposed }
        assertTrue { singleOnNextOnErrorIsDisposed }
    }

    @Test
    fun `subscribe until detach disposes maybe after detach`() {
        val presenter = PresenterForTest()

        var maybeIsDisposed = true
        var maybeOnNextIsDisposed = true
        var maybeOnNextOnErrorIsDisposed = true
        var maybeOnNextOnErrorOnCompleteIsDisposed = true

        val maybe = Maybe.create<Unit> { }.doOnSubscribe { maybeIsDisposed = false }.doFinally { maybeIsDisposed = true }
        val maybeOnNext = Maybe.create<Unit> { }.doOnSubscribe { maybeOnNextIsDisposed = false }.doFinally { maybeOnNextIsDisposed = true }
        val maybeOnNextOnError = Maybe.create<Unit> { }.doOnSubscribe { maybeOnNextOnErrorIsDisposed = false }.doFinally { maybeOnNextOnErrorIsDisposed = true }
        val maybeOnNextOnErrorOnComplete = Maybe.create<Unit> { }.doOnSubscribe { maybeOnNextOnErrorOnCompleteIsDisposed = false }.doFinally { maybeOnNextOnErrorOnCompleteIsDisposed = true }

        whenever(viewForTest.maybe()).thenReturn(maybe)
        whenever(viewForTest.maybeOnSuccess()).thenReturn(maybeOnNext)
        whenever(viewForTest.maybeOnSuccessOnError()).thenReturn(maybeOnNextOnError)
        whenever(viewForTest.maybeOnSuccessOnErrorOnComplete()).thenReturn(maybeOnNextOnErrorOnComplete)

        presenter attach viewForTest
        assertFalse { maybeIsDisposed }
        assertFalse { maybeOnNextIsDisposed }
        assertFalse { maybeOnNextOnErrorIsDisposed }
        assertFalse { maybeOnNextOnErrorOnCompleteIsDisposed }

        presenter detach viewForTest
        assertTrue { maybeIsDisposed }
        assertTrue { maybeOnNextIsDisposed }
        assertTrue { maybeOnNextOnErrorIsDisposed }
        assertTrue { maybeOnNextOnErrorOnCompleteIsDisposed }
    }

    @Test
    fun `when presenter is attached-detached it attaches-detaches all behaviors`() {
        val behavior1 = BehaviorForTest()
        val behavior2 = BehaviorForTest()
        val presenter = PresenterForTest(behavior1, behavior2)

        presenter attach viewForTest
        behavior1.isAttached = true
        behavior2.isAttached = true

        presenter detach viewForTest
        behavior1.isAttached = false
        behavior2.isAttached = false
    }

    interface ViewForTest : Presenter.View {
        fun observable(): Observable<Unit>
        fun observableOnNext(): Observable<Unit>
        fun observableOnNextOnError(): Observable<Unit>
        fun observableOnNextOnErrorOnComplete(): Observable<Unit>
        fun flowable(): Flowable<Unit>
        fun flowableOnNext(): Flowable<Unit>
        fun flowableOnNextOnError(): Flowable<Unit>
        fun flowableOnNextOnErrorOnComplete(): Flowable<Unit>
        fun single(): Single<Unit>
        fun singleOnSuccess(): Single<Unit>
        fun singleOnSuccessOnError(): Single<Unit>
        fun maybe(): Maybe<Unit>
        fun maybeOnSuccess(): Maybe<Unit>
        fun maybeOnSuccessOnError(): Maybe<Unit>
        fun maybeOnSuccessOnErrorOnComplete(): Maybe<Unit>
    }

    class PresenterForTest(vararg behavior: Behavior<ViewForTest>) : Presenter<ViewForTest>(*behavior) {
        override fun onViewAttached(view: ViewForTest) {
            super.onViewAttached(view)
            view.observable().subscribeUntilDetached()
            view.observableOnNext().subscribeUntilDetached()
            view.observableOnNextOnError().subscribeUntilDetached()
            view.observableOnNextOnErrorOnComplete().subscribeUntilDetached()
            view.flowable().subscribeUntilDetached()
            view.flowableOnNext().subscribeUntilDetached()
            view.flowableOnNextOnError().subscribeUntilDetached()
            view.flowableOnNextOnErrorOnComplete().subscribeUntilDetached()
            view.single().subscribeUntilDetached()
            view.singleOnSuccess().subscribeUntilDetached()
            view.singleOnSuccessOnError().subscribeUntilDetached()
            view.maybe().subscribeUntilDetached()
            view.maybeOnSuccess().subscribeUntilDetached()
            view.maybeOnSuccessOnError().subscribeUntilDetached()
            view.maybeOnSuccessOnErrorOnComplete().subscribeUntilDetached()
        }
    }

    class BehaviorForTest : Behavior<ViewForTest>() {
        var isAttached = false

        override fun onViewAttached(view: ViewForTest) {
            super.onViewAttached(view)
            isAttached = true
        }

        override fun onViewDetached(view: ViewForTest) {
            super.onViewDetached(view)
            isAttached = false
        }
    }
}