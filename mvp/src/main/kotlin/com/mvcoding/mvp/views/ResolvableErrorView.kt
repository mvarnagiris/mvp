package com.mvcoding.mvp.views

import com.mvcoding.mvp.Presenter
import io.reactivex.Single

interface ResolvableErrorView<in ERROR> : Presenter.View {
    fun showResolvableError(error: ERROR): Single<ErrorResolution>
}

enum class ErrorResolution { POSITIVE, NEGATIVE }