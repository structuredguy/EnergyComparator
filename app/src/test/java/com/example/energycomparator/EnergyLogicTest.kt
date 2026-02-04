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
}