package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataSource
import io.reactivex.Observable

class MemoryDataSource<DATA>(dataSource: DataSource<DATA>) : DataSource<DATA> {
    constructor(dataSource: () -> Observable<DATA>) : this(dataSource.dataSource())

    private val observable by lazy { dataSource.data().replay(1).autoConnect() }

    override fun data(): Observable<DATA> = observable
}

fun <DATA> DataSource<DATA>.memoryDataSource() = MemoryDataSource(this)
fun <DATA> (() -> Observable<DATA>).memoryDataSource() = MemoryDataSource(this)
fun <DATA> Observable<DATA>.memoryDataSource() = this.dataSource().memoryDataSource()