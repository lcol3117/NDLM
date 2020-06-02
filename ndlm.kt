import java.awt.geom.Arc2D
import kotlin.math.*

fun main(args: Array<String>): Unit {
    val mydataInt: List<List<Int>> = listOf(listOf(1,2),listOf(3,2),listOf(3,4),listOf(3,3),listOf(4,3),listOf(2,2),listOf(7,9),listOf(8,9),listOf(10,10),listOf(10,9),listOf(9,8))
    val mydata: List<List<Double>> = mydataInt.map {
        it.map {
            x -> x.toDouble()
        }
    }
    val mymodel: ndlm = ndlm(mydata,20,null)
    mymodel.cluster(null)
    println(mymodel.linked(listOf(2.0,2.0),listOf(3.0,4.0)))
    println(mymodel.linked(listOf(8.0,9.0),listOf(10.0,10.0)))
    println(mymodel.linked(listOf(3.0,2.0),listOf(7.0,9.0)))
    return Unit
}

class ndlm(var alldata: List<List<Double>>?, val eta: Int, var cndt: List<Double>?) {
    fun cluster(newAlldata: List<List<Double>>?): List<Double> {
        alldata = alldata ?: (newAlldata!!)
        alldata!!
        val ndTransform: List<Double> = alldata!!.map(::getNDTransform)
        cndt = ndTransform.map {
            if (it == 0.0) 1.0 else (1.0 / it)
        }
        return cndt!!
    }

    private fun getNDTransform(item: List<Double>): Double {
        val closest: List<Double> = closestToSafe(item)
        return l2d(closest, item)
    }

    private fun l2d(a: List<Double>, b: List<Double>): Double {
        val sumd: Double = a.indices.toList().fold(0.0, { acc, x ->
            acc + (a[x] - b[x]).pow(2.0)
        })
        return sqrt(sumd)
    }

    private fun maxLinked(a: List<Double>, b: List<Double>, ndt: List<Double>): Boolean {
        val aNDT: Double = ndt[alldata!!.indexOf(a)]
        val bNDT: Double = ndt[alldata!!.indexOf(b)]
        val middleNDT: List<Double> = (0 until eta).toList().map {
            ndt[alldata!!.indexOf(closestTo(alongLine(a, b, (it / eta.toDouble()))))]
        }
        val meanNDMiddle: Double = middleNDT.fold(0.0, {
                acc, x -> acc + x
        }) / eta.toDouble()
        (if (meanNDMiddle == Double.POSITIVE_INFINITY) null else 0.0)!!
        val outerNDT: List<Double> = listOf(aNDT, bNDT)
        val minNDOuter: Double = outerNDT.fold(Double.POSITIVE_INFINITY, {
                acc, x -> if (x < acc) x else acc
        })
        (if (minNDOuter == Double.POSITIVE_INFINITY) null else 0.0)!!
        return minNDOuter < meanNDMiddle
    }

    private fun closestToSafe(point: List<Double>): List<Double> {
        val worst: List<Double> = listOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        val closest: List<Double> = alldata!!.fold(point, {
                acc, x ->
                if (l2d(x, point) == 0.0) worst else (if (l2d(x, point) < l2d(acc, point)) x else acc)
        })
        (if (closest == point) null else 0.0)!!
        val real: List<Double> = (if (closest == worst) alldata!![alldata!!.size - 1] else closest)
        return real
    }

    private fun closestTo(point: List<Double>): List<Double> {
        val worst: List<Double> = listOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        val closest: List<Double> = alldata!!.fold(alldata!![0], {
                acc, x ->
            if (l2d(x, point) == 0.0) worst else (if (l2d(x, point) < l2d(acc, point)) x else acc)
        })
        (if (closest == point) null else 0.0)!!
        val real: List<Double> = (if (closest == worst) alldata!![alldata!!.size - 1] else closest)
        return real
    }

    private fun alongLine(a: List<Double>, b: List<Double>, d: Double): List<Double> {
        val totalDeltas: List<Double> = a.indices.toList().map {
            b[it] - a[it]
        }
        val newDeltas: List<Double> = totalDeltas.map {
            it * d
        }
        val xpoint: List<Double> = a.indices.toList().map {
            a[it] + newDeltas[it]
        }
        return xpoint
    }

    fun linked(a: List<Double>, b: List<Double>): Boolean {
        cndt!!
        return maxLinked(a, b, cndt!!)
    }
}
