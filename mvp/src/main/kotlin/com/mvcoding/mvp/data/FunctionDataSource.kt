package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataSource
import com.mvcoding.mvp.O
import io.reactivex.Observable

class FunctionDataSource<DATA>(private val dataSource: () -> O<DATA>) : DataSource<DATA> {
    override fun data(): Observable<DATA> = dataSource()
}

fun <DATA> (() -> O<DATA>).dataSource() = FunctionDataSource(this)
fun <DATA> O<DATA>.dataSource() = FunctionDataSource { this }