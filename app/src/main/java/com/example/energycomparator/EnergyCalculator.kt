package com.example.energycomparator

import kotlin.math.abs
import kotlin.math.ceil

// 1. DATA MODELS
data class EnergyProvider(
    val name: String,
    val tariffName: String,
    val pricePerKwh: Double,
    val standingCharge: Double
)

data class SolarInvestmentReport(
    val location: String,
    // Scenario A: No Solar
    val bestProviderNoSolar: EnergyProvider,
    val annualBillNoSolar: Double,
    val totalCost20YearsNoSolar: Double, // The "Do Nothing" cost

    // Scenario B: Solar
    val bestProviderWithSolar: EnergyProvider,
    val annualBillWithSolar: Double,     // The reduced utility bill
    val solarSystemCost: Double,         // Fixed €5,000
    val totalCost20YearsWithSolar: Double, // (Bill * 20) + €5,000

    // The Verdict
    val annualOperationalSavings: Double, // How much lower the bill is per year
    val breakEvenYears: Double,           // When do you get your money back?
    val netSavings20Years: Double,        // Final profit after 20 years
    val isProfitable: Boolean             // True if you break even before year 20
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

    // NEW: 20-Year Horizon Logic
    fun calculateInvestmentLogic(
        usageKwhPerYear: Double,
        latitude: Double,
        locationName: String
    ): SolarInvestmentReport {

        // 1. SCENARIO A: DO NOTHING (Grid only)
        val avgMonthlyUsage = usageKwhPerYear / 12
        val bestNoSolarQuote = getCheapestQuotes(avgMonthlyUsage).first()
        val annualBillNoSolar = bestNoSolarQuote.second * 12
        val totalCost20YearsNoSolar = annualBillNoSolar * 20

        // 2. SCENARIO B: BUY SOLAR (€5,000)
        val annualProduction = calculateAnnualSolarKwh(latitude)
        val monthlyProduction = annualProduction / 12
        val netMonthlyUsage = (avgMonthlyUsage - monthlyProduction).coerceAtLeast(0.0)

        val bestWithSolarQuote = getCheapestQuotes(netMonthlyUsage).first()
        val annualBillWithSolar = bestWithSolarQuote.second * 12

        val solarSystemCost = 5000.0
        // Total cost = The panels + 20 years of reduced bills
        val totalCost20YearsWithSolar = solarSystemCost + (annualBillWithSolar * 20)

        // 3. ANALYSIS
        val annualOperationalSavings = annualBillNoSolar - annualBillWithSolar

        // Avoid division by zero if savings are 0 or negative
        val breakEvenYears = if (annualOperationalSavings > 0) {
            solarSystemCost / annualOperationalSavings
        } else {
            999.0 // Never breaks even
        }

        val netSavings20Years = totalCost20YearsNoSolar - totalCost20YearsWithSolar

        return SolarInvestmentReport(
            location = locationName,
            bestProviderNoSolar = bestNoSolarQuote.first,
            annualBillNoSolar = annualBillNoSolar,
            totalCost20YearsNoSolar = totalCost20YearsNoSolar,
            bestProviderWithSolar = bestWithSolarQuote.first,
            annualBillWithSolar = annualBillWithSolar,
            solarSystemCost = solarSystemCost,
            totalCost20YearsWithSolar = totalCost20YearsWithSolar,
            annualOperationalSavings = annualOperationalSavings,
            breakEvenYears = breakEvenYears,
            netSavings20Years = netSavings20Years,
            isProfitable = netSavings20Years > 0
        )
    }
}