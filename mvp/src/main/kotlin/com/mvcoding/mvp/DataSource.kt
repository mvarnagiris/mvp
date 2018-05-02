package com.mvcoding.mvp

import io.reactivex.Observable

interface DataSource<DATA> {
    fun data(): Observable<DATA>
}

fun <DATA> DataSource<DATA>.functionDataSource(): () -> O<DATA> = { data() }
