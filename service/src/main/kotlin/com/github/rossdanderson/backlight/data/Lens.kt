package com.github.rossdanderson.backlight.data

/**
 * @param S the source of a [Lens]
 * @param A the focus of a [Lens]
 */
interface Lens<S, A> {

    fun get(s: S): A

    fun set(s: S, b: A): S

    fun asSetter(): Setter<S, A> = Setter { s, f -> set(s, f(get(s))) }

    fun asGetter(): Getter<S, A> = Getter(this::get)

    companion object {
        operator fun <S, A> invoke(
            get: (S) -> A,
            set: (S, A) -> S
        ): Lens<S, A> = object : Lens<S, A> {

            override fun get(s: S) = get(s)

            override fun set(s: S, b: A) = set(s, b)
        }
    }

    /**
     * @param B the modified focus of a [Lens]
     */
    infix fun <B> compose(other: Lens<A, B>): Lens<S, B> = object : Lens<S, B> {
        override fun get(s: S): B {
            return other.get(this@Lens.get(s))
        }

        override fun set(s: S, b: B): S {
            return this@Lens.set(s, other.set(this@Lens.get(s), b))
        }
    }
}