package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataSource
import io.reactivex.Observable

class FunctionDataSource<DATA>(private val dataSource: () -> Observable<DATA>) : DataSource<DATA> {
    override fun data(): Observable<DATA> = dataSource()
}

fun <DATA> (() -> Observable<DATA>).dataSource() = FunctionDataSource(this)
fun <DATA> Observable<DATA>.dataSource() = FunctionDataSource { this }