package com.mvcoding.mvp

import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class Presenter<VIEW : Presenter.View> {
    private lateinit var viewSubscriptions: CompositeSubscription
    private var view: View? = null

    fun attach(view: VIEW) {
        ensureViewIsNotAttached(view)
        this.view = view
        this.viewSubscriptions = CompositeSubscription()
        onViewAttached(view)
    }

    fun detach(view: VIEW) {
        ensureGivenViewIsAttached(view)
        this.view = null
        this.viewSubscriptions.unsubscribe()
        onViewDetached(view)
    }

    open protected fun onViewAttached(view: VIEW) {
    }

    open protected fun onViewDetached(view: VIEW) {
    }

    protected fun unsubscribeOnDetach(subscription: Subscription) {
        viewSubscriptions.add(subscription)
    }

    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit) = subscribe(onNext).apply { unsubscribeOnDetach(this) }
    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit) =
            subscribe(onNext, onError).apply { unsubscribeOnDetach(this) }

    private fun ensureViewIsNotAttached(view: VIEW) {
        if (this.view != null) throw IllegalStateException("Cannot attach $view, because ${this.view} is already attached")
    }

    private fun ensureGivenViewIsAttached(view: VIEW) {
        if (this.view == null)
            throw IllegalStateException("View is already detached.")
        else if (this.view != view)
            throw IllegalStateException("Trying to detach different view. We have view: ${this.view}. Trying to detach view: $view")
    }

    interface View
}