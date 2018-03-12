package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataSource
import com.mvcoding.mvp.O

class FunctionDataSource<DATA>(private val dataSource: () -> O<DATA>) : DataSource<DATA> {
    override fun data(): O<DATA> = dataSource()
}

fun <DATA> (() -> O<DATA>).dataSource() = FunctionDataSource(this)