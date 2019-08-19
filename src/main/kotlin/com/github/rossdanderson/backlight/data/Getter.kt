package com.github.rossdanderson.backlight.data

interface Getter<S, A> {

    fun get(s: S): A

    companion object {
        operator fun <S, A> invoke(get: (S) -> A): Getter<S, A> = object : Getter<S, A> {
            override fun get(s: S): A = get(s)
        }
    }
}