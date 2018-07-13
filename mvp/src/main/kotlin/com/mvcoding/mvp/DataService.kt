package com.mvcoding.mvp

import io.reactivex.Single

interface DataService<INPUT, DATA> {
    fun run(input: INPUT): Single<DATA>
}

fun <INPUT, DATA> DataService<INPUT, DATA>.singleFunction(): (INPUT) -> Single<DATA> = { run(it) }