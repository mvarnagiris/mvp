package com.mvcoding.mvp

interface DataSource<DATA> {
    fun data(): O<DATA>
}