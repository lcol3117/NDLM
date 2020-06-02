import kotlin.math.*

fun main(args: Array<String>): Unit {
    val cluster1: List<List<Double>> = listOf(listOf(1.0,2.0),listOf(3.0,4.0))
    val cluster2: List<List<Double>> = listOf(listOf(6.0,7.0),listOf(8.0,9.0))
    val mydata: List<List<Double>> = cluster1 + cluster2
    val mymodel: ndlm = ndlm(mydata,20)
    val myresults: List<List<List<Double>>> = mymodel.cluster(null)
    println(myresults)
    return Unit
}

class ndlm(var alldata: List<List<Double>>?, val eta: Int) {
    fun cluster(newAlldata: List<List<Double>>?): List<List<List<Double>>> {
        alldata = alldata ?: (newAlldata!!)
        alldata!!
        val ndTransform: List<Double> = alldata!!.map(::getNDTransform)
        return linkMaxima(ndTransform)
    }

    private fun getNDTransform(item: List<Double>): Double {
        val closest: List<Double> = closestTo(item)
        return l2d(closest, item)
    }

    private fun l2d(a: List<Double>, b: List<Double>): Double {
        val sumd: Double = a.indices.toList().fold(0.0, { acc, x ->
            acc + (a[x] - b[x]).pow(2.0)
        })
        return sqrt(sumd)
    }

    private fun linkMaxima(ndt: List<Double>): List<List<List<Double>>> {
        var result: MutableList<MutableList<List<Double>>> =
                mutableListOf(mutableListOf())
        var tested: MutableList<List<Double>> = mutableListOf()
        for (i in alldata!!) {
            val newCluster: Boolean = !(tested.fold(false, {
                acc, x -> if (maxLinked(i,x,ndt)) true else acc
            }))
            if (newCluster) {
                result.add(mutableListOf(i))
            } else {
                for ((j, k) in result.withIndex()) {
                    val inCluster: Boolean = k.fold(false, { acc, x ->
                        if (maxLinked(i, x, ndt)) true else acc
                    })
                    if (inCluster) {
                        result[j].add(i)
                    }
                }
            }
        }
        return result
    }

    private fun maxLinked(a: List<Double>, b: List<Double>, ndt: List<Double>): Boolean {
        val aNDT: Double = ndt[alldata!!.indexOf(a)]
        val bNDT: Double = ndt[alldata!!.indexOf(b)]
        val middleNDT: List<Double> = (0 until eta).toList().map {
            ndt[alldata!!.indexOf(closestTo(alongLine(a, b, (it / eta.toDouble()))))]
        }
        val minNDMiddle: Double = middleNDT.fold(Double.POSITIVE_INFINITY, {
            acc, x -> if (x < acc) x else acc
        })
        (if (minNDMiddle == Double.POSITIVE_INFINITY) null else 0.0)!!
        val outerNDT: List<Double> = listOf(aNDT, bNDT)
        val minNDOuter: Double = outerNDT.fold(Double.POSITIVE_INFINITY, {
            acc, x -> if (x < acc) x else acc
        })
        (if (minNDOuter == Double.POSITIVE_INFINITY) null else 0.0)!!
        return minNDOuter < minNDMiddle
    }

    private fun closestTo(point: List<Double>): List<Double> {
        return alldata!!.fold(point, {
            acc, x -> if (l2d(x, point) < l2d(acc, point)) x else acc
        })
    }

    private fun alongLine(a: List<Double>, b: List<Double>, d: Double): List<Double> {
        val totalDeltas: List<Double> = a.indices.toList().map {
            b[it] - a[it]
        }
        val newDeltas: List<Double> = totalDeltas.map {
            it * d
        }
        return a.indices.toList().map {
            a[it] + newDeltas[it]
        }
    }
}
