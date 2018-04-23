package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataSource
import com.mvcoding.mvp.O

class MemoryDataSource<DATA>(dataSource: DataSource<DATA>) : DataSource<DATA> {
    constructor(dataSource: () -> O<DATA>) : this(dataSource.dataSource())

    private val observable by lazy { dataSource.data().replay(1).autoConnect() }

    override fun data(): O<DATA> = observable
}

fun <DATA> DataSource<DATA>.memoryDataSource() = MemoryDataSource(this)
fun <DATA> (() -> O<DATA>).memoryDataSource() = MemoryDataSource(this)
fun <DATA> O<DATA>.memoryDataSource() = this.dataSource().memoryDataSource()