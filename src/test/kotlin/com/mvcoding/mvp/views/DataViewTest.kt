package com.mvcoding.mvp.views

import com.mvcoding.mvp.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Test
import ro.kreator.instantiateRandomClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType


class DataViewTest {

    @Test
    fun `loadData shows data from observable`() {
        val view = mock<DataView<Int>>()
        val dataSource = mock<(Int) -> O<Int>>()
        whenever(dataSource(any())).thenReturn(O.just(2))
        Observable.just(1).loadData(view, dataSource).subscribe()

        verify(dataSource).invoke(1)
        verify(view).showData(2)
    }
}

val testSchedulers = trampolines.copy(
        io = RxJavaPlugins.createSingleScheduler { Thread(it, "test io") },
        computation = RxJavaPlugins.createSingleScheduler { Thread(it, "test computation") },
        main = RxJavaPlugins.createSingleScheduler { Thread(it, "test main") }
)

class DataTestAssertion {

    val dataSource = mock<DataSource<String>>()

    val presenter = PresenterForTest(dataSource, testSchedulers)
    val assertion by LoadDataBehaviourAssertion(presenter, dataSource)

    @Test
    fun `presenter has loadData behavior`() {
        assertion.verifyLoadDataBehavior()
//                .verifyAllViewInteractionOnScheduler(testSchedulers.io)
    }


    class PresenterForTest(val dataSource: DataSource<String>, val testSchedulers: RxSchedulers) : Presenter<DataView<String>>() {
        override fun onViewAttached(view: DataView<String>) {
            super.onViewAttached(view)
            Observable.just("").loadData(view, { dataSource.data() }, testSchedulers).subscribe()
        }
    }
}

class LoadDataBehaviourAssertion<T, V : DataView<T>>(val presenter: Presenter<V>, val dataSource: DataSource<T>) {
    operator fun getValue(any: Any, kProperty: KProperty<*>): BehAssertion<T, V> {
        val dataType: KType = kProperty.returnType.arguments.first().type!!
        return BehAssertion(presenter, dataSource, dataType)
    }

    class BehAssertion<T, V : DataView<T>>(val presenter: Presenter<V>, val dataSource: DataSource<T>, val dataType: KType) {

        lateinit var view: DataView<T>
        private val value = instantiateRandomClass(dataType) as T

        fun verifyLoadDataBehavior(): BehAssertion<T, V> {
            view = mock<DataView<T>>()

            whenever(dataSource.data()).thenReturn(O.just(value))

            presenter attach view as V

            verify(view).showData(value)

            return this
        }

//        fun verifyAllViewInteractionOnScheduler(scheduler: Scheduler) {
//            val latch = CountDownLatch(1)
//
//            var t: Throwable? = null
//            scheduler.scheduleDirect{
//                try {
//                    expect that threadName isEqualToIgnoringCase Thread.currentThread().name
//                } catch (e: Throwable) {
//                    t = e
//                } finally {
//                    latch.countDown()
//                }
//
//            }
//            latch.await()
//            t?.run { throw this }
//        }
    }
}

