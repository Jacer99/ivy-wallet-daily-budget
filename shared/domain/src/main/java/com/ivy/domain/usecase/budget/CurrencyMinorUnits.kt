package com.ivy.domain.usecase.budget

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility for converting between Ivy's major units (Double) and the budget engine's
 * minor units (Long).
 *
 * This ensures financial calculations are performed using exact integer arithmetic
 * to avoid floating-point errors.
 */
object CurrencyMinorUnits {

    /**
     * Returns the number of decimal places (exponent) for the given ISO 4217 currency.
     *
     * 3 -> TND, BHD, IQD, JOD, KWD, OMR, LYD
     * 0 -> JPY, KRW, VND, CLP, ISK
     * 2 -> Fallback for all other currencies.
     */
    fun exponentFor(currencyCode: String): Int {
        return when (currencyCode.uppercase()) {
            "TND", "BHD", "IQD", "JOD", "KWD", "OMR", "LYD" -> 3
            "JPY", "KRW", "VND", "CLP", "ISK" -> 0
            else -> 2
        }
    }

    /**
     * Converts a Double amount in the given currency to Long minor units.
     *
     * Uses HALF-UP rounding, which rounds ties away from zero for both positive
     * and negative amounts (e.g. 1.005 USD -> 101, -1.005 USD -> -101).
     */
    fun toMinorUnits(amount: Double, currencyCode: String): Long {
        val exponent = exponentFor(currencyCode)
        return BigDecimal.valueOf(amount)
            .setScale(exponent, RoundingMode.HALF_UP)
            .movePointRight(exponent)
            .toLong()
    }

    /**
     * Converts a Long minor-unit amount back to Double in the given currency.
     */
    fun toMajorUnits(minorUnits: Long, currencyCode: String): Double {
        val exponent = exponentFor(currencyCode)
        return BigDecimal.valueOf(minorUnits)
            .movePointLeft(exponent)
            .toDouble()
    }
}