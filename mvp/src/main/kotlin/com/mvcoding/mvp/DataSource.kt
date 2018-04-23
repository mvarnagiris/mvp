package com.mvcoding.mvp

import io.reactivex.Observable

interface DataSource<DATA> {
    fun data(): Observable<DATA>
}

fun <DATA> DataSource<DATA>.function(): () -> O<DATA> = { data() }
