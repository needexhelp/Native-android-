package com.example.ui.partners

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.CrowmixElevation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.ui.partners.common.PanelColors
import com.example.ui.partners.common.PartnerKeys
import com.example.ui.partners.common.PartnerType
import com.example.ui.partners.common.partnerDataStore
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerRegistrationScreen(
    partnerType: PartnerType,
    onRegistrationComplete: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Extras based on partner kind
    var extra1 by remember { mutableStateOf("") } // e.g. Vehicle Type/Model, License etc
    var extra2 by remember { mutableStateOf("") } // e.g. License Plate, Specialty etc

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register — ${partnerType.displayName}", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reg_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PanelColors.Teal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Low)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Partner Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PanelColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name / Business Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PanelColors.Teal,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PanelColors.Teal,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PanelColors.Teal,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (PIN)") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PanelColors.Teal,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Conditional fields based on partner types
                    when (partnerType) {
                        PartnerType.DELIVERY_BOY, PartnerType.TAXI, PartnerType.AUTO, PartnerType.AMBULANCE -> {
                            OutlinedTextField(
                                value = extra1,
                                onValueChange = { extra1 = it },
                                label = { Text("Vehicle Type / Model") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PanelColors.Teal)
                            )
                            OutlinedTextField(
                                value = extra2,
                                onValueChange = { extra2 = it },
                                label = { Text("Vehicle Plate Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PanelColors.Teal)
                            )
                        }
                        PartnerType.MEDICINE_SHOP, PartnerType.RESTAURANT -> {
                            OutlinedTextField(
                                value = extra1,
                                onValueChange = { extra1 = it },
                                label = { Text("Business License or Tax ID") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PanelColors.Teal)
                            )
                        }
                        PartnerType.HOSPITAL -> {
                            OutlinedTextField(
                                value = extra1,
                                onValueChange = { extra1 = it },
                                label = { Text("Medical Specialty / Dept") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PanelColors.Teal)
                            )
                            OutlinedTextField(
                                value = extra2,
                                onValueChange = { extra2 = it },
                                label = { Text("Hospital Registration Code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PanelColors.Teal)
                            )
                        }
                        PartnerType.BUS_AGENCY -> {
                            OutlinedTextField(
                                value = extra1,
                                onValueChange = { extra1 = it },
                                label = { Text("Agency License Permit") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PanelColors.Teal)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill in Name, Phone and Password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // Process sign-up, generate a clean identity code
                            val randomCodeNum = Random.nextInt(1000, 9999)
                            val identityCode = "${partnerType.codePrefix}$randomCodeNum"

                            scope.launch {
                                context.partnerDataStore.edit { prefs ->
                                    prefs[PartnerKeys.partnerName(partnerType)] = name
                                    prefs[PartnerKeys.partnerPhone(partnerType)] = phone
                                    prefs[PartnerKeys.passwordHash(partnerType)] = password // simple plain-text password for template
                                    prefs[PartnerKeys.identityCode(partnerType)] = identityCode
                                    prefs[PartnerKeys.isRegistered(partnerType)] = true
                                }
                                Toast.makeText(context, "Registration successful! ID: $identityCode", Toast.LENGTH_LONG).show()
                                onRegistrationComplete()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reg_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = PanelColors.Teal)
                    ) {
                        Text("REGISTER & START", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
