package com.mvcoding.mvp.data

import com.mvcoding.mvp.*
import io.reactivex.Observable

class FunctionDataCache<DATA>(
        private val getData: () -> Observable<DATA>,
        private val setData: (DATA) -> Unit) : DataCache<DATA> {

    override fun data(): Observable<DATA> = getData()
    override fun write(data: DATA) = setData(data)
}

fun <DATA> (() -> Observable<DATA>).dataCache(set: (DATA) -> Unit) = FunctionDataCache(this, set)
fun <DATA> (() -> Observable<DATA>).dataCache(dataWriter: DataWriter<DATA>) = FunctionDataCache(this, dataWriter.writeFunction())
fun <DATA> Observable<DATA>.dataCache(set: (DATA) -> Unit) = FunctionDataCache({ this }, set)
fun <DATA> Observable<DATA>.dataCache(dataWriter: DataWriter<DATA>) = FunctionDataCache({ this }, { dataWriter.write(it) })
fun <DATA> ((DATA) -> Unit).dataCache(get: () -> Observable<DATA>) = FunctionDataCache(get, this)
fun <DATA> ((DATA) -> Unit).dataCache(dataSource: DataSource<DATA>) = FunctionDataCache(dataSource.observableFunction(), this)
fun <DATA> DataWriter<DATA>.dataCache(get: () -> Observable<DATA>) = FunctionDataCache(get, this.writeFunction())
fun <DATA> DataWriter<DATA>.dataCache(dataSource: DataSource<DATA>) = FunctionDataCache(dataSource.observableFunction(), this.writeFunction())