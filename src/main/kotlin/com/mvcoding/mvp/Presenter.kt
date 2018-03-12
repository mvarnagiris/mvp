package com.mvcoding.mvp

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class Presenter<VIEW : Presenter.View>(private vararg val behaviors: Behavior<VIEW>) {
    private lateinit var compositeDisposable: CompositeDisposable
    private var view: VIEW? = null
    private var viewDelegate: VIEW? = null

    infix fun attach(view: VIEW) {
        ensureViewIsNotAttached(view)
        this.view = view
        viewDelegate = decorateView(view)
        compositeDisposable = CompositeDisposable()
        behaviors.forEach { it attach viewDelegate as VIEW }
        onViewAttached(viewDelegate as VIEW)
    }

    infix fun detach(view: VIEW) {
        ensureGivenViewIsAttached(view)

        compositeDisposable.dispose()
        behaviors.forEach { it detach viewDelegate as VIEW }
        onViewDetached(viewDelegate as VIEW)

        this.view = null
        this.viewDelegate = null
    }

    open protected fun decorateView(view: VIEW): VIEW = view

    operator fun plus(view: VIEW) {
        attach(view)
    }

    operator fun minus(view: VIEW) {
        detach(view)
    }

    protected open fun onViewAttached(view: VIEW) {
    }

    protected open fun onViewDetached(view: VIEW) {
    }

    private fun disposeOnDetach(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    protected fun <T> Observable<T>.subscribeUntilDetached(): Disposable = subscribe().apply { disposeOnDetach(this) }
    protected fun <T> Flowable<T>.subscribeUntilDetached(): Disposable = subscribe().apply { disposeOnDetach(this) }
    protected fun <T> Single<T>.subscribeUntilDetached(): Disposable = subscribe().apply { disposeOnDetach(this) }
    protected fun <T> Maybe<T>.subscribeUntilDetached(): Disposable = subscribe().apply { disposeOnDetach(this) }

    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit): Disposable = subscribe(onNext).apply { disposeOnDetach(this) }
    protected fun <T> Flowable<T>.subscribeUntilDetached(onNext: (T) -> Unit): Disposable = subscribe(onNext).apply { disposeOnDetach(this) }
    protected fun <T> Single<T>.subscribeUntilDetached(onSuccess: (T) -> Unit): Disposable = subscribe(onSuccess).apply { disposeOnDetach(this) }
    protected fun <T> Maybe<T>.subscribeUntilDetached(onSuccess: (T) -> Unit): Disposable = subscribe(onSuccess).apply { disposeOnDetach(this) }

    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
            subscribe(onNext, onError).apply { disposeOnDetach(this) }

    protected fun <T> Flowable<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
            subscribe(onNext, onError).apply { disposeOnDetach(this) }

    protected fun <T> Single<T>.subscribeUntilDetached(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
            subscribe(onSuccess, onError).apply { disposeOnDetach(this) }

    protected fun <T> Maybe<T>.subscribeUntilDetached(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
            subscribe(onSuccess, onError).apply { disposeOnDetach(this) }

    protected fun <T> Observable<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit): Disposable =
            subscribe(onNext, onError, onComplete).apply { disposeOnDetach(this) }

    protected fun <T> Flowable<T>.subscribeUntilDetached(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit): Disposable =
            subscribe(onNext, onError, onComplete).apply { disposeOnDetach(this) }

    protected fun <T> Maybe<T>.subscribeUntilDetached(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit): Disposable =
            subscribe(onSuccess, onError, onComplete).apply { disposeOnDetach(this) }

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