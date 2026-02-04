package com.example.energycomparator

import kotlin.math.abs

// 1. The Data Model
data class EnergyProvider(
    val name: String,
    val tariffName: String,
    val pricePerKwh: Double,
    val standingCharge: Double
)

// 2. The Logic Object
object EnergyCalculator {

    private val allProviders = listOf(
        EnergyProvider("Volt-Age", "Standard Saver", 0.14, 15.00),
        EnergyProvider("GreenSpark", "Eco-Friendly", 0.18, 10.00),
        EnergyProvider("PowerPlus", "Fixed 12 Months", 0.16, 12.50),
        EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00)
    )

    // Used by Screen 1 (List)
    fun getProvidersSortedByYearlyCost(usageKwh: Double): List<Pair<EnergyProvider, Double>> {
        return allProviders.map { provider ->
            val totalCost = (usageKwh * provider.pricePerKwh) + provider.standingCharge
            provider to totalCost
        }.sortedBy { it.second }
    }

    // Used by Screen 2 (Project Expenses)
    // This was the missing function causing your error!
    fun calculateAnnualSolarKwh(latitude: Double, systemSizeKw: Double = 3.0): Double {
        // Simple logic: closer to equator (0 lat) = more sun
        val peakSunHours = when (abs(latitude)) {
            in 0.0..25.0 -> 5.5  // Tropics (High potential)
            in 25.1..45.0 -> 4.5 // Temperate (Medium potential)
            in 45.1..60.0 -> 3.0 // High Latitude (Lower potential)
            else -> 2.0          // Low potential
        }
        // Formula: Hours/Day * 365 Days * System Size * Efficiency (0.75)
        return peakSunHours * 365 * systemSizeKw * 0.75
    }

    // Legacy function (kept for safety, though Screen 2 now calculates its own math)
    fun calculateSolarBreakEven(yearlyUsage: Double, latitude: Double, provider: EnergyProvider): Double {
        val systemCost = 5000.0
        val production = calculateAnnualSolarKwh(latitude)
        val annualSavings = production * provider.pricePerKwh
        return if (annualSavings > 0) systemCost / annualSavings else 999.0
    }
}