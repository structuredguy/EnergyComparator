package com.example.energycomparator

import kotlin.math.abs

// 1. DATA MODELS
data class EnergyProvider(
    val name: String,
    val tariffName: String,
    val pricePerKwh: Double,
    val standingCharge: Double
)

// NEW: The Report for the Investment Decision
data class SolarInvestmentReport(
    val location: String,
    val bestProviderNoSolar: EnergyProvider,
    val annualBillNoSolar: Double,
    val bestProviderWithSolar: EnergyProvider,
    val annualBillWithSolar: Double, // The utility bill only
    val annualSolarCost: Double,     // The investment (e.g. €1000/yr)
    val totalAnnualCostWithSolar: Double, // Bill + Investment
    val annualNetSavings: Double,    // Positive = Profit, Negative = Loss
    val isProfitable: Boolean        // The boolean for your UI "Verdict"
)

// 2. LOGIC SINGLETON
object EnergyCalculator {

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

    fun calculateAnnualSolarKwh(latitude: Double, systemSizeKw: Double = 3.0): Double {
        val peakSunHours = when (abs(latitude)) {
            in 0.0..25.0 -> 5.5
            in 25.1..45.0 -> 4.5
            in 45.1..60.0 -> 3.0
            else -> 2.0
        }
        val systemEfficiency = 0.8
        return peakSunHours * 365 * systemSizeKw * systemEfficiency
    }

    // NEW: The "ROI" Decision Logic
    fun calculateInvestmentLogic(
        usageKwhPerYear: Double,
        latitude: Double,
        locationName: String
    ): SolarInvestmentReport {

        // Scenario 1: NO SOLAR (Status Quo)
        // We calculate cost based on average monthly usage
        val avgMonthlyUsage = usageKwhPerYear / 12
        val bestNoSolarQuote = getCheapestQuotes(avgMonthlyUsage).first()
        val annualBillNoSolar = bestNoSolarQuote.second * 12

        // Scenario 2: WITH SOLAR
        val annualProduction = calculateAnnualSolarKwh(latitude)
        val monthlyProduction = annualProduction / 12

        // Net usage: We buy less from the grid. (coerceAtLeast(0.0) ensures no negative bills)
        val netMonthlyUsage = (avgMonthlyUsage - monthlyProduction).coerceAtLeast(0.0)

        val bestWithSolarQuote = getCheapestQuotes(netMonthlyUsage).first()
        val annualUtilityBill = bestWithSolarQuote.second * 12

        // Investment Cost: €5,000 spread over 5 years = €1,000/year
        val annualInvestmentCost = 1000.0
        val totalCostWithSolar = annualUtilityBill + annualInvestmentCost

        // The Verdict
        val savings = annualBillNoSolar - totalCostWithSolar

        return SolarInvestmentReport(
            location = locationName,
            bestProviderNoSolar = bestNoSolarQuote.first,
            annualBillNoSolar = annualBillNoSolar,
            bestProviderWithSolar = bestWithSolarQuote.first,
            annualBillWithSolar = annualUtilityBill,
            annualSolarCost = annualInvestmentCost,
            totalAnnualCostWithSolar = totalCostWithSolar,
            annualNetSavings = savings,
            isProfitable = savings > 0
        )
    }
}