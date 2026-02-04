package com.example.energycomparator

import kotlin.math.abs

// 1. DATA MODELS
data class EnergyProvider(
    val name: String,
    val tariffName: String,
    val pricePerKwh: Double,
    val monthlyStandingCharge: Double // Explicitly named "Monthly" to avoid math errors
)

// 2. LOGIC SINGLETON
object EnergyCalculator {

    private val allProviders = listOf(
        EnergyProvider("Volt-Age", "Standard Saver", 0.14, 15.00),
        EnergyProvider("GreenSpark", "Eco-Friendly", 0.18, 10.00),
        EnergyProvider("PowerPlus", "Fixed 12 Months", 0.16, 12.50),
        EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00)
    )

    // --- SCREEN 1 LOGIC: CALCULATOR ---
    // Input: Yearly Usage -> Output: List of Providers with YEARLY cost
    fun getProvidersSortedByYearlyCost(yearlyUsageKwh: Double): List<Pair<EnergyProvider, Double>> {
        return allProviders.map { provider ->
            // Math: (YearlyKwh * Rate) + (12 * MonthlyCharge)
            val energyCost = yearlyUsageKwh * provider.pricePerKwh
            val fixedCost = provider.monthlyStandingCharge * 12
            val totalYearlyCost = energyCost + fixedCost

            provider to totalYearlyCost
        }.sortedBy { it.second } // Cheapest first
    }

    // --- SCREEN 2 LOGIC: SOLAR BREAK-EVEN ---
    // Input: Usage, Lat, and the Provider selected in Screen 1
    fun calculateSolarBreakEven(
        yearlyUsageKwh: Double,
        latitude: Double,
        provider: EnergyProvider
    ): Double {
        val solarCost = 5000.0

        // 1. Calculate Bill WITHOUT Solar (Status Quo)
        val oldEnergyCost = yearlyUsageKwh * provider.pricePerKwh
        val fixedCost = provider.monthlyStandingCharge * 12
        val oldBillYearly = oldEnergyCost + fixedCost

        // 2. Calculate Solar Production
        val peakSunHours = when (abs(latitude)) {
            in 0.0..25.0 -> 5.5
            in 25.1..45.0 -> 4.5
            in 45.1..60.0 -> 3.0
            else -> 2.0
        }
        // Formula: PSH * 365 * 3kW system * 0.8 efficiency
        val annualSolarGeneration = peakSunHours * 365 * 3.0 * 0.8

        // 3. Calculate Bill WITH Solar
        // Net usage cannot be less than 0
        val newNetUsage = (yearlyUsageKwh - annualSolarGeneration).coerceAtLeast(0.0)

        val newEnergyCost = newNetUsage * provider.pricePerKwh
        // Standing charge remains the same (you are still connected to grid)
        val newBillYearly = newEnergyCost + fixedCost

        // 4. Calculate Break-Even
        val annualSavings = oldBillYearly - newBillYearly

        return if (annualSavings > 0) {
            solarCost / annualSavings
        } else {
            999.0 // Impossible (infinite years)
        }
    }
}