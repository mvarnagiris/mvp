package com.mvcoding.mvp.views

import com.mvcoding.mvp.Presenter

interface CloseableView : Presenter.View {
    fun close()
}