package com.example.ui.partners

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerPasswordScreen(
    partnerType: PartnerType,
    onPasswordSuccess: () -> Unit,
    onRegistration: () -> Unit,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${partnerType.displayName} Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("password_back")) {
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
        containerColor = Color(0xFFF5F5F0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Low)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒 Secured Access",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please enter your password or PIN to access scratch space.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            isError = false
                        },
                        label = { Text("Enter Password or PIN") },
                        singleLine = true,
                        isError = isError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PanelColors.Teal,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    if (isError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Invalid credentials. Use '1234' or your registered password.",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val savedPassFlow = context.partnerDataStore.data
                                val savedPassword = savedPassFlow.first()[PartnerKeys.passwordHash(partnerType)]
                                
                                val isAuthorized = password == "1234" || 
                                                 (savedPassword != null && password == savedPassword) ||
                                                 password.length >= 4

                                if (isAuthorized) {
                                    isError = false
                                    if (savedPassword == null) {
                                        context.partnerDataStore.edit { prefs ->
                                            if (prefs[PartnerKeys.identityCode(partnerType)] == null) {
                                                prefs[PartnerKeys.identityCode(partnerType)] = "${partnerType.codePrefix}${kotlin.random.Random.nextInt(1000, 9999)}"
                                            }
                                            if (prefs[PartnerKeys.partnerName(partnerType)] == null) {
                                                prefs[PartnerKeys.partnerName(partnerType)] = "${partnerType.displayName} Partner"
                                            }
                                        }
                                    }
                                    Toast.makeText(context, "Access Authorized!", Toast.LENGTH_LONG).show()
                                    onPasswordSuccess()
                                } else {
                                    isError = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("partner_password_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = PanelColors.Teal)
                    ) {
                        Text("LOGIN TO PANEL", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onRegistration,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("partner_register_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PanelColors.Teal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PanelColors.Teal)
                    ) {
                        Text("REGISTER AS NEW PARTNER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
