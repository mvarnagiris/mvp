package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataCache
import io.reactivex.observers.TestObserver
import org.junit.Test

class MemoryDataCacheTest {

    private val createDataCache: (Int?) -> MemoryDataCache<Int> = { MemoryDataCache(it) }

    @Test
    fun `does not emit anything initially if initial data was not provided`() {
        testDoesNotEmitAnythingInitiallyIfInitialDataWasNotProvided(createDataCache)
    }

    @Test
    fun `emits initial value if it was provided`() {
        testEmitsInitialValueIfItWasProvided(1, createDataCache)
    }

    @Test
    fun `emits last value that was written before subscriptions`() {
        testEmitsLastValueThatWasWrittenBeforeSubscriptions(1, createDataCache)
    }

    @Test
    fun `emits last value that was written after subscriptions`() {
        testEmitsLastValueThatWasWrittenAfterSubscriptions(1, createDataCache)
    }
}

private fun <DATA> testDoesNotEmitAnythingInitiallyIfInitialDataWasNotProvided(createDataCache: (DATA?) -> DataCache<DATA>) {
    val observer = TestObserver.create<DATA>()
    val dataCache = createDataCache(null)

    dataCache.data().subscribe(observer)

    observer.assertNoValues()
}

private fun <DATA> testEmitsInitialValueIfItWasProvided(initialData: DATA, createDataCache: (DATA?) -> DataCache<DATA>) {
    val observer = TestObserver.create<DATA>()
    val dataCache = createDataCache(initialData)

    dataCache.data().subscribe(observer)

    observer.assertValue(initialData)
}

private fun <DATA> testEmitsLastValueThatWasWrittenBeforeSubscriptions(data: DATA, createDataCache: (DATA?) -> DataCache<DATA>) {
    val observer = TestObserver.create<DATA>()
    val dataCache = createDataCache(null)

    dataCache.write(data)
    dataCache.data().subscribe(observer)

    observer.assertValue(data)
}

private fun <DATA> testEmitsLastValueThatWasWrittenAfterSubscriptions(data: DATA, createDataCache: (DATA?) -> DataCache<DATA>) {
    val observer1 = TestObserver.create<DATA>()
    val observer2 = TestObserver.create<DATA>()
    val dataCache = createDataCache(null)

    dataCache.data().subscribe(observer1)
    dataCache.data().subscribe(observer2)
    dataCache.write(data)

    observer1.assertValue(data)
    observer2.assertValue(data)
}