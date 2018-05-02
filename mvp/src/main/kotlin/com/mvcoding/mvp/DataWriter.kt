package com.mvcoding.mvp

interface DataWriter<in DATA> {
    fun write(data: DATA)
}

fun <DATA> DataWriter<DATA>.functionDataWriter(): (DATA) -> Unit = { write(it) }