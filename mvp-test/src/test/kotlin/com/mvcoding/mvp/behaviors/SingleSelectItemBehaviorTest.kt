package com.mvcoding.mvp.behaviors

import com.jakewharton.rxrelay2.PublishRelay
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.trampolines
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test

class SingleSelectItemBehaviorTest {

    private val item = "item"
    private val otherItem = "otherItem"
    private val noItem = ""

    @Test
    fun `sets item selected after select when nothing was selected yet`() {
        testSetsItemSelectedAfterSelectWhenNothingWasSelectedYet(item, noItem, createPresenter())
    }

    @Test
    fun `sets item selected after select when other item was selected`() {
        testSetsItemSelectedAfterSelectWhenOtherItemWasSelected(item, otherItem, createPresenter())
    }

    @Test
    fun `sets item not selected after select when this item was selected`() {
        testSetsItemNotSelectedAfterSelectWhenThisItemWasSelected(item, noItem, createPresenter())
    }

    @Test
    fun `sets item not selected on detach when this item was selected`() {
        testSetsItemNotSelectedOnDetachWhenThisItemWasSelected(item, noItem, createPresenter(true))
    }

    @Test
    fun `does not change select state on detach when other item was selected`() {
        testDoesNotChangeSelectStateOnDetachWhenOtherItemWasSelected(otherItem, noItem, createPresenter())
    }

    @Test
    fun `does not change select state on detach when nothing was selected`() {
        testDoesNotChangeSelectStateOnDetachWhenNothingWasSelected(noItem, createPresenter())
    }

    @Test
    fun `shows nothing selected when nothing is selected`() {
        testShowsNothingSelectedWhenNothingIsSelected(item, noItem, createPresenter())
    }

    @Test
    fun `shows other selected when other is selected`() {
        testShowsOtherSelectedWhenOtherItemIsSelected(item, otherItem, createPresenter())
    }

    @Test
    fun `shows this selected when this is selected`() {
        testShowsThisSelectedWhenThisItemIsSelected(item, createPresenter())
    }

    private fun createPresenter(deselectOnDetach: Boolean = false): (() -> Observable<String>, (String) -> Unit) -> SingleSelectItemBehavior<String, SingleSelectItemBehavior.View<String>> {
        return { get, set -> SingleSelectItemBehavior(item, noItem, get, set, trampolines, deselectOnDetach) }
    }
}

inline fun <ITEM : Any, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testSingleSelectItemBehavior(
        item: ITEM,
        noItem: ITEM,
        otherItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {

    testSetsItemSelectedAfterSelectWhenNothingWasSelectedYet(item, noItem, createPresenter)
    testSetsItemSelectedAfterSelectWhenOtherItemWasSelected(item, otherItem, createPresenter)
    testSetsItemNotSelectedAfterSelectWhenThisItemWasSelected(item, noItem, createPresenter)
    testSetsItemNotSelectedOnDetachWhenThisItemWasSelected(item, noItem, createPresenter)
    testDoesNotChangeSelectStateOnDetachWhenOtherItemWasSelected(otherItem, noItem, createPresenter)
    testDoesNotChangeSelectStateOnDetachWhenNothingWasSelected(noItem, createPresenter)
    testShowsNothingSelectedWhenNothingIsSelected(item, noItem, createPresenter)
    testShowsOtherSelectedWhenOtherItemIsSelected(item, otherItem, createPresenter)
    testShowsThisSelectedWhenThisItemIsSelected(item, createPresenter)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testSetsItemSelectedAfterSelectWhenNothingWasSelectedYet(
        item: ITEM,
        noItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val setSelectedItem = mock<(ITEM) -> Unit>()
    val selectsRelay = PublishRelay.create<Unit>()
    val presenter = createPresenter(getSelectedItem, setSelectedItem)
    val view = view<ITEM, VIEW>()
    whenever(view.selects()).thenReturn(selectsRelay)
    whenever(getSelectedItem()).thenReturn(Observable.just(noItem))
    presenter attach view

    selectsRelay.accept(Unit)

    verify(setSelectedItem).invoke(item)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testSetsItemSelectedAfterSelectWhenOtherItemWasSelected(
        item: ITEM,
        otherItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val setSelectedItem = mock<(ITEM) -> Unit>()
    val selectsRelay = PublishRelay.create<Unit>()
    val presenter = createPresenter(getSelectedItem, setSelectedItem)
    val view = view<ITEM, VIEW>()
    whenever(view.selects()).thenReturn(selectsRelay)
    whenever(getSelectedItem()).thenReturn(Observable.just(otherItem))
    presenter attach view

    selectsRelay.accept(Unit)

    verify(setSelectedItem).invoke(item)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testSetsItemNotSelectedAfterSelectWhenThisItemWasSelected(
        item: ITEM,
        noItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val setSelectedItem = mock<(ITEM) -> Unit>()
    val selectsRelay = PublishRelay.create<Unit>()
    val presenter = createPresenter(getSelectedItem, setSelectedItem)
    val view = view<ITEM, VIEW>()
    whenever(view.selects()).thenReturn(selectsRelay)
    whenever(getSelectedItem()).thenReturn(Observable.just(item))
    presenter attach view

    selectsRelay.accept(Unit)

    verify(setSelectedItem).invoke(noItem)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testSetsItemNotSelectedOnDetachWhenThisItemWasSelected(
        item: ITEM,
        noItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val setSelectedItem = mock<(ITEM) -> Unit>()
    val presenter = createPresenter(getSelectedItem, setSelectedItem)
    val view = view<ITEM, VIEW>()
    whenever(getSelectedItem()).thenReturn(Observable.just(item))

    presenter attach view
    presenter detach view

    verify(setSelectedItem).invoke(noItem)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testDoesNotChangeSelectStateOnDetachWhenOtherItemWasSelected(
        otherItem: ITEM,
        noItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val setSelectedItem = mock<(ITEM) -> Unit>()
    val presenter = createPresenter(getSelectedItem, setSelectedItem)
    val view = view<ITEM, VIEW>()
    whenever(getSelectedItem()).thenReturn(Observable.just(otherItem))

    presenter attach view
    presenter detach view

    verify(setSelectedItem, never()).invoke(noItem)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testDoesNotChangeSelectStateOnDetachWhenNothingWasSelected(
        noItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val setSelectedItem = mock<(ITEM) -> Unit>()
    val presenter = createPresenter(getSelectedItem, setSelectedItem)
    val view = view<ITEM, VIEW>()
    whenever(getSelectedItem()).thenReturn(Observable.just(noItem))

    presenter attach view
    presenter detach view

    verify(setSelectedItem, never()).invoke(noItem)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testShowsNothingSelectedWhenNothingIsSelected(
        item: ITEM,
        noItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val presenter = createPresenter(getSelectedItem, mock())
    val view = view<ITEM, VIEW>()
    whenever(getSelectedItem()).thenReturn(Observable.just(noItem))

    presenter attach view

    verify(view).showNothingSelected(item)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testShowsOtherSelectedWhenOtherItemIsSelected(
        item: ITEM,
        otherItem: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val presenter = createPresenter(getSelectedItem, mock())
    val view = view<ITEM, VIEW>()
    whenever(getSelectedItem()).thenReturn(Observable.just(otherItem))

    presenter attach view

    verify(view).showOtherSelected(item, otherItem)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> testShowsThisSelectedWhenThisItemIsSelected(
        item: ITEM,
        createPresenter: (() -> Observable<ITEM>, (ITEM) -> Unit) -> Presenter<VIEW>) {
    val getSelectedItem = mock<() -> Observable<ITEM>>()
    val presenter = createPresenter(getSelectedItem, mock())
    val view = view<ITEM, VIEW>()
    whenever(getSelectedItem()).thenReturn(Observable.just(item))

    presenter attach view

    verify(view).showThisSelected(item)
}

inline fun <ITEM, reified VIEW : SingleSelectItemBehavior.View<ITEM>> view(): VIEW = mock<VIEW>().apply {
    whenever(selects()).thenReturn(Observable.never())
}