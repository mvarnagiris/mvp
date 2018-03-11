package com.mvcoding.mvp.data

import com.mvcoding.mvp.O

fun <T> O<T>.withMemoryCache(): O<T> = replay(1).autoConnect()