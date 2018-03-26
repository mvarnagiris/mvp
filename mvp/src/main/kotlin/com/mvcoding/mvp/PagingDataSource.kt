package com.mvcoding.mvp

interface PagingDataSource<INPUT, DATA> : DataSource<PagingData<INPUT, DATA>> {
    fun getPage()
    fun invalidate()
}

data class Page<out INPUT, out DATA>(val input: INPUT, val data: DATA)

sealed class PagingData<out INPUT, out DATA> {
    abstract val pages: List<Page<INPUT, DATA>>
}

data class SoFarAllPagingData<out INPUT, out DATA>(override val pages: List<Page<INPUT, DATA>>) : PagingData<INPUT, DATA>()

data class AllPagingData<out INPUT, out DATA>(override val pages: List<Page<INPUT, DATA>>) : PagingData<INPUT, DATA>()

data class NextPagePagingData<out INPUT, out DATA>(override val pages: List<Page<INPUT, DATA>>) : PagingData<INPUT, DATA>() {
    val nextPage = pages.last()
}

data class LastPagePagingData<out INPUT, out DATA>(override val pages: List<Page<INPUT, DATA>>) : PagingData<INPUT, DATA>() {
    val lastPage = pages.last()
}