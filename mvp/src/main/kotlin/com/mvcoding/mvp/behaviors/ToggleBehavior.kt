package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.*
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom

class ToggleBehavior<VIEW : ToggleBehavior.View>(
        private val getSelected: () -> Observable<Boolean>,
        private val setSelected: (Boolean) -> Unit,
        private val schedulers: RxSchedulers) : Behavior<VIEW>() {

    constructor(selectedSource: DataSource<Boolean>, selectedWriter: DataWriter<Boolean>, schedulers: RxSchedulers) :
            this(selectedSource.functionDataSource(), selectedWriter.functionDataWriter(), schedulers)

    constructor(selectedCache: DataCache<Boolean>, schedulers: RxSchedulers) : this(selectedCache, selectedCache, schedulers)

    override fun onViewAttached(view: VIEW) {
        super.onViewAttached(view)

        getSelected()
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeUntilDetached { if (it) view.showToggleOn() else view.showToggleOff() }

        view.toggles()
                .observeOn(schedulers.io)
                .withLatestFrom(getSelected()) { _, isSelected -> !isSelected }
                .subscribeUntilDetached { setSelected(it) }
    }

    interface View : Presenter.View {
        fun toggles(): Observable<Unit>
        fun showToggleOn()
        fun showToggleOff()
    }
}