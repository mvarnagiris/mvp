package com.mvcoding.mvp.data

import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

class PagingDataSourceTest {

    private val invalidatingInputRelay = PublishRelay.create<InvalidatingInput>()

    private val getInvalidatingInput = mock<() -> Observable<InvalidatingInput>>().apply { whenever(invoke()).thenReturn(invalidatingInputRelay) }
    private val getPage = mock<(PageInput) -> Single<Data>>()
    private val getNextPageInput = mock<(InvalidatingInput, List<Page<PageInput, Data>>) -> Single<PageInput>>()
    private val hasNextPage = mock<(List<Page<PageInput, Data>>) -> Boolean>()
    private val pagingDataSource = PagingDataSource(getInvalidatingInput, getPage, getNextPageInput, hasNextPage)
    private val observer1 = TestObserver.create<PagingData<PageInput, Data>>()
    private val observer2 = TestObserver.create<PagingData<PageInput, Data>>()
    private val observer3 = TestObserver.create<PagingData<PageInput, Data>>()

    @Before
    fun setUp() {
        whenever(getPage(any())).thenAnswer { Single.just(it.getArgument<PageInput>(0) + it.getArgument<PageInput>(0)) }
        whenever(getNextPageInput(any(), any())).thenAnswer { Single.just(it.getArgument<List<*>>(1).size.toString()) }
        whenever(hasNextPage(any())).thenAnswer { it.getArgument<List<*>>(0).size < 3 }
    }

    @Test
    fun `does not return anything by default when there is no data yet`() {
        pagingDataSource.data().subscribe(observer1)

        observer1.assertNoValues()
    }

    @Test
    fun `initially observer will get first page as SoFarAllPagingData when there are more pages`() {
        pagingDataSource.data().subscribe(observer1)
        pagingDataSource.data().subscribe(observer2)

        receiveInvalidatingInput(0)

        observer1.assertValue(SoFarAllPagingData(listOf(Page("0", "00"))))
        observer2.assertValue(SoFarAllPagingData(listOf(Page("0", "00"))))
        verify(getPage, times(1)).invoke(any())
    }

    @Test
    fun `initially observer will get first page as AllPagingData when there are no more pages`() {
        whenever(hasNextPage(any())).thenReturn(false)
        pagingDataSource.data().subscribe(observer1)
        pagingDataSource.data().subscribe(observer2)

        receiveInvalidatingInput(0)

        observer1.assertValue(AllPagingData(listOf(Page("0", "00"))))
        observer2.assertValue(AllPagingData(listOf(Page("0", "00"))))
    }

    @Test
    fun `observer will get NextPagePagingData data when requesting next page and there are still more pages`() {
        pagingDataSource.data().subscribe(observer1)
        pagingDataSource.data().subscribe(observer2)

        receiveInvalidatingInput(0)
        pagingDataSource.getPage()

        observer1.assertValueAt(1, NextPagePagingData(listOf(Page("0", "00"), Page("1", "11"))))
        observer2.assertValueAt(1, NextPagePagingData(listOf(Page("0", "00"), Page("1", "11"))))
    }

    @Test
    fun `observer will get LastPagePagingData data when requesting next page and there are no more pages`() {
        pagingDataSource.data().subscribe(observer1)
        pagingDataSource.data().subscribe(observer2)

        receiveInvalidatingInput(0)
        whenever(hasNextPage(any())).thenReturn(false)
        pagingDataSource.getPage()

        observer1.assertValueAt(1, LastPagePagingData(listOf(Page("0", "00"), Page("1", "11"))))
        observer2.assertValueAt(1, LastPagePagingData(listOf(Page("0", "00"), Page("1", "11"))))
    }

    @Test
    fun `late observers will get currently loaded pages`() {
        pagingDataSource.data().subscribe()
        receiveInvalidatingInput(0)

        pagingDataSource.data().subscribe(observer1)

        pagingDataSource.getPage()
        pagingDataSource.data().subscribe(observer2)

        whenever(hasNextPage(any())).thenReturn(false)
        pagingDataSource.getPage()
        pagingDataSource.data().subscribe(observer3)

        observer1.assertValues(SoFarAllPagingData(listOf(Page("0", "00"))), NextPagePagingData(listOf(Page("0", "00"), Page("1", "11"))), LastPagePagingData(listOf(Page("0", "00"), Page("1", "11"), Page("2", "22"))))
        observer2.assertValues(SoFarAllPagingData(listOf(Page("0", "00"), Page("1", "11"))), LastPagePagingData(listOf(Page("0", "00"), Page("1", "11"), Page("2", "22"))))
        observer3.assertValues(AllPagingData(listOf(Page("0", "00"), Page("1", "11"), Page("2", "22"))))
    }

    @Test
    fun `can invalidate cache`() {
        pagingDataSource.data().subscribe(observer1)
        receiveInvalidatingInput(0)
        pagingDataSource.getPage()

        pagingDataSource.data().subscribe(observer2)
        pagingDataSource.invalidate()
        pagingDataSource.getPage()

        pagingDataSource.data().subscribe(observer3)

        observer1.assertValues(SoFarAllPagingData(listOf(Page("0", "00"))), NextPagePagingData(listOf(Page("0", "00"), Page("1", "11"))), SoFarAllPagingData(listOf(Page("0", "00"))))
        observer2.assertValues(SoFarAllPagingData(listOf(Page("0", "00"), Page("1", "11"))), SoFarAllPagingData(listOf(Page("0", "00"))))
        observer3.assertValues(SoFarAllPagingData(listOf(Page("0", "00"))))
    }

    private fun receiveInvalidatingInput(invalidatingInput: InvalidatingInput) = invalidatingInputRelay.accept(invalidatingInput)
}

private typealias InvalidatingInput = Int
private typealias PageInput = String
private typealias Data = String