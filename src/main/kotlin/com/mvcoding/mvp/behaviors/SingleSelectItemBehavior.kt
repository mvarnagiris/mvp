package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Behavior
import com.mvcoding.mvp.O
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.behaviors.SingleSelectItemBehavior.SingleSelectState.*
import io.reactivex.rxkotlin.withLatestFrom

class SingleSelectItemBehavior<ITEM, in VIEW>(
        private val item: ITEM,
        private val noItem: ITEM,
        private val getSelectedItem: () -> O<ITEM>,
        private val setSelectedItem: (ITEM) -> Unit,
        private val showNothingSelected: (VIEW, ITEM) -> Unit,
        private val showOtherSelected: (VIEW, ITEM) -> Unit,
        private val showThisSelected: (VIEW, ITEM) -> Unit,
        private val schedulers: RxSchedulers) : Behavior<VIEW>()
        where VIEW : Presenter.View,
              VIEW : SingleSelectItemBehavior.View {

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
            NOTHING_SELECTED -> showNothingSelected(view, item)
            OTHER_SELECTED -> showOtherSelected(view, item)
            THIS_SELECTED -> showThisSelected(view, item)
        }
    }

    enum class SingleSelectState { NOTHING_SELECTED, OTHER_SELECTED, THIS_SELECTED }

    interface View : Presenter.View {
        fun selects(): O<Unit>
    }
}