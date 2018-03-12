package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Behavior
import com.mvcoding.mvp.O
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.behaviors.SingleSelectItemBehavior.SingleSelectState.*
import io.reactivex.rxkotlin.withLatestFrom

class SingleSelectItemBehavior<in ITEM, VIEW : SingleSelectItemBehavior.View<ITEM>>(
        private val item: ITEM,
        private val noItem: ITEM,
        private val getSelectedItem: () -> O<ITEM>,
        private val setSelectedItem: (ITEM) -> Unit,
        private val schedulers: RxSchedulers) : Behavior<VIEW>() {

    override fun onViewAttached(view: VIEW) {
        super.onViewAttached(view)

        val singleSelectStateObservable = getSelectedItem().map { newSingleSelectState(it) }

        singleSelectStateObservable
                .distinctUntilChanged()
                .observeOn(schedulers.main)
                .subscribeUntilDetached { showSingleSelectState(view, it) }

        view.selects()
                .withLatestFrom(singleSelectStateObservable) { _, singleSelectState -> if (singleSelectState == THIS_SELECTED) noItem else item }
                .observeOn(schedulers.main)
                .subscribeUntilDetached { setSelectedItem(it) }
    }

    override fun onViewDetached(view: VIEW) {
        super.onViewDetached(view)
        getSelectedItem().firstOrError().map { it }.subscribe { item -> if (item == this.item) setSelectedItem(noItem) }
    }

    private fun newSingleSelectState(item: ITEM): SingleSelectState = when (item) {
        noItem -> NOTHING_SELECTED
        this.item -> THIS_SELECTED
        else -> OTHER_SELECTED
    }

    private fun showSingleSelectState(view: VIEW, singleSelectState: SingleSelectState) {
        when (singleSelectState) {
            NOTHING_SELECTED -> view.showNothingSelected(item)
            OTHER_SELECTED -> view.showOtherSelected(item)
            THIS_SELECTED -> view.showThisSelected(item)
        }
    }

    enum class SingleSelectState { NOTHING_SELECTED, OTHER_SELECTED, THIS_SELECTED }

    interface View<in DATA> : Presenter.View {
        fun showNothingSelected(item: DATA)
        fun showOtherSelected(item: DATA)
        fun showThisSelected(item: DATA)

        fun selects(): O<Unit>
    }
}