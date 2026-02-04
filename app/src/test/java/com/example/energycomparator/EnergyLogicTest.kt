package com.example.energycomparator

import org.junit.Test

class EnergyLogicTest {

    @Test
    fun testEnergyCalculator() {
        println("--- STARTING CONSOLE TEST ---")

        // Input for testing
        val testUsage = 300.0

        // CALL THE SHARED LOGIC
        // We are using the exact same code the app will use
        val results = EnergyCalculator.getCheapestQuotes(testUsage)

        // Print results
        println("Quotes for $testUsage kWh:")
        results.forEach { (provider, cost) ->
            val formattedCost = "%.2f".format(cost)
            println("- ${provider.name}: €$formattedCost (${provider.tariffName})")
        }

        println("--- TEST COMPLETE ---")
    }
    @Test
    fun testSolarCalculator() {
        println("--- SOLAR CALCULATION TEST ---")

        // Case 1: Tenerife, Spain (Lat ~28.4) - Should be "Temperate/High" tier
        val tenerifeLat = 28.4
        val tenerifeOutput = EnergyCalculator.calculateAnnualSolarKwh(tenerifeLat)
        println("Annual Production in Tenerife (Lat $tenerifeLat): ${tenerifeOutput.toInt()} kWh")

        // Case 2: London, UK (Lat ~51.5) - Should be "High Latitude" tier
        val londonLat = 51.5
        val londonOutput = EnergyCalculator.calculateAnnualSolarKwh(londonLat)
        println("Annual Production in London (Lat $londonLat): ${londonOutput.toInt()} kWh")

        // Assert logic (JUnit verification)
        // We expect Tenerife to produce MORE energy than London
        assert(tenerifeOutput > londonOutput) { "Error: Tenerife should have higher potential than London" }

        println("--- SOLAR TEST COMPLETE ---")
    }

    @Test
    fun testFullSolarSavings() {
        println("--- FULL INTEGRATION TEST: SOLAR SAVINGS ---")

        // 1. MOCK INPUTS
        val latitude = 28.4 // Tenerife
        val annualGridUsage = 3600.0
        val monthlyAvgUsage = annualGridUsage / 12 // 300 kWh/month

        println("📍 Location: Lat $latitude")
        println("🔌 Current Consumption: ${monthlyAvgUsage.toInt()} kWh/month")

        // 2. CALCULATE SOLAR PRODUCTION
        val annualSolarProduction = EnergyCalculator.calculateAnnualSolarKwh(latitude)
        val monthlySolarProduction = annualSolarProduction / 12

        println("☀️ Estimated Solar Production: ${monthlySolarProduction.toInt()} kWh/month (Avg)")

        // 3. CALCULATE "BEFORE" COSTS (100% Grid)
        // We get the cheapest quote for the full usage
        val quotesBefore = EnergyCalculator.getCheapestQuotes(monthlyAvgUsage)
        val bestProviderBefore = quotesBefore.first() // The cheapest one

        // 4. CALCULATE "AFTER" COSTS (Net Grid Usage)
        // If solar covers 200 kWh, we only buy 100 kWh from the grid.
        // We ensure we don't go below 0 (assuming no feed-in tariff for simplicity)
        val netGridUsage = (monthlyAvgUsage - monthlySolarProduction).coerceAtLeast(0.0)

        val quotesAfter = EnergyCalculator.getCheapestQuotes(netGridUsage)
        val bestProviderAfter = quotesAfter.first()

        // 5. CALCULATE SAVINGS
        val monthlySavings = bestProviderBefore.second - bestProviderAfter.second
        val annualSavings = monthlySavings * 12

        // 6. PRINT THE REPORT
        println("\n--- 💰 SAVINGS REPORT ---")
        println("Before Solar Bill: €%.2f /month (${bestProviderBefore.first.name})".format(bestProviderBefore.second))
        println("After Solar Bill:  €%.2f /month (${bestProviderAfter.first.name})".format(bestProviderAfter.second))
        println("------------------------------------------------")
        println("TOTAL SAVINGS:     €%.2f per year".format(annualSavings))

        if (netGridUsage == 0.0) {
            println("🎉 CONGRATULATIONS: You are effectively off-grid!")
        }
    }
}