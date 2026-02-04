package com.example.energycomparator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnergyApp()
        }
    }
}

// --- MAIN NAVIGATION CONTROLLER ---
@Composable
fun EnergyApp() {
    // Simple State to handle navigation
    var currentScreen by remember { mutableStateOf("screen1") }
    var yearlyUsageInput by remember { mutableStateOf("3600") } // Default value
    var selectedProvider by remember { mutableStateOf<EnergyProvider?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (currentScreen) {
                "screen1" -> {
                    ProviderListScreen(
                        initialUsage = yearlyUsageInput,
                        onUsageChanged = { yearlyUsageInput = it },
                        onProviderSelected = { provider ->
                            selectedProvider = provider
                            currentScreen = "screen2"
                        }
                    )
                }
                "screen2" -> {
                    if (selectedProvider != null) {
                        SolarCalculatorScreen(
                            provider = selectedProvider!!,
                            yearlyUsage = yearlyUsageInput.toDoubleOrNull() ?: 0.0,
                            onBack = { currentScreen = "screen1" }
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 1: LISTA DE PROVEEDORES ---
@Composable
fun ProviderListScreen(
    initialUsage: String,
    onUsageChanged: (String) -> Unit,
    onProviderSelected: (EnergyProvider) -> Unit
) {
    var resultList by remember { mutableStateOf(emptyList<Pair<EnergyProvider, Double>>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "⚡ Comparador de Energía",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = initialUsage,
            onValueChange = onUsageChanged,
            label = { Text("Consumo Anual (kWh)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val usage = initialUsage.toDoubleOrNull() ?: 0.0
                // Call our logic
                resultList = EnergyCalculator.getProvidersSortedByYearlyCost(usage)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔍 BUSCAR MEJOR TARIFA")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (resultList.isNotEmpty()) {
            Text(text = "Resultados (Toca para calcular solar):", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(resultList) { (provider, annualCost) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onProviderSelected(provider) }, // Navigate on click
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = provider.name, fontWeight = FontWeight.Bold)
                                Text(text = provider.tariffName, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "%.0f €/año".format(annualCost),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: CALCULADORA SOLAR ---
@Composable
fun SolarCalculatorScreen(
    provider: EnergyProvider,
    yearlyUsage: Double,
    onBack: () -> Unit
) {
    var latitude by remember { mutableStateOf("28.4") } // Default Tenerife
    var breakEvenYears by remember { mutableStateOf(0.0) }
    var hasCalculated by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onBack) { Text("⬅ Volver") }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "☀️ Rentabilidad Solar", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card about current situation
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light Orange
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Proveedor Seleccionado: ${provider.name}")
                Text("Tarifa: ${provider.tariffName}")
                Text("Consumo: ${yearlyUsage.toInt()} kWh/año")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitud (ej. 28.4 Tenerife)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val lat = latitude.toDoubleOrNull() ?: 0.0
                // Call logic
                breakEvenYears = EnergyCalculator.calculateSolarBreakEven(yearlyUsage, lat, provider)
                hasCalculated = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)) // Solar Orange
        ) {
            Text("CALCULAR AMORTIZACIÓN")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasCalculated) {
            Text("Resultado del Análisis:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (breakEvenYears < 20) {
                Text(
                    text = "✅ AMORTIZADO EN %.1f AÑOS".format(breakEvenYears),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32) // Green
                )
                Text("Después de ${String.format("%.1f", breakEvenYears)} años, ¡la electricidad es casi gratis!")
            } else {
                Text(
                    text = "❌ NO RENTABLE (> 20 años)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text("Con tu consumo actual, los paneles no se pagan solos.")
            }
        }
    }
}

// --- PREVIEW GENERATOR (The "PNG") ---
// Use the "Design" tab in Android Studio to see this WITHOUT running the app.
@Preview(showBackground = true, name = "1. Lista de Proveedores")
@Composable
fun PreviewScreen1() {
    ProviderListScreen(initialUsage = "3600", onUsageChanged = {}, onProviderSelected = {})
}

@Preview(showBackground = true, name = "2. Calculadora Solar")
@Composable
fun PreviewScreen2() {
    val mockProvider = EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00)
    SolarCalculatorScreen(provider = mockProvider, yearlyUsage = 3600.0, onBack = {})
}