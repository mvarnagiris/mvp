package com.mvcoding.mvp

import io.reactivex.Observable
import io.reactivex.Single

interface DataSource<DATA> {
    fun data(): Observable<DATA>
}

fun <DATA> DataSource<DATA>.observableFunction(): () -> Observable<DATA> = { data() }
fun <DATA> DataSource<DATA>.singleFunction(): () -> Single<DATA> = { data().firstOrError() }
