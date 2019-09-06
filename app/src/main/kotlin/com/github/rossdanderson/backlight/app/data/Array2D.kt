package com.github.rossdanderson.backlight.app.data

data class Array2D<T>(
    private val array: Array<Array<T>>
) {
    val xSize: Int = array.size
    val ySize: Int = if (xSize == 0) 0 else array[0].size

    companion object {
        inline operator fun <reified T> invoke() =
            Array2D(Array(0) { emptyArray<T>() })

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
            Array2D(Array(xWidth) { arrayOfNulls<T>(yWidth) })

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Array2D<T> =
            Array2D(Array(xWidth) { x -> Array(yWidth) { y -> operator(x, y) } })
    }

    operator fun get(x: Int, y: Int): T = array[x][y]

    operator fun set(x: Int, y: Int, t: T) {
        array[x][y] = t
    }

    fun forEach(operation: (T) -> Unit) {
        array.forEach { innerArray -> innerArray.forEach { t -> operation.invoke(t) } }
    }

    fun forEachIndexed(operation: (x: Int, y: Int, T) -> Unit) {
        array.forEachIndexed { x, innerArray -> innerArray.forEachIndexed { y, t -> operation.invoke(x, y, t) } }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Array2D<*>

        if (!array.contentDeepEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int = array.contentDeepHashCode()
}
