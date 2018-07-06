package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.PublishRelay
import com.mvcoding.mvp.DataSource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.observers.TestObserver

fun <DATA> testMemoryDataSource(data: DATA, createDataSource: (DataSource<DATA>) -> DataSource<DATA>) {
    testUpstreamDataSourceIsCalledOnlyOnceAndCachedDataIsReturnedToOtherObservers(data, createDataSource)
    testReturnsDataToLateObserversWhenInitialObserversAreDisposedBeforeDataIsDelivered(data, createDataSource)
}

internal fun <DATA> testUpstreamDataSourceIsCalledOnlyOnceAndCachedDataIsReturnedToOtherObservers(
        data: DATA,
        createDataSource: (DataSource<DATA>) -> DataSource<DATA>) {

    val initialObserver1 = TestObserver.create<DATA>()
    val initialObserver2 = TestObserver.create<DATA>()
    val otherObserver = TestObserver.create<DATA>()
    val relay = PublishRelay.create<DATA>()
    val upstreamDataSource = mock<DataSource<DATA>>()
    val dataSource = createDataSource(upstreamDataSource)
    whenever(upstreamDataSource.data()).thenReturn(relay)

    dataSource.data().subscribe(initialObserver1)
    dataSource.data().subscribe(initialObserver2)
    relay.accept(data)
    dataSource.data().subscribe(otherObserver)

    initialObserver1.assertValue(data)
    initialObserver2.assertValue(data)
    otherObserver.assertValue(data)
    verify(upstreamDataSource, times(1)).data()
}

internal fun <DATA> testReturnsDataToLateObserversWhenInitialObserversAreDisposedBeforeDataIsDelivered(
        data: DATA,
        createDataSource: (DataSource<DATA>) -> DataSource<DATA>) {

    val initialObserver = TestObserver.create<DATA>()
    val otherObserver = TestObserver.create<DATA>()
    val relay = PublishRelay.create<DATA>()
    val upstreamDataSource = mock<DataSource<DATA>>()
    val dataSource = createDataSource(upstreamDataSource)
    whenever(upstreamDataSource.data()).thenReturn(relay)

    dataSource.data().subscribe(initialObserver)
    initialObserver.dispose()
    relay.accept(data)
    dataSource.data().subscribe(otherObserver)

    initialObserver.assertNoValues()
    otherObserver.assertValue(data)
    verify(upstreamDataSource, times(1)).data()
}