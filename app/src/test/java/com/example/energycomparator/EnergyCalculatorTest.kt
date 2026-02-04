package com.example.energycomparator

import org.junit.Test

class EnergyCalculatorTest {

    @Test
    fun testSolarInvestmentDecision() {
        println("--- 5-YEAR INVESTMENT ANALYSIS (Cost: €5,000) ---")
        val yearlyUsage = 3600.0 // 300 kWh/month

        // CASE 1: TENERIFE (High Sun)
        // We expect this to be PROFITABLE because 4.5+ sun hours is huge
        val reportTenerife = EnergyCalculator.calculateInvestmentLogic(yearlyUsage, 28.4, "Tenerife")
        printReport(reportTenerife)

        println("\n------------------------------------------------\n")

        // CASE 2: LONDON (Low Sun)
        // We expect this to be LESS PROFITABLE or a LOSS
        val reportLondon = EnergyCalculator.calculateInvestmentLogic(yearlyUsage, 51.5, "London")
        printReport(reportLondon)
    }

    private fun printReport(report: SolarInvestmentReport) {
        println("📍 REPORT FOR: ${report.location}")

        println("1. NO SOLAR Strategy:")
        println("   - Best Provider: ${report.bestProviderNoSolar.name}")
        println("   - Annual Cost:   €%.2f".format(report.annualBillNoSolar))

        println("2. SOLAR Strategy (Investment €1,000/yr):")
        println("   - Best Provider: ${report.bestProviderWithSolar.name}")
        println("   - Utility Bill:  €%.2f /yr".format(report.annualBillWithSolar))
        println("   - Solar Amort.:  €%.2f /yr".format(report.annualSolarCost))
        println("   - Total Annual:  €%.2f".format(report.totalAnnualCostWithSolar))

        println("3. VERDICT:")
        if (report.isProfitable) {
            println("   ✅ YES! SAVE MONEY.")
            println("   - You save €%.2f per year".format(report.annualNetSavings))
            println("   - 5-Year Profit: €%.2f".format(report.annualNetSavings * 5))
        } else {
            println("   ❌ NO. LOSE MONEY.")
            println("   - You lose €%.2f per year".format(Math.abs(report.annualNetSavings)))
            println("   - The panels cost more than the electricity savings.")
        }
    }
}