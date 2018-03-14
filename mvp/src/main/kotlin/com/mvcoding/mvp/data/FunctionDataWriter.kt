package com.mvcoding.mvp.data

import com.mvcoding.mvp.DataWriter

class FunctionDataWriter<in DATA>(private val dataWriter: (DATA) -> Unit) : DataWriter<DATA> {
    override fun write(data: DATA) = dataWriter(data)
}

fun <DATA> ((DATA) -> Unit).dataWriter() = FunctionDataWriter(this)