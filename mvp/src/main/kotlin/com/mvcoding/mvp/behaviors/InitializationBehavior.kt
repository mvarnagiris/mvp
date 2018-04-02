package com.mvcoding.mvp.behaviors

import com.mvcoding.mvp.Behavior
import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.views.*
import io.reactivex.Single

class InitializationBehavior<RESULT, in SUCCESS, in FAILURE, in ERROR, VIEW : InitializationBehavior.View<SUCCESS, FAILURE, ERROR>>(
        private val initialize: () -> Single<RESULT>,
        private val isSuccess: (RESULT) -> Boolean,
        private val getSuccess: (RESULT) -> SUCCESS,
        private val getFailure: (RESULT) -> FAILURE,
        private val mapError: (Throwable) -> ERROR,
        private val schedulers: RxSchedulers) : Behavior<VIEW>() {

    override fun onViewAttached(view: VIEW) {
        super.onViewAttached(view)

        initialize().subscribeOn(schedulers.io)
                .showHideLoading(view, schedulers)
                .doOnSuccess { if (isSuccess(it)) view.displayInitialized(getSuccess(it)) else view.displayNotInitialized(getFailure(it)) }
                .retryWhen { it.flatMap { view.showResolvableError(mapError(it)).filter { it == ErrorResolution.POSITIVE }.toFlowable() } }
                .subscribeUntilDetached({ view.close() }, { view.showError(mapError(it)) })
    }

    interface View<in INITIALIZED, in NOT_INITIALIZED, in ERROR> : Presenter.View, LoadingView, ResolvableErrorView<ERROR>, ErrorView<ERROR>, CloseableView {
        fun displayInitialized(success: INITIALIZED)
        fun displayNotInitialized(data: NOT_INITIALIZED)
    }
}