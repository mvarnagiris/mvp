package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.*
import com.mvcoding.mvp.behaviors.SingleSelectItemBehavior.SingleSelectState.*
import io.reactivex.rxkotlin.withLatestFrom

class SingleSelectItemBehavior<in ITEM, VIEW : SingleSelectItemBehavior.View<ITEM>>(
        private val item: ITEM,
        private val noItem: ITEM,
        private val getSelectedItem: () -> O<ITEM>,
        private val setSelectedItem: (ITEM) -> Unit,
        private val schedulers: RxSchedulers,
        private val deselectOnDetach: Boolean = false) : Behavior<VIEW>() {

    constructor(
            item: ITEM,
            noItem: ITEM,
            selectedItemSource: DataSource<ITEM>,
            selectedItemWriter: DataWriter<ITEM>,
            schedulers: RxSchedulers,
            deselectOnDetach: Boolean = false) :
            this(item, noItem, selectedItemSource.function(), selectedItemWriter.function(), schedulers, deselectOnDetach)

    override fun onViewAttached(view: VIEW) {
        super.onViewAttached(view)

        val singleSelectStateObservable = getSelectedItem().map { newSingleSelectState(it) }

        singleSelectStateObservable
                .distinctUntilChanged()
                .observeOn(schedulers.main)
                .subscribeUntilDetached { showSingleSelectState(view, it) }

        view.selects()
                .withLatestFrom(singleSelectStateObservable) { _, singleSelectState -> if (singleSelectState == ThisSelected) noItem else item }
                .observeOn(schedulers.main)
                .subscribeUntilDetached { setSelectedItem(it) }
    }

    override fun onViewDetached(view: VIEW) {
        super.onViewDetached(view)
        if (deselectOnDetach) {
            getSelectedItem().firstOrError().map { it }.subscribe { item -> if (item == this.item) setSelectedItem(noItem) }
        }
    }

    private fun newSingleSelectState(item: ITEM): SingleSelectState = when (item) {
        this.item -> ThisSelected
        noItem -> NothingSelected
        else -> OtherSelected(item)
    }

    private fun showSingleSelectState(view: VIEW, singleSelectState: SingleSelectState) {
        @Suppress("UNCHECKED_CAST")
        when (singleSelectState) {
            ThisSelected -> view.showThisSelected(item)
            NothingSelected -> view.showNothingSelected(item)
            is OtherSelected<*> -> view.showOtherSelected(item, singleSelectState.item as ITEM)
        }
    }

    private sealed class SingleSelectState {
        object ThisSelected : SingleSelectState()
        object NothingSelected : SingleSelectState()
        data class OtherSelected<out ITEM>(val item: ITEM) : SingleSelectState()
    }

    interface View<in ITEM> : Presenter.View {
        fun selects(): O<Unit>
        fun showThisSelected(item: ITEM)
        fun showNothingSelected(item: ITEM)
        fun showOtherSelected(item: ITEM, other: ITEM)
    }
}