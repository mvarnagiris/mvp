package com.mvcoding.mvp

abstract class Behavior<VIEW : Presenter.View>(vararg behaviors: Behavior<VIEW>) : Presenter<VIEW>(*behaviors)