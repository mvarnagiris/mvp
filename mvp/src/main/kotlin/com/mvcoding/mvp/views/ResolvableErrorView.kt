package com.mvcoding.mvp.views

import com.mvcoding.mvp.Presenter
import com.mvcoding.mvp.RxSchedulers
import com.mvcoding.mvp.S
import com.mvcoding.mvp.trampolines
import io.reactivex.Flowable
import io.reactivex.Single

interface ResolvableErrorView<in ERROR> : Presenter.View {
    fun showResolvableError(error: ERROR): Single<ErrorResolution>
}

enum class ErrorResolution { POSITIVE, NEGATIVE }

fun <T, ERROR, VIEW : ResolvableErrorView<ERROR>> S<T>.resolveErrorOrFail(view: VIEW, mapError: (Throwable) -> ERROR, schedulers: RxSchedulers = trampolines): S<T> =
        retryWhen {
            it.observeOn(schedulers.main).switchMap { throwable ->
                view.showResolvableError(mapError(throwable)).flatMapPublisher {
                    if (it == ErrorResolution.POSITIVE) Flowable.just(it)
                    else Flowable.error(throwable)
                }
            }
        }

fun <T, VIEW : ResolvableErrorView<Throwable>> S<T>.resolveErrorOrFail(view: VIEW, schedulers: RxSchedulers = trampolines): S<T> = resolveErrorOrFail(view, { it }, schedulers)