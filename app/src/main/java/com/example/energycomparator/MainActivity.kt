package com.example.energycomparator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORS ---
val SolarOrange = Color(0xFFF57C00) // The "Action" color
val ChartBarColor = Color(0xFF4CAF50) // Green for the bars
val BackgroundColor = Color(0xFFFAFAFA) // Very light grey background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnergyApp()
        }
    }
}

@Composable
fun EnergyApp() {
    var currentScreen by remember { mutableStateOf("screen1") }
    var yearlyUsageInput by remember { mutableStateOf("3600") }
    var selectedProvider by remember { mutableStateOf<EnergyProvider?>(null) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = SolarOrange,
            background = BackgroundColor
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = BackgroundColor) {
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

// ------------------------------------------------------------------------
// SCREEN 1: PROVIDER CALCULATOR (With Bar Chart)
// ------------------------------------------------------------------------
@Composable
fun ProviderListScreen(
    initialUsage: String,
    onUsageChanged: (String) -> Unit,
    onProviderSelected: (EnergyProvider) -> Unit
) {
    // State to hold the list. If empty, we haven't searched yet.
    var resultList by remember { mutableStateOf(emptyList<Pair<EnergyProvider, Double>>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        // HEADER
        Text(
            text = "⚡ Comparador de Energía",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(24.dp))

        // INPUT SECTION
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = initialUsage,
                    onValueChange = onUsageChanged,
                    label = { Text("Consumo Anual (kWh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // THE "ACTION" BUTTON (Orange)
                Button(
                    onClick = {
                        val usage = initialUsage.toDoubleOrNull() ?: 0.0
                        resultList = EnergyCalculator.getProvidersSortedByYearlyCost(usage)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SolarOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("BUSCAR MEJOR TARIFA", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OUTPUT SECTION: BAR CHART
        if (resultList.isNotEmpty()) {
            Text("Resultados (Elige uno):", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Calculate max cost for relative bar width
            val maxCost = resultList.maxOf { it.second }

            LazyColumn {
                items(resultList) { (provider, annualCost) ->
                    ProviderBarChartItem(
                        provider = provider,
                        cost = annualCost,
                        maxCost = maxCost,
                        onClick = { onProviderSelected(provider) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderBarChartItem(
    provider: EnergyProvider,
    cost: Double,
    maxCost: Double,
    onClick: () -> Unit
) {
    // Determine bar width relative to the screen width (max cost = 100% width)
    val widthFraction = (cost / maxCost).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        // Label Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = provider.name, fontWeight = FontWeight.Bold)
                Text(text = provider.tariffName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = "€${cost.toInt()}",
                fontWeight = FontWeight.Bold,
                color = if (cost == maxCost) Color.Black else ChartBarColor // Highlight cheapest in green logic later?
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // The Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(12.dp)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(if (widthFraction < 0.99) ChartBarColor else Color.LightGray)
            // Logic: If it's not the most expensive, make it Green. Expensive = Grey.
        )
    }
}

// ------------------------------------------------------------------------
// SCREEN 2: SOLAR SIMULATOR (Modern "Back" & Consistent Button)
// ------------------------------------------------------------------------
@Composable
fun SolarCalculatorScreen(
    provider: EnergyProvider,
    yearlyUsage: Double,
    onBack: () -> Unit
) {
    var latitude by remember { mutableStateOf("28.4") }
    var breakEvenYears by remember { mutableStateOf(0.0) }
    var hasCalculated by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        // MODERN TOP BAR
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulador Solar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SUMMARY CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Light Blue for info
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Proveedor Actual", style = MaterialTheme.typography.labelSmall)
                    Text(provider.name, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Consumo", style = MaterialTheme.typography.labelSmall)
                    Text("${yearlyUsage.toInt()} kWh", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // INPUT
        Text("Ubicación", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitud (ej. 28.4)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ACTION BUTTON (Consistent Orange)
        Button(
            onClick = {
                val lat = latitude.toDoubleOrNull() ?: 0.0
                breakEvenYears = EnergyCalculator.calculateSolarBreakEven(yearlyUsage, lat, provider)
                hasCalculated = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SolarOrange),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("CALCULAR AMORTIZACIÓN", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // RESULT OUTPUT
        if (hasCalculated) {
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("RESULTADO FINAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            if (breakEvenYears < 20) {
                Text(
                    text = "✅ RENTABLE",
                    fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Amortizado en %.1f años".format(breakEvenYears),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("En 20 años habrás ahorrado miles de euros.", color = Color.Gray)
            } else {
                Text(
                    text = "❌ NO RENTABLE",
                    fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Red
                )
                Text("Tardarías más de 20 años en recuperar la inversión.")
            }
        }
    }
}

// ------------------------------------------------------------------------
// VISUAL PREVIEWS (The "PNGs")
// ------------------------------------------------------------------------

@Preview(showBackground = true, name = "Screen 1: Output Chart")
@Composable
fun PreviewScreen1_Output() {
    // We create a specific component just for previewing the output state
    val mockResults = listOf(
        EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00) to 708.0,
        EnergyProvider("Volt-Age", "Standard", 0.14, 15.00) to 750.0,
        EnergyProvider("GreenSpark", "Eco", 0.18, 10.00) to 850.0
    )

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp).background(Color.White)) {
            Text("Resultados Mock (Visual Check):")
            val max = mockResults.maxOf { it.second }
            mockResults.forEach {
                ProviderBarChartItem(it.first, it.second, max, {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Screen 2: Solar Result")
@Composable
fun PreviewScreen2_Output() {
    MaterialTheme {
        // We force the screen to look like calculation is done
        val mockProvider = EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00)
        SolarCalculatorScreen(mockProvider, 3600.0, {})
        // Note: The preview logic for 'hasCalculated' is internal state, 
        // so to see the result in preview, you'd usually pass state in.
        // For now, just trust the 'Run' or click the Interactive button in Preview!
    }
}
