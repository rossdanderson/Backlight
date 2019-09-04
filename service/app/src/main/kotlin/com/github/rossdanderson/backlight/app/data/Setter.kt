package com.github.rossdanderson.backlight.app.data

interface Setter<S, A> {
    fun set(s: S, b: A): S

    fun modify(s: S, f: (A) -> A): S

    companion object {
        operator fun <S, A> invoke(modify: (S, ((A) -> A)) -> S): Setter<S, A> =
            object : Setter<S, A> {
                override fun modify(s: S, f: (A) -> A): S = modify(s, f)

                override fun set(s: S, b: A): S = modify(s) { b }
            }
    }
}
