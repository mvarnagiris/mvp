package com.mvcoding.mvp

import io.reactivex.Observable
import io.reactivex.Single

interface SingleDataSource<DATA> {
    fun data(): Single<DATA>
}

fun <DATA> SingleDataSource<DATA>.singleFunction(): () -> Single<DATA> = { data() }
fun <DATA> SingleDataSource<DATA>.observableFunction(): () -> Observable<DATA> = { data().toObservable() }