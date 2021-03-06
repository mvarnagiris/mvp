package com.mvcoding.mvp

import com.memoizr.assertk.expect
import com.mvcoding.mvp.views.DataView
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import org.mockito.Mockito
import ro.kreator.instantiateRandomClass
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType


val testSchedulers = trampolines.copy(
        io = RxJavaPlugins.createSingleScheduler { Thread(it, "test io") },
        computation = RxJavaPlugins.createSingleScheduler { Thread(it, "test computation") },
        main = RxJavaPlugins.createSingleScheduler { Thread(it, "test main") }
)

inline fun <reified T : Any> verify(mock: ThreadMock<T>): T = Mockito.verify(mock.mock, timeout(100))!!

inline fun <reified T : Any> T.mockWrapper(): MockWrapper {
    return this as MockWrapper
}


inline fun <reified T : Any> threadMock(): ThreadMock<T> {
    val mock = mock<T>()
    return ThreadMock(mock, T::class)
}

data class ThreadMock<T : Any>(val mock: T, val mockClass: KClass<T>) {

    private data class ThreadInvocations(val method: String, val threadName: String)
    operator fun invoke() = proxy
    private val threads = mutableListOf<ThreadInvocations>()

    val proxy = Proxy.newProxyInstance(mockClass.java.classLoader, arrayOf(mockClass.java, MockWrapper::class.java),
            { proxy, method, args ->
                val signature = "${method.name}(${method.parameters.toList().joinToString(",")})"

                if (method.name == MockWrapper::threadMock.name) {
                    this
                } else {
                    val find = mock::class.java.methods.find { it.name == method.name }
                    if (args == null) {
                        val invoke = find?.invoke(mock)

                        if (find != null && find?.returnType.name == "void" || invoke != null) {
                            threads += ThreadInvocations(signature, Thread.currentThread().name)
                        }

                        invoke
                    } else {
                        val invoke = find?.invoke(mock, *args)
                        if (find != null && find?.returnType.name == "void" || invoke != null) {
                            threads += ThreadInvocations(signature, Thread.currentThread().name)
                        }
                        invoke
                    }
                }
            }) as T

    fun verifyAllInteractionsOn(scheduler: Scheduler) {
        val latch = CountDownLatch(1)

        var t: Throwable? = null
        scheduler.scheduleDirect {
            try {
                expect that threads.all { it.threadName == Thread.currentThread().name } describedAs "Expected all invocations to occur on ${Thread.currentThread().name}, but some invocations occurred on a different thread :${threads}" _is true
            } catch (e: Throwable) {
                t = e
            } finally {
                latch.countDown()
            }

        }
        latch.await()
        t?.run { throw this }
    }
}

interface MockWrapper {
    fun threadMock(): ThreadMock<*>
}

class LoadDataBehaviourAssertion<T, V : DataView<T>>(val presenter: Presenter<V>, val dataSource: DataSource<T>) {
    operator fun getValue(any: Any, kProperty: KProperty<*>): BehAssertion<T, V> {
        val dataType: KType = kProperty.returnType.arguments.first().type!!
        return BehAssertion(presenter, dataSource, dataType)
    }

    class BehAssertion<T, V : DataView<T>>(val presenter: Presenter<V>, val dataSource: DataSource<T>, dataType: KType) {
        private val value = instantiateRandomClass(dataType) as T

        fun verifyLoadDataBehavior() {
            val viewMock = threadMock<DataView<T>>()

            whenever(dataSource.data()).thenReturn(Observable.just(value))

            presenter attach viewMock() as V

            verify(viewMock).showData(value)

            viewMock.verifyAllInteractionsOn(testSchedulers.main)
            dataSource.mockWrapper().threadMock().verifyAllInteractionsOn(testSchedulers.io)
        }
    }
}

