package com.mvcoding.mvp

interface DataSource<DATA> {
    fun data(): O<DATA>
}

fun <DATA> DataSource<DATA>.function(): () -> O<DATA> = { data() }