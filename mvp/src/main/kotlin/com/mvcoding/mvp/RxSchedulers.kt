package com.mvcoding.mvp

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers.trampoline

data class RxSchedulers(val io: Scheduler, val main: Scheduler, val computation: Scheduler)

internal val trampolines = RxSchedulers(trampoline(), trampoline(), trampoline())