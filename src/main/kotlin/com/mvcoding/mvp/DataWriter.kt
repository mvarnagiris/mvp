package com.mvcoding.mvp

interface DataWriter<in DATA> {
    fun write(data: DATA)
}

fun <DATA> DataWriter<DATA>.function(): (DATA) -> Unit = { write(it) }