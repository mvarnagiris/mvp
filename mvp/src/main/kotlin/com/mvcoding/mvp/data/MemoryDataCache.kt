package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.BehaviorRelay
import com.mvcoding.mvp.DataCache
import com.mvcoding.mvp.O

class MemoryDataCache<DATA>(initialData: DATA? = null) : DataCache<DATA> {

    private val relay = initialData?.let { BehaviorRelay.createDefault(it) } ?: BehaviorRelay.create<DATA>()

    override fun write(data: DATA) = relay.accept(data)
    override fun data(): O<DATA> = relay
}