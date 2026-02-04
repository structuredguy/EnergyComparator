package com.example.energycomparator

// 1. The Data Model
data class EnergyProvider(
    val name: String,
    val tariffName: String,
    val pricePerKwh: Double,
    val standingCharge: Double
)

// 2. The Logic Object (Singleton)
object EnergyCalculator {

    // We keep the data private so only this object manages it
    private val allProviders = listOf(
        EnergyProvider("Volt-Age", "Standard Saver", 0.14, 15.00),
        EnergyProvider("GreenSpark", "Eco-Friendly", 0.18, 10.00),
        EnergyProvider("PowerPlus", "Fixed 12 Months", 0.16, 12.50),
        EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00)
    )

    fun getCheapestQuotes(usageKwh: Double): List<Pair<EnergyProvider, Double>> {
        return allProviders.map { provider ->
            val totalCost = (usageKwh * provider.pricePerKwh) + provider.standingCharge
            provider to totalCost
        }.sortedBy { it.second }
    }

    // Inside object EnergyCalculator { ... }

    /**
     * Estimates annual solar production in kWh.
     * @param latitude The user's latitude (e.g., 28.4 for Tenerife, 51.5 for London)
     * @param systemSizeKw Size of the solar panel system (default 3.0 kW for a typical home)
     */
    fun calculateAnnualSolarKwh(latitude: Double, systemSizeKw: Double = 3.0): Double {
        // 1. Estimate Peak Sun Hours (PSH) per day based on latitude tiers
        // In reality, this depends on clouds/weather, but latitude is the baseline.
        val peakSunHours = when (Math.abs(latitude)) {
            in 0.0..25.0 -> 5.5  // Tropics (High potential)
            in 25.1..45.0 -> 4.5 // Temperate (Medium potential)
            in 45.1..60.0 -> 3.0 // High Latitude (Lower potential)
            else -> 2.0          // Polar regions
        }

        // 2. Efficiency factor (0.8) accounts for system losses (heat, cables, inverter)
        val systemEfficiency = 0.8

        // 3. The Math: PSH * Days * Size * Efficiency
        return peakSunHours * 365 * systemSizeKw * systemEfficiency
    }
}