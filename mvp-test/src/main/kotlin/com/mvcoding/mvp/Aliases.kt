package com.mvcoding.mvp

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

internal typealias O<T> = Observable<T>
internal typealias F<T> = Flowable<T>
internal typealias S<T> = Single<T>
internal typealias M<T> = Maybe<T>