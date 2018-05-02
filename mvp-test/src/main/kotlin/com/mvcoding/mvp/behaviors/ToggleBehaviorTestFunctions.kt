package com.mvcoding.mvp.behaviors

import com.jakewharton.rxrelay2.PublishRelay
import com.mvcoding.mvp.Presenter
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable


inline fun <reified VIEW : ToggleBehavior.View> testToggleBehavior(
        createPresenter: (() -> Observable<Boolean>, (Boolean) -> Unit) -> Presenter<VIEW>) {

    testShowsToggleStateOn(createPresenter)
    testShowsToggleStateOff(createPresenter)
    testSetsToggleStateOnWhenItWasOff(createPresenter)
    testSetsToggleStateOffWhenItWasOn(createPresenter)
}

inline fun <reified VIEW : ToggleBehavior.View> testShowsToggleStateOn(
        createPresenter: (() -> Observable<Boolean>, (Boolean) -> Unit) -> Presenter<VIEW>) {

    val getSelected = mock<() -> Observable<Boolean>>()
    val view = mock<VIEW>()
    whenever(getSelected()).thenReturn(Observable.just(true))
    whenever(view.toggles()).thenReturn(mock())
    val presenter = createPresenter(getSelected, mock())

    presenter attach view

    verify(view).showToggleOn()
    verify(view, never()).showToggleOff()
}

inline fun <reified VIEW : ToggleBehavior.View> testShowsToggleStateOff(
        createPresenter: (() -> Observable<Boolean>, (Boolean) -> Unit) -> Presenter<VIEW>) {

    val getSelected = mock<() -> Observable<Boolean>>()
    val view = mock<VIEW>()
    whenever(getSelected()).thenReturn(Observable.just(false))
    whenever(view.toggles()).thenReturn(mock())
    val presenter = createPresenter(getSelected, mock())

    presenter attach view

    verify(view).showToggleOff()
    verify(view, never()).showToggleOn()
}

inline fun <reified VIEW : ToggleBehavior.View> testSetsToggleStateOnWhenItWasOff(
        createPresenter: (() -> Observable<Boolean>, (Boolean) -> Unit) -> Presenter<VIEW>) {

    val getSelected = mock<() -> Observable<Boolean>>()
    val setSelected = mock<(Boolean) -> Unit>()
    val view = mock<VIEW>()
    val togglesRelay = PublishRelay.create<Unit>()
    whenever(getSelected()).thenReturn(Observable.just(false))
    whenever(view.toggles()).thenReturn(togglesRelay)
    val presenter = createPresenter(getSelected, setSelected)
    presenter attach view

    togglesRelay.accept(Unit)

    verify(setSelected).invoke(true)
}

inline fun <reified VIEW : ToggleBehavior.View> testSetsToggleStateOffWhenItWasOn(
        createPresenter: (() -> Observable<Boolean>, (Boolean) -> Unit) -> Presenter<VIEW>) {

    val getSelected = mock<() -> Observable<Boolean>>()
    val setSelected = mock<(Boolean) -> Unit>()
    val view = mock<VIEW>()
    val togglesRelay = PublishRelay.create<Unit>()
    whenever(getSelected()).thenReturn(Observable.just(true))
    whenever(view.toggles()).thenReturn(togglesRelay)
    val presenter = createPresenter(getSelected, setSelected)
    presenter attach view

    togglesRelay.accept(Unit)

    verify(setSelected).invoke(false)
}