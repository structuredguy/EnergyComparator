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
}