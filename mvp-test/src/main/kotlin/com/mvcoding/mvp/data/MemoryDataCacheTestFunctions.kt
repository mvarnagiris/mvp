package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataSource
import com.mvcoding.mvp.DataWriter
import io.reactivex.observers.TestObserver

fun <DATA, CACHE> testMemoryDataCache(data: DATA, createDataCache: (DATA?) -> CACHE) where CACHE : DataSource<DATA>, CACHE : DataWriter<DATA> {
    testDoesNotEmitAnythingInitiallyIfInitialDataWasNotProvided(createDataCache)
    testEmitsInitialValueIfItWasProvided(data, createDataCache)
    testEmitsLastValueThatWasWrittenBeforeSubscriptions(data, createDataCache)
    testEmitsLastValueThatWasWrittenAfterSubscriptions(data, createDataCache)
}

fun <DATA, CACHE> testMemoryDataCacheWithDefaultValue(defaultValue: DATA, data: DATA, createDataCache: (DATA?) -> CACHE) where CACHE : DataSource<DATA>, CACHE : DataWriter<DATA> {
    testEmitsInitialValueIfItWasProvided(defaultValue, createDataCache)
    testEmitsLastValueThatWasWrittenBeforeSubscriptions(data, createDataCache)
    testEmitsLastValueThatWasWrittenAfterSubscriptions(data, createDataCache)
}

internal fun <DATA, CACHE> testDoesNotEmitAnythingInitiallyIfInitialDataWasNotProvided(createDataCache: (DATA?) -> CACHE) where CACHE : DataSource<DATA>, CACHE : DataWriter<DATA> {
    val observer = TestObserver.create<DATA>()
    val dataCache = createDataCache(null)

    dataCache.data().subscribe(observer)

    observer.assertNoValues()
}

internal fun <DATA, CACHE> testEmitsInitialValueIfItWasProvided(initialData: DATA, createDataCache: (DATA?) -> CACHE) where CACHE : DataSource<DATA>, CACHE : DataWriter<DATA> {
    val observer = TestObserver.create<DATA>()
    val dataCache = createDataCache(initialData)

    dataCache.data().subscribe(observer)

    observer.assertValue(initialData)
}

internal fun <DATA, CACHE> testEmitsLastValueThatWasWrittenBeforeSubscriptions(data: DATA, createDataCache: (DATA?) -> CACHE) where CACHE : DataSource<DATA>, CACHE : DataWriter<DATA> {
    val observer = TestObserver.create<DATA>()
    val dataCache = createDataCache(null)

    dataCache.write(data)
    dataCache.data().subscribe(observer)

    observer.assertValue(data)
}

internal fun <DATA, CACHE> testEmitsLastValueThatWasWrittenAfterSubscriptions(data: DATA, createDataCache: (DATA?) -> CACHE) where CACHE : DataSource<DATA>, CACHE : DataWriter<DATA> {
    val observer1 = TestObserver.create<DATA>()
    val observer2 = TestObserver.create<DATA>()
    val dataCache = createDataCache(null)

    dataCache.data().subscribe(observer1)
    dataCache.data().subscribe(observer2)
    dataCache.write(data)

    observer1.assertValue(data)
    observer2.assertValue(data)
}