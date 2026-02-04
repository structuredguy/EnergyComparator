package com.example.energycomparator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORS ---
val SolarOrange = Color(0xFFF57C00)
val BackgroundColor = Color(0xFFFAFAFA)
val RankBest = Color(0xFF43A047)   // Green
val RankMid = Color(0xFFFFB300)    // Amber
val RankWorst = Color(0xFFE53935)  // Red

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
    // Navigation State
    var currentScreen by remember { mutableStateOf("screen1") }

    // Data State (Held here to persist between screens)
    var yearlyUsageInput by remember { mutableStateOf("3600") }
    var solarBudgetInput by remember { mutableStateOf("5000") }
    var latitudeInput by remember { mutableStateOf("28.4") }
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
                        yearlyUsage = yearlyUsageInput,
                        solarBudget = solarBudgetInput,
                        latitude = latitudeInput,
                        selectedProvider = selectedProvider,
                        onUsageChange = { yearlyUsageInput = it },
                        onBudgetChange = { solarBudgetInput = it },
                        onLatitudeChange = { latitudeInput = it },
                        onProviderSelect = { selectedProvider = it },
                        onNavigateNext = { currentScreen = "screen2" }
                    )
                }
                "screen2" -> {
                    if (selectedProvider != null) {
                        SolarResultScreen(
                            provider = selectedProvider!!,
                            yearlyUsage = yearlyUsageInput.toDoubleOrNull() ?: 0.0,
                            solarBudget = solarBudgetInput.toDoubleOrNull() ?: 5000.0,
                            latitude = latitudeInput.toDoubleOrNull() ?: 28.4,
                            onBack = { currentScreen = "screen1" }
                        )
                    }
                }
            }
        }
    }
}

// --- SHARED FOOTER ---
@Composable
fun AppFooter() {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:structuredguy@gmail.com")
                }
                try { context.startActivity(intent) } catch (e: Exception) {}
            }
    ) {
        Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 8.dp))
        Text("Contact the developer: structuredguy@gmail.com", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text("Credits to Gemini", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

// ------------------------------------------------------------------------
// SCREEN 1: PROVIDER SELECTION & PROJECT SETUP
// ------------------------------------------------------------------------
@Composable
fun ProviderListScreen(
    yearlyUsage: String,
    solarBudget: String,
    latitude: String,
    selectedProvider: EnergyProvider?,
    onUsageChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onProviderSelect: (EnergyProvider) -> Unit,
    onNavigateNext: () -> Unit
) {
    var resultList by remember { mutableStateOf(emptyList<Pair<EnergyProvider, Double>>()) }
    var searchedUsage by remember { mutableStateOf("") }

    // Use a LazyColumn for the whole screen so it scrolls if keyboard opens
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // 1. HEADER & USAGE INPUT
        item {
            Text("⚡ Comparador de Energía", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = yearlyUsage,
                        onValueChange = onUsageChange,
                        label = { Text("Consumo Anual (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val usage = yearlyUsage.toDoubleOrNull() ?: 0.0
                            searchedUsage = yearlyUsage
                            resultList = EnergyCalculator.getProvidersSortedByYearlyCost(usage)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolarOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BUSCAR TARIFA", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. PROVIDER LIST (Only if results exist)
        if (resultList.isNotEmpty()) {
            item {
                Text(
                    text = "Selecciona un proveedor:",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val maxCost = resultList.maxOf { it.second }
            val itemCount = resultList.size

            itemsIndexed(resultList) { index, (provider, annualCost) ->
                val barColor = when {
                    index == 0 -> RankBest
                    index == itemCount - 1 -> RankWorst
                    else -> RankMid
                }

                SelectableProviderItem(
                    provider = provider,
                    cost = annualCost,
                    maxCost = maxCost,
                    barColor = barColor,
                    isSelected = selectedProvider == provider,
                    onSelect = { onProviderSelect(provider) }
                )
            }

            // 3. SOLAR PROJECT INPUTS (Only appears after search)
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Divider()
                Spacer(modifier = Modifier.height(20.dp))

                Text("Configuración Solar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Solar Budget Input
                    OutlinedTextField(
                        value = solarBudget,
                        onValueChange = onBudgetChange,
                        label = { Text("Presupuesto Solar (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    // Latitude Input
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = onLatitudeChange,
                        label = { Text("Latitud") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. ACTION BUTTON
                Button(
                    onClick = onNavigateNext,
                    enabled = selectedProvider != null, // Only active if checkmark selected
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarOrange,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("PROJECT EXPENSES ➔", fontWeight = FontWeight.Bold)
                }

                if (selectedProvider == null) {
                    Text(
                        " * Selecciona un proveedor arriba para continuar",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                AppFooter()
            }
        } else {
            // Footer when no results yet
            item { AppFooter() }
        }
    }
}

@Composable
fun SelectableProviderItem(
    provider: EnergyProvider,
    cost: Double,
    maxCost: Double,
    barColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val widthFraction = (cost / maxCost).toFloat()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() } // Click row to select
    ) {
        // THE CHECKMARK (Radio Button)
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = SolarOrange)
        )

        // THE CHART
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = provider.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = provider.tariffName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Text(
                    text = "€${cost.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

// ------------------------------------------------------------------------
// SCREEN 2: PROJECT EXPENSES DASHBOARD (Result Only)
// ------------------------------------------------------------------------
@Composable
fun SolarResultScreen(
    provider: EnergyProvider,
    yearlyUsage: Double,
    solarBudget: Double,
    latitude: Double,
    onBack: () -> Unit
) {
    // Perform Calculation immediately
    // Note: We need a slight modification to the break-even logic to accept variable budget.
    // Since EnergyCalculator logic is hidden in this file, I will replicate the simple math here
    // or assume we use the standard logic and divide by the custom budget.

    // Standard logic from previous: (Cost / AnnualSavings)
    // We calculate Annual Savings first.

    val annualSolarKwh = EnergyCalculator.calculateAnnualSolarKwh(latitude, 3.0) // Assume 3kW system for now
    val annualSavings = annualSolarKwh * provider.pricePerKwh

    // Break Even = UserBudget / Savings
    val breakEvenYears = if (annualSavings > 0) solarBudget / annualSavings else 999.0
    val totalSavings20Years = (annualSavings * 20) - solarBudget

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // TOP BAR
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Project Expenses", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. RECAP CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen del Proyecto", fontWeight = FontWeight.Bold, color = SolarOrange)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                RowResult("Proveedor", provider.name)
                RowResult("Inversión (Presupuesto)", "€${solarBudget.toInt()}")
                RowResult("Ubicación (Lat)", latitude.toString())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. THE BIG RESULT
        Text("Resultados Financieros", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (breakEvenYears < 10) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                if (breakEvenYears < 20) {
                    Text("✅ AMORTIZACIÓN", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        text = "%.1f Años".format(breakEvenYears),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (breakEvenYears < 8) RankBest else RankMid
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ahorro Neto (20 Años)", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        text = "+€${totalSavings20Years.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = RankBest
                    )
                } else {
                    Text("❌ NO RENTABLE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = RankWorst)
                    Text("El retorno de inversión supera los 20 años.")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        AppFooter()
    }
}

@Composable
fun RowResult(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

// ------------------------------------------------------------------------
// PREVIEW
// ------------------------------------------------------------------------
@Preview(showBackground = true, name = "Screen 1: Selection")
@Composable
fun PreviewSelection() {
    val mockResults = listOf(
        EnergyProvider("BudgetEnergy", "No Frills", 0.13, 20.00) to 708.0,
        EnergyProvider("Volt-Age", "Standard", 0.14, 15.00) to 850.0
    )
    MaterialTheme {
        // We simulate a screen with results populated
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            itemsIndexed(mockResults) { index, (prov, cost) ->
                SelectableProviderItem(prov, cost, 1000.0, RankBest, index == 0, {})
            }
        }
    }
}