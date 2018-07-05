package com.mvcoding.mvp.data

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

