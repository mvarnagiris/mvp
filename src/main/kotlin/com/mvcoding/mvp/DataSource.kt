package com.mvcoding.mvp

import io.reactivex.Observable

interface DataSource<in INPUT, DATA> {
    fun data(input: INPUT): Observable<DATA>
}