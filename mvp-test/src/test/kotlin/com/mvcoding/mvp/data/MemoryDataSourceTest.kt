package com.mvcoding.mvp.data

import org.junit.Test

class MemoryDataSourceTest {

    @Test
    fun `upstream DataSource is called only once and cached data is returned to other observers`() {
        testUpstreamDataSourceIsCalledOnlyOnceAndCachedDataIsReturnedToOtherObservers(1) { it.memoryDataSource() }
    }

    @Test
    fun `returns data to late observers when initial observers are disposed before data is delivered`() {
        testReturnsDataToLateObserversWhenInitialObserversAreDisposedBeforeDataIsDelivered(1) { it.memoryDataSource() }
    }
}