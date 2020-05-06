package it.unibo.acdingnet.protelis.util

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator

object Interpolator {

    private val splineTempByPollution = SplineInterpolator().interpolate(
        listOf(1.0, 25.0, 50.0, 65.0, 85.0, 100.0).toDoubleArray(), // pollution value
        listOf(25.0, 24.0, 22.0, 20.0, 18.0, 17.0).toDoubleArray() // temperature
    )
    fun interpolateTempByPollution(value: Double) = splineTempByPollution.value(value)
}
