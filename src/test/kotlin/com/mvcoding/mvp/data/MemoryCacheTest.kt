package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.mvcoding.mvp.O
import io.reactivex.observers.TestObserver
import org.junit.Test

class MemoryCacheTest {

    @Test
    fun `observer gets value emitted after subscription`() {
        PublishRelay.create<Int>()
                .observerGetsValueEmittedAfterSubscription(1) { (it as Relay<Int>).accept(1) }
//        val relay = PublishRelay.create<Int>()
//        val observer = TestObserver.create<Int>()
//        relay.withMemoryCache().subscribe(observer)
//
//        relay.accept(1)
//
//        observer.assertValue(1)
    }

    @Test
    fun `observer gets last value emitted before subscription`() {
        val relay = PublishRelay.create<Int>()
        val observer = TestObserver.create<Int>()
        val observable = relay.withMemoryCache()
        observable.subscribe().dispose()

        relay.accept(1)
        relay.accept(2)
        observable.subscribe(observer)

        observer.assertValue(2)
    }

    @Test
    fun `only new observers get last known value`() {
        val relay = PublishRelay.create<Int>()
        val observer1 = TestObserver.create<Int>()
        val observer2 = TestObserver.create<Int>()
        val observable = relay.withMemoryCache()

        observable.subscribe(observer1)
        relay.accept(1)
        relay.accept(2)
        observable.subscribe(observer2)

        observer1.assertValues(1, 2)
        observer2.assertValue(2)
    }
}

private inline fun <reified T> O<T>.observerGetsValueEmittedAfterSubscription(value: T,
                                                                              emitValue: (O<T>) -> Unit) {
    val observer = TestObserver.create<T>()
    this.subscribe(observer)

    emitValue(this)

    observer.assertValue(value)
}