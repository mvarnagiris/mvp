package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.PublishRelay
import com.mvcoding.mvp.DataSource
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class PagingDataSource<INVALIDATING_INPUT, PAGE_INPUT, DATA>(
        getInvalidatingInput: () -> Observable<INVALIDATING_INPUT>,
        getPage: (PAGE_INPUT) -> Single<DATA>,
        getNextPageInput: (INVALIDATING_INPUT, List<Page<PAGE_INPUT, DATA>>) -> Single<PAGE_INPUT>,
        private val hasNextPage: (List<Page<PAGE_INPUT, DATA>>) -> Boolean) : DataSource<PagingData<PAGE_INPUT, DATA>> {

    private val nextPagesRelay = PublishRelay.create<Unit>()
    private val pages = AtomicReference(emptyList<Page<PAGE_INPUT, DATA>>())
    private val isLoading = AtomicBoolean(false)

    private val pagingObservable = getInvalidatingInput()
            .doOnNext { invalidate() }
            .switchMap { invalidatingInput ->
                nextPagesRelay
                        .startWith(if (!isLoading.get() || pages.get().isEmpty()) Observable.just(Unit) else Observable.never())
                        .filter { !isLoading.getAndSet(true) }
                        .switchMapSingle {
                            getNextPageInput(invalidatingInput, pages.get())
                                    .flatMap { input -> getPage(input).map { Page(input, it) } }
                                    .map {
                                        val newPages = pages.get().plus(it)
                                        pages.set(newPages)
                                        newPages
                                    }
                        }
            }
            .map { createPagingData(it) }
            .doOnNext { isLoading.set(false) }
            .startWith { Observable.just(createPagingData(pages.get())) }
            .share()

    private fun createPagingData(pages: List<Page<PAGE_INPUT, DATA>>): PagingData<PAGE_INPUT, DATA> {
        val hasNext = hasNextPage(pages)
        return when {
            pages.size == 1 && hasNext -> SoFarAllPagingData(pages)
            pages.size == 1 && !hasNext -> AllPagingData(pages)
            pages.size > 1 && hasNext -> NextPagePagingData(pages)
            else -> LastPagePagingData(pages)
        }
    }

    fun nextPage() = nextPagesRelay.accept(Unit)
    override fun data(): Observable<PagingData<PAGE_INPUT, DATA>> = pagingObservable

    private fun invalidate() {
        pages.set(emptyList())
        isLoading.set(false)
    }
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