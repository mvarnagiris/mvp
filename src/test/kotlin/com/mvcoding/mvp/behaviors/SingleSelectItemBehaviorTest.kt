package com.mvcoding.mvp.behaviors

import com.jakewharton.rxrelay2.PublishRelay
import com.mvcoding.mvp.O
import com.mvcoding.mvp.trampolines
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

class SingleSelectItemBehaviorTest {

    private val selectsRelay = PublishRelay.create<Unit>()

    private val item = "item"
    private val otherItem = "otherItem"
    private val noItem = ""

    private val getSelectedItem = mock<() -> O<String>>()
    private val setSelectedItem = mock<(String) -> Unit>()
    private val view = mock<TestView>()
    private val behavior = SingleSelectItemBehavior<String, TestView>(item,
            noItem,
            getSelectedItem,
            setSelectedItem,
            { view, item -> view.showNothingSelected(item) },
            { view, item -> view.showOtherSelected(item) },
            { view, item -> view.showThisSelected(item) },
            trampolines)

    @Before
    fun setUp() {
        whenever(view.selects()).thenReturn(selectsRelay)
    }

    @Test
    fun `sets item selected after select when nothing was selected yet`() {
        whenever(getSelectedItem()).thenReturn(O.just(noItem))
        behavior attach view

        select()

        verify(setSelectedItem).invoke(item)
    }

    @Test
    fun `sets item selected after select when other item is selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(otherItem))
        behavior attach view

        select()

        verify(setSelectedItem).invoke(item)
    }

    @Test
    fun `sets item not selected after select when this item was selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(item))
        behavior attach view

        select()

        verify(setSelectedItem).invoke(noItem)
    }

    @Test
    fun `sets item not selected on detach when this item was selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(item))

        behavior attach view
        behavior detach view

        verify(setSelectedItem).invoke(noItem)
    }

    @Test
    fun `does not change select state on detach when other item was selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(otherItem))

        behavior attach view
        behavior detach view

        verify(setSelectedItem, never()).invoke(any())
    }

    @Test
    fun `does not change select state on detach when nothing was selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(noItem))

        behavior attach view
        behavior detach view

        verify(setSelectedItem, never()).invoke(any())
    }

    @Test
    fun `shows nothing selected when nothing is selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(noItem))

        behavior attach view

        verify(view).showNothingSelected(item)
    }

    @Test
    fun `shows other selected when other is selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(otherItem))

        behavior attach view

        verify(view).showOtherSelected(item)
    }

    @Test
    fun `shows this selected when this is selected`() {
        whenever(getSelectedItem()).thenReturn(O.just(item))

        behavior attach view

        verify(view).showThisSelected(item)
    }

    private fun select() = selectsRelay.accept(Unit)

    private interface TestView : SingleSelectItemBehavior.View {
        fun showNothingSelected(item: String)
        fun showOtherSelected(item: String)
        fun showThisSelected(item: String)
    }
}