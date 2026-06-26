package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.CrowmixElevation
import com.example.ui.theme.crowmixShadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppViewModel
import com.example.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabCategory by remember { mutableStateOf("All") }
    var isPrescriptionUploaded by remember { mutableStateOf(false) }

    val secondaryPurple = Color(0xFF8B5CF6)
    val accentGreen = Color(0xFF00C853)
    val headerBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6C2BD9), Color(0xFF8B5CF6))
    )

    // Healthcare sample medicines
    val medicines = listOf(
        ProductItem("med1", "Crocin Advance 650mg", "meds", 25.0, 30.0, "15 tablets", "💊", 0xFFFFF2F2, true),
        ProductItem("med2", "Volini Pain Relief Spray", "pain", 110.0, 135.0, "40g", "💨", 0xFFEBF5FB, false),
        ProductItem("med3", "Celin 500mg (Vitamin C)", "wellness", 38.0, 42.0, "15 tablets", "🍋", 0xFFFFFDE7, true),
        ProductItem("med4", "Otrivin Pediatric Spray", "meds", 84.0, 95.0, "10 ml", "👃", 0xFFEAF9EC, false),
        ProductItem("med5", "Strepsils Honey & Lemon", "wellness", 28.0, 32.0, "8 lozenges", "🍬", 0xFFFFF3D1, true),
        ProductItem("med6", "Revital H Capsules Daily", "wellness", 210.0, 250.0, "30 caps", "🔋", 0xFFEDE7F6, true),
        ProductItem("med7", "Dolo 650 Tablet", "meds", 22.0, 30.0, "15 tablets", "💊", 0xFFFFEBE3, true),
        ProductItem("med8", "Moov Pain Relief Balm", "pain", 72.0, 85.0, "25g", "🧴", 0xFFF5F5F5, false)
    )

    val categories = listOf("All", "Medicines", "Wellness", "Pain Relief")

    val filteredMedicines = medicines.filter { med ->
        val matchesCategory = when (selectedTabCategory) {
            "All" -> true
            "Medicines" -> med.category == "meds"
            "Wellness" -> med.category == "wellness"
            "Pain Relief" -> med.category == "pain"
            else -> true
        }
        val matchesSearch = med.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Apothecary & Medicines",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Delivering in 8 mins ⚡ • Licensed Pharmacy",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("medicines_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6C2BD9)
                ),
                modifier = Modifier.background(headerBrush)
            )
        },
        containerColor = Color(0xFFF8F9FC),
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Search Input Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBrush)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search prescription, OTC drugs, wellness items...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF6C2BD9)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("medicines_search_input")
                    )
                }
            }

            // Promotional Health banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 24.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Premium Doctor Consultation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Talk to licensed physicians instantly. Flat 15% discount on medicines via prescription.",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            // Prescription Upload Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    border = BorderStroke(1.5.dp, Color(0xFFDDD6FE))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Prescription",
                                tint = Color(0xFF6C2BD9),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Order via Prescription",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF6C2BD9)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Have a handwritten note or medical Rx prescription? Upload it, and our licensed pharmacist will verify & add items for you.",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                isPrescriptionUploaded = true
                                Toast.makeText(context, "✅ Prescription Uploaded Successfully!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPrescriptionUploaded) Color(0xFF00C853) else Color(0xFF6C2BD9)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("upload_prescription_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPrescriptionUploaded) {
                                    Icon(Icons.Default.Check, "Success", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Prescription Appended", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Upload Medical Prescription Rx", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Category Tab Filter Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedTabCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTabCategory = cat }
                                .testTag("med_filter_tab_$cat"),
                            color = if (isSelected) Color(0xFF6C2BD9) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF6C2BD9) else Color(0xFFEEF2F7)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF111827),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Grid of Healthcare Products
            items(filteredMedicines.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { med ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .crowmixShadow(
                                    elevation = CrowmixElevation.Low,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(Color(med.tintColorHex), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(med.labelChar, fontSize = 48.sp)

                                    if (med.discountPercent != null) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .background(Color(0xFF00C853), RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${med.discountPercent}% OFF",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = med.name,
                                    color = Color(0xFF111827),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = med.qtyUnit,
                                    color = Color(0xFF6B7280),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "₹${med.price.toInt()}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color(0xFF111827)
                                        )
                                        if (med.originalPrice != null) {
                                            Text(
                                                text = "₹${med.originalPrice.toInt()}",
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDecoration = TextDecoration.LineThrough
                                                ),
                                                color = Color(0xFF6B7280),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.addToCart(med.id)
                                            Toast.makeText(context, "${med.name} added to prescription cart!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C2BD9)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("add_med_${med.id}_btn")
                                    ) {
                                        Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ADD", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (pair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
