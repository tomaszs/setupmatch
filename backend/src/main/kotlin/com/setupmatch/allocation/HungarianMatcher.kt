package com.setupmatch.allocation

/**
 * Maximum-weight bipartite matching (Hungarian / Kuhn-Munkres).
 * Returns column assigned to each row, or -1 if unmatched.
 *
 * Rectangular case (few rows, many columns) runs in O(rows² × cols) instead of O(cols³).
 */
object HungarianMatcher {

    fun maxWeightAssignment(weights: Array<DoubleArray>): IntArray {
        val nRows = weights.size
        if (nRows == 0) {
            return intArrayOf()
        }
        val nCols = weights[0].size
        if (nCols == 0) {
            return IntArray(nRows) { -1 }
        }

        val cost = Array(nRows) { row ->
            DoubleArray(nCols) { col -> -weights[row][col] }
        }

        val assignment = if (nRows <= nCols) {
            hungarianMinCostRectangular(cost, nRows, nCols)
        } else {
            hungarianMinCostSquare(padToSquare(cost, nRows, nCols))
        }

        return IntArray(nRows) { row ->
            val col = assignment[row]
            if (col < 0 || col >= nCols) -1 else col
        }
    }

    private fun padToSquare(cost: Array<DoubleArray>, nRows: Int, nCols: Int): Array<DoubleArray> {
        val n = maxOf(nRows, nCols)
        return Array(n) { row ->
            DoubleArray(n) { col ->
                when {
                    row < nRows && col < nCols -> cost[row][col]
                    else -> 0.0
                }
            }
        }
    }

    /** Workers ≤ jobs: O(rows² × cols). */
    private fun hungarianMinCostRectangular(cost: Array<DoubleArray>, nRows: Int, nCols: Int): IntArray {
        val u = DoubleArray(nRows + 1)
        val v = DoubleArray(nCols + 1)
        val p = IntArray(nCols + 1)
        val way = IntArray(nCols + 1)

        for (i in 1..nRows) {
            p[0] = i
            var j0 = 0
            val minv = DoubleArray(nCols + 1) { Double.POSITIVE_INFINITY }
            val used = BooleanArray(nCols + 1)

            do {
                used[j0] = true
                val i0 = p[j0]
                var delta = Double.POSITIVE_INFINITY
                var j1 = 0
                for (j in 1..nCols) {
                    if (used[j]) {
                        continue
                    }
                    val cur = cost[i0 - 1][j - 1] - u[i0] - v[j]
                    if (cur < minv[j]) {
                        minv[j] = cur
                        way[j] = j0
                    }
                    if (minv[j] < delta) {
                        delta = minv[j]
                        j1 = j
                    }
                }
                for (j in 0..nCols) {
                    if (used[j]) {
                        u[p[j]] += delta
                        v[j] -= delta
                    } else {
                        minv[j] -= delta
                    }
                }
                j0 = j1
            } while (p[j0] != 0)

            do {
                val j1 = way[j0]
                p[j0] = p[j1]
                j0 = j1
            } while (j0 != 0)
        }

        val answer = IntArray(nRows) { -1 }
        for (j in 1..nCols) {
            if (p[j] > 0) {
                answer[p[j] - 1] = j - 1
            }
        }

        return answer
    }

    private fun hungarianMinCostSquare(cost: Array<DoubleArray>): IntArray {
        val n = cost.size
        val u = DoubleArray(n + 1)
        val v = DoubleArray(n + 1)
        val p = IntArray(n + 1)
        val way = IntArray(n + 1)

        for (i in 1..n) {
            p[0] = i
            var j0 = 0
            val minv = DoubleArray(n + 1) { Double.POSITIVE_INFINITY }
            val used = BooleanArray(n + 1)

            do {
                used[j0] = true
                val i0 = p[j0]
                var delta = Double.POSITIVE_INFINITY
                var j1 = 0
                for (j in 1..n) {
                    if (used[j]) {
                        continue
                    }
                    val cur = cost[i0 - 1][j - 1] - u[i0] - v[j]
                    if (cur < minv[j]) {
                        minv[j] = cur
                        way[j] = j0
                    }
                    if (minv[j] < delta) {
                        delta = minv[j]
                        j1 = j
                    }
                }
                for (j in 0..n) {
                    if (used[j]) {
                        u[p[j]] += delta
                        v[j] -= delta
                    } else {
                        minv[j] -= delta
                    }
                }
                j0 = j1
            } while (p[j0] != 0)

            do {
                val j1 = way[j0]
                p[j0] = p[j1]
                j0 = j1
            } while (j0 != 0)
        }

        val answer = IntArray(n) { -1 }
        for (j in 1..n) {
            if (p[j] > 0) {
                answer[p[j] - 1] = j - 1
            }
        }

        return answer
    }
}
