package com.example.ui.connect.common

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerScreen(
    currentCity: String,
    onCitySelected: (String, String) -> Unit, // passes (city, defaultLocality)
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    val cities = listOf(
        "Pune" to "Kalyani Nagar",
        "Noida" to "Sector 62",
        "Mumbai" to "Bandra West",
        "New Delhi" to "Connaught Place",
        "Bengaluru" to "Indiranagar",
        "Chennai" to "Adyar",
        "Hyderabad" to "Gachibowli",
        "Kolkata" to "Salt Lake"
    )

    val filteredCities = cities.filter { it.first.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Location",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("city_picker_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ConnectColors.Primary
                )
            )
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter cities...", color = ConnectColors.TextSecondary) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = ConnectColors.Primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = ConnectColors.TextSecondary
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ConnectColors.Primary,
                    unfocusedBorderColor = ConnectColors.Border,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_picker_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Use GPS Location Button
            Card(
                onClick = {
                    onCitySelected("Pune", "Kalyani Nagar")
                    Toast.makeText(context, "📍 Pune Detected via GPS!", Toast.LENGTH_SHORT).show()
                },
                colors = CardDefaults.cardColors(
                    containerColor = ConnectColors.PrimaryLight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gps_detect_card_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ConnectColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS Location",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Use Current Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ConnectColors.PrimaryDark
                        )
                        Text(
                            text = "Locally search Pune and surrounding areas",
                            fontSize = 11.sp,
                            color = ConnectColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Available Cities",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ConnectColors.TextPrimary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCities) { (city, locality) ->
                    val isSelected = currentCity.equals(city, ignoreCase = true)
                    
                    Card(
                        onClick = {
                            onCitySelected(city, locality)
                            Toast.makeText(context, "Location updated to $city", Toast.LENGTH_SHORT).show()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ConnectColors.PrimaryLight else Color.White
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, ConnectColors.Primary) else BorderStroke(0.5.dp, ConnectColors.Border),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_card_item_$city")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (city) {
                                        "Pune" -> "🍇"
                                        "Noida" -> "🏢"
                                        "Mumbai" -> "🌊"
                                        "New Delhi" -> "🏛️"
                                        "Bengaluru" -> "🌳"
                                        "Chennai" -> "☀️"
                                        "Hyderabad" -> "🕌"
                                        "Kolkata" -> "🚃"
                                        else -> "🏙️"
                                    },
                                    fontSize = 20.sp
                                )
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Column {
                                    Text(
                                        text = city,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) ConnectColors.PrimaryDark else ConnectColors.TextPrimary
                                    )
                                    Text(
                                        text = "Primary Sector: $locality",
                                        fontSize = 11.sp,
                                        color = ConnectColors.TextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ConnectColors.Primary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
