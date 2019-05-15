package com.github.rossdanderson.backlight

class Array2D<T>(
    val array: Array<Array<T>>
) {
    val xSize = array.size
    val ySize = if (xSize == 0) 0 else array[0].size

    companion object {

        inline operator fun <reified T> invoke() = Array2D(Array(0) { emptyArray<T>() })

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
            Array2D(Array(xWidth) { arrayOfNulls<T>(yWidth) })

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Array2D<T> {
            val array = Array(xWidth) {
                val x = it
                Array(yWidth) { y -> operator(x, y) }
            }
            return Array2D(array)
        }
    }

    operator fun get(x: Int, y: Int): T {
        return array[x][y]
    }

    operator fun set(x: Int, y: Int, t: T) {
        array[x][y] = t
    }

    inline fun forEach(operation: (T) -> Unit) {
        array.forEach { innerArray -> innerArray.forEach { t -> operation.invoke(t) } }
    }

    inline fun forEachIndexed(operation: (x: Int, y: Int, T) -> Unit) {
        array.forEachIndexed { x, innerArray -> innerArray.forEachIndexed { y, t -> operation.invoke(x, y, t) } }
    }
}

/**
 * 'from' is inclusive, 'to' is exclusive
 */
inline fun <reified T> Array2D<T>.sliceArray2D(xIndices: IntRange, yIndices: IntRange): Array2D<T> =
    Array2D(xIndices.last - xIndices.first, yIndices.last - yIndices.first) { xIndex, yIndex ->
        val x = xIndex + xIndices.first
        val y = yIndex + yIndices.first
        this[x, y]
    }