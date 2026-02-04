package com.example.energycomparator

import org.junit.Test
import org.junit.Assert.*

class EnergyCalculatorTest {

    // --- TEST SCREEN 1: PROVIDER CALCULATOR ---
    @Test
    fun testScreen1_ProviderRanking() {
        println("--- SCREEN 1 TEST: Provider Ranking ---")
        val yearlyUsage = 3600.0 // 300 kWh/month

        // Action: Get sorted list
        val results = EnergyCalculator.getProvidersSortedByYearlyCost(yearlyUsage)
        val bestOption = results.first()

        // Verify Output
        println("Input: $yearlyUsage kWh/year")
        println("Best Provider: ${bestOption.first.name} at €%.2f/year".format(bestOption.second))

        // Assertions (Logic Checks)
        // 1. Ensure list is actually sorted (Cost A < Cost B)
        assertTrue(results[0].second < results[1].second)

        // 2. Manual Math Check for 'BudgetEnergy' (0.13/kWh + 20.00/mo)
        // (3600 * 0.13) + (20 * 12) = 468 + 240 = 708.0
        // Let's verify if the code gets it right
        val budgetEnergyQuote = results.find { it.first.name == "BudgetEnergy" }
        assertEquals(708.0, budgetEnergyQuote?.second!!, 0.01)

        println("✅ Screen 1 Logic Verified")
    }

    // --- TEST SCREEN 2: SOLAR CALCULATOR ---
    @Test
    fun testScreen2_SolarBreakEven() {
        println("\n--- SCREEN 2 TEST: Solar Break-Even ---")
        val yearlyUsage = 3600.0

        // Setup: Assume user picked "BudgetEnergy" from Screen 1
        val selectedProvider = EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00)

        // CASE A: Tenerife (High Sun)
        val yearsTenerife = EnergyCalculator.calculateSolarBreakEven(yearlyUsage, 28.4, selectedProvider)
        println("📍 Tenerife (Lat 28.4): Break-even in %.1f years".format(yearsTenerife))

        // CASE B: London (Low Sun)
        val yearsLondon = EnergyCalculator.calculateSolarBreakEven(yearlyUsage, 51.5, selectedProvider)
        println("📍 London (Lat 51.5):   Break-even in %.1f years".format(yearsLondon))

        // Assertions
        // Tenerife should pay off FASTER than London
        assertTrue("Tenerife should be faster than London", yearsTenerife < yearsLondon)

        // Tenerife (approx 4800kWh solar vs 3600 usage) means 0 energy cost.
        // Savings = OldEnergyCost (3600*0.13 = €468).
        // Investment = 5000.
        // Expected Years = 5000 / 468 = ~10.6 years.
        assertEquals(10.68, yearsTenerife, 0.5) // allowing small margin for float math

        println("✅ Screen 2 Logic Verified")
    }
}