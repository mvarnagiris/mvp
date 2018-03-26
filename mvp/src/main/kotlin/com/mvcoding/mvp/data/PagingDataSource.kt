package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.PublishRelay
import com.mvcoding.mvp.DataSource
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class PagingDataSource<INVALIDATING_INPUT, PAGE_INPUT, DATA>(
        getInvalidatingInput: () -> Observable<INVALIDATING_INPUT>,
        private val getPage: (PAGE_INPUT) -> Single<DATA>,
        private val getNextPageInput: (INVALIDATING_INPUT, List<Page<PAGE_INPUT, DATA>>) -> Single<PAGE_INPUT>,
        private val hasNextPage: (List<Page<PAGE_INPUT, DATA>>) -> Boolean) : DataSource<PagingData<PAGE_INPUT, DATA>> {

    private val nextPagesRelay = PublishRelay.create<Unit>()
    private val stopRequestRelay = PublishRelay.create<Unit>()
    private val pages = AtomicReference(emptyList<Page<PAGE_INPUT, DATA>>())
    private val isLoading = AtomicBoolean(false)

    private val pagingObservable = getInvalidatingInput()
            .doOnNext { invalidate() }
            .switchMap { loadPages(it) }
            .map { createPagingData(it, false) }
            .share()

    private fun loadPages(invalidatingInput: INVALIDATING_INPUT): Observable<List<Page<PAGE_INPUT, DATA>>> {
        return nextPagesRelay
                .startWith(triggerIfNotLoadingAndHasNoPages())
                .filter { onlyNotLoadingAndMakeLoading() }
                .switchMap {
                    getNextPageInput(invalidatingInput)
                            .switchMap { getPageData(it) }
                            .map { newPages(it) }
                }
                .doOnNext { makeNotLoading() }
    }

    private fun makeNotLoading() {
        isLoading.set(false)
    }

    private fun newPages(page: Page<PAGE_INPUT, DATA>): List<Page<PAGE_INPUT, DATA>> {
        val newPages = pages.get().plus(page)
        pages.set(newPages)
        return newPages
    }

    private fun getPageData(input: PAGE_INPUT) =
            getPage(input).toObservable().takeUntil(stopRequestRelay).map { Page(input, it) }

    private fun getNextPageInput(invalidatingInput: INVALIDATING_INPUT): Observable<PAGE_INPUT> {
        return getNextPageInput(invalidatingInput, pages.get())
                .toObservable()
                .takeUntil(stopRequestRelay)
    }

    private fun onlyNotLoadingAndMakeLoading() = !isLoading.getAndSet(true)

    private fun triggerIfNotLoadingAndHasNoPages() =
            if (!isLoading.get() && pages.get().isEmpty()) Observable.just(Unit) else Observable.never()

    override fun data(): Observable<PagingData<PAGE_INPUT, DATA>> = when {
        pages.get().isNotEmpty() -> pagingObservable.startWith(createPagingData(pages.get(), true))
        !isLoading.get() -> pagingObservable.apply { getPage() }
        else -> pagingObservable
    }

    fun getPage() = nextPagesRelay.accept(Unit)

    fun invalidate() {
        stopRequestRelay.accept(Unit)
        pages.set(emptyList())
        isLoading.set(false)
    }

    private fun createPagingData(pages: List<Page<PAGE_INPUT, DATA>>, isInitialSubscription: Boolean): PagingData<PAGE_INPUT, DATA> {
        val hasNext = hasNextPage(pages)
        return when {
            (pages.size == 1 || isInitialSubscription) && hasNext -> SoFarAllPagingData(pages)
            (pages.size == 1 || isInitialSubscription) && !hasNext -> AllPagingData(pages)
            pages.size > 1 && hasNext -> NextPagePagingData(pages)
            else -> LastPagePagingData(pages)
        }
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