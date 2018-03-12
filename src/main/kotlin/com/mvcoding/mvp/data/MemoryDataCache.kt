package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.BehaviorRelay
import com.mvcoding.mvp.DataCache
import com.mvcoding.mvp.DataSource
import com.mvcoding.mvp.O

class MemoryDataCache<DATA>(dataSource: DataSource<DATA> = O.never<DATA>().dataSource()) : DataCache<DATA> {

    private val relay = BehaviorRelay.create<DATA>()

    override fun write(data: DATA) = relay.accept(data)
    override fun data(): O<DATA> = relay.sta
}