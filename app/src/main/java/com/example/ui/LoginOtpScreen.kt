package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginOtpScreen(onOtpVerified: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    val blue = Color(0xFF1800AD)
    val blueDark = Color(0xFF0D006A)
    val blueLight = Color(0xFFEFF6FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1800AD))
    ) {
        // Top branding area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Crow", color = Color(0xFFFFFFFF), fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                Text("m", color = Color(0xFF0BDD14), fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                Text("i", color = Color(0xFFFFC107), fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                Text("x", color = Color(0xFFFF3B30), fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Everything. Delivered Fast.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom white card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column {
                if (phase == 1) {
                    Text(
                        "Enter your mobile number",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "We'll send a secure OTP to verify your number",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it },
                        placeholder = { Text("10-digit mobile number") },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            ) {
                                Text("🇮🇳", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+91", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blue,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { if (phone.length == 10) phase = 2 },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(blueDark, blue))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Send OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                } else {
                    Text(
                        "Verify OTP",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Enter the 6-digit code sent to +91 $phone",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        placeholder = { Text("6-digit OTP") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = blue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blue,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { if (otp.length == 6) onOtpVerified(phone) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(blueDark, blue))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Verify & Enter App", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { phase = 1 }, modifier = Modifier.fillMaxWidth()) {
                        Text("← Change Number", color = blue, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "By continuing, you agree to our Terms of Service & Privacy Policy",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
