package com.github.rossdanderson.backlight

class Array2D<T>(
    val array: Array<Array<T>>
) {
    val xSize: Int = array.size
    val ySize: Int = if (xSize == 0) 0 else array[0].size

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