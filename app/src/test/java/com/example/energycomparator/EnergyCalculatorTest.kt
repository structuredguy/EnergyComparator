package com.example.energycomparator

import org.junit.Test
import kotlin.math.abs

class EnergyCalculatorTest {

    @Test
    fun testSolarInvestmentDecision() {
        println("--- 20-YEAR LIFETIME ANALYSIS (Capex: €5,000) ---")
        val yearlyUsage = 3600.0 // 300 kWh/month

        // CASE 1: TENERIFE
        printReport(EnergyCalculator.calculateInvestmentLogic(yearlyUsage, 28.4, "Tenerife"))

        println("\n------------------------------------------------\n")

        // CASE 2: LONDON
        printReport(EnergyCalculator.calculateInvestmentLogic(yearlyUsage, 51.5, "London"))
    }

    private fun printReport(report: SolarInvestmentReport) {
        println("📍 REPORT FOR: ${report.location}")

        println("1. BILLS:")
        println("   - Without Solar: €%.2f /year".format(report.annualBillNoSolar))
        println("   - With Solar:    €%.2f /year".format(report.annualBillWithSolar))
        println("   - Annual Saving: €%.2f /year".format(report.annualOperationalSavings))

        println("2. RETURN ON INVESTMENT:")
        if (report.breakEvenYears < 20) {
            println("   ✅ BREAK-EVEN: Year %.1f".format(report.breakEvenYears))
        } else {
            println("   ❌ NEVER breaks even (within 20 years)")
        }

        println("3. 20-YEAR TOTALS:")
        println("   - Cost NO Solar:   €%.2f".format(report.totalCost20YearsNoSolar))
        println("   - Cost WITH Solar: €%.2f".format(report.totalCost20YearsWithSolar))

        if (report.isProfitable) {
            println("   🎉 TOTAL PROFIT:   €%.2f".format(report.netSavings20Years))
        } else {
            println("   Warning: You lose €%.2f over 20 years".format(abs(report.netSavings20Years)))
        }
    }
}