package com.mvcoding.mvp

abstract class Behavior<in VIEW : Presenter.View> : Presenter<VIEW>()