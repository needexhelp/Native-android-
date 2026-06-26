package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.CrowmixElevation
import com.example.ui.theme.crowmixShadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val myContext = LocalContext.current
                val sharedPrefs = remember(myContext) { myContext.getSharedPreferences("crowmix_prefs", android.content.Context.MODE_PRIVATE) }
                var isLoggedIn by remember { mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false)) }
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(3000)
                    showSplash = false
                }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 800),
                    label = "SplashToMain"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen()
                    } else {
                        if (!isLoggedIn) {
                            LoginOtpScreen(
                                onOtpVerified = { phone ->
                                    sharedPrefs.edit()
                                        .putBoolean("is_logged_in", true)
                                        .putString("verified_phone_number", phone)
                                        .apply()
                                    isLoggedIn = true
                                }
                            )
                        } else {
                            val isOrderPlaced by viewModel.isOrderPlaced.collectAsState()
                            val activeTab by viewModel.activeTab.collectAsState()
                            val activeScreen by viewModel.activeScreen.collectAsState()
                            val unreadCount by viewModel.unreadCount.collectAsState()

                // Automatic Standard Android Permissions Requester on Startup / New User
                val context = LocalContext.current
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { perms ->
                    val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (fine || coarse) {
                        viewModel.setGpsDetected(true)
                    }
                    val camera = perms[Manifest.permission.CAMERA] == true
                    val record = perms[Manifest.permission.RECORD_AUDIO] == true
                    if (camera && record) {
                        viewModel.setCameraMicApproved(true)
                    }
                    val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms[Manifest.permission.POST_NOTIFICATIONS] == true
                    } else true
                    if (notifications) {
                        viewModel.setBackgroundEngineOn(true)
                    }
                }

                LaunchedEffect(Unit) {
                    val requiredPermissions = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    
                    val permissionsToRequest = requiredPermissions.filter {
                        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                    }
                    
                    if (permissionsToRequest.isNotEmpty()) {
                        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    } else {
                        viewModel.setGpsDetected(true)
                        viewModel.setCameraMicApproved(true)
                        viewModel.setBackgroundEngineOn(true)
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .statusBarsPadding(),
                    containerColor = Color(0xFFF7F9FC),
                    bottomBar = {
                        val showCartForBottomBar by viewModel.showCart.collectAsState()
                        if (!isOrderPlaced && !showCartForBottomBar && activeScreen == ActiveScreen.MAIN) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .crowmixShadow(elevation = CrowmixElevation.High, shape = RoundedCornerShape(28.dp))
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val tabs = listOf(
                                        Triple(AppTab.HOME, "Home", Icons.Default.Home),
                                        Triple(AppTab.ORDERS, "Orders", Icons.Default.ListAlt),
                                        Triple(AppTab.SERVICES, "Services", Icons.Default.GridView),
                                        Triple(AppTab.NOTIFY, "Alerts", Icons.Default.Notifications),
                                        Triple(AppTab.NEWS, "News", Icons.Default.Newspaper)
                                    )
                                    tabs.forEach { (tab, label, icon) ->
                                        val isSelected = activeScreen == ActiveScreen.MAIN && activeTab == tab
                                        Column(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    viewModel.navigateTo(ActiveScreen.MAIN)
                                                    viewModel.setActiveTab(tab)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected)
                                                            Brush.horizontalGradient(listOf(Color(0xFF1A3BBF), Color(0xFF2563EB)))
                                                        else
                                                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color(0xFF1A56DB) else Color(0xFF9CA3AF)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    val showCart by viewModel.showCart.collectAsState()
                    val mode by viewModel.currentMode.collectAsState()
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        AnimatedContent(
                            targetState = isOrderPlaced to activeScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "MainAppNavigation"
                        ) { (placed, screen) ->
                            if (placed) {
                                OrderTrackingScreen(viewModel = viewModel)
                            } else {
                                when (screen) {
                                    ActiveScreen.MAIN -> {
                                        when (activeTab) {
                                            AppTab.HOME -> MainDashboardScreen(viewModel = viewModel)
                                            AppTab.ORDERS -> com.example.ui.OrdersScreen(viewModel = viewModel)
                                            AppTab.SERVICES -> {
                                                Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    colors = listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB))
                                                                )
                                                            )
                                                            .padding(horizontal = 16.dp, vertical = 16.dp)
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = "All Services",
                                                                color = Color.White,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 22.sp
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "Grocery • Food • Medicine • Transport & more",
                                                                color = Color.White.copy(alpha = 0.75f),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                    com.example.ui.ServicesGrid(
                                                        viewModel = viewModel,
                                                        columns = 3,
                                                        onServiceClick = { s -> viewModel.navigateTo(s) }
                                                    )
                                                }
                                            }
                                            AppTab.NOTIFY -> com.example.ui.NotificationsScreen(viewModel = viewModel)
                                            AppTab.NEWS -> com.example.ui.NewsScreen(viewModel = viewModel)
                                        }
                                    }
                                    ActiveScreen.FOOD -> com.example.ui.FoodOrderScreen(viewModel = viewModel, onBack = { viewModel.navigateBack() })
                                    ActiveScreen.MEDICINE -> com.example.ui.MedicineScreen(viewModel = viewModel, onBack = { viewModel.navigateBack() })
                                    ActiveScreen.TAXI -> com.example.ui.TaxiAutoScreen(onBack = { viewModel.navigateBack() })
                                    ActiveScreen.AMBULANCE -> com.example.ui.AmbulanceScreen(onBack = { viewModel.navigateBack() })
                                    ActiveScreen.HOSPITAL -> com.example.ui.HospitalBookingScreen(onBack = { viewModel.navigateBack() }, onGoHome = { viewModel.navigateTo(ActiveScreen.MAIN); viewModel.setActiveTab(AppTab.HOME) })
                                    ActiveScreen.PRINT -> com.example.ui.PrintDocsScreen(onBack = { viewModel.navigateBack() }, onGoHome = { viewModel.navigateTo(ActiveScreen.MAIN); viewModel.setActiveTab(AppTab.HOME) })
                                    ActiveScreen.BUS -> com.example.ui.BusTicketScreen(onBack = { viewModel.navigateBack() }, onGoHome = { viewModel.navigateTo(ActiveScreen.MAIN); viewModel.setActiveTab(AppTab.HOME) })
                                    ActiveScreen.PROFILE -> com.example.ui.ProfileScreen(onBack = { viewModel.navigateBack() })
                                    ActiveScreen.DOXA_AI -> com.example.ui.DoxaAIScreen(onBack = { viewModel.navigateBack() })
                                }
                            }
                        }

                        // --- GLOBAL VIEW CART FLOATING BAR ---
                        val cartSubtotal = viewModel.getCartSubtotal()
                        if (cartSubtotal > 0 && !isOrderPlaced && activeScreen != ActiveScreen.DOXA_AI && activeScreen != ActiveScreen.PROFILE) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(bottom = if (activeScreen == ActiveScreen.MAIN) 0.dp else 16.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0x990F172A), Color(0xFF0F172A))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .crowmixShadow(
                                            elevation = CrowmixElevation.High,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.setShowCart(true) }
                                        .testTag("floating_cart_bar"),
                                    colors = CardDefaults.cardColors(containerColor = if (mode == CommerceMode.ZEPTO) Color(0xFF1A56DB) else Color(0xFF0C8346)),
                                    shape = RoundedCornerShape(20.dp), // Corner radius of 20px (20dp) on the view cart button bar!
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingBag,
                                                contentDescription = "Cart",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${viewModel.getCartCount()} Item(s)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "₹${cartSubtotal} • Free delivery applied",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "View Cart",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Proceed",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    com.example.ui.ProductDetailsBottomSheet(viewModel = viewModel)

                    // --- GLOBAL DIALOG SHEET FOR CART ---
                    if (showCart) {
                        CartDrawerSheet(
                            viewModel = viewModel,
                            onDismiss = { viewModel.setShowCart(false) },
                            brandPrimary = getBrandPrimaryColor(mode),
                            brandAccent = getBrandAccentColor(mode),
                            mode = mode
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

// --- COLOR SELECTORS TIED TO MODE ---
@Composable
fun getBrandPrimaryColor(mode: CommerceMode): Color = Color(0xFF1A56DB)

@Composable
fun getBrandAccentColor(mode: CommerceMode): Color = Color(0xFF2563EB)

@Composable
fun getBrandHeaderBrush(mode: CommerceMode): Brush {
    return if (mode == CommerceMode.ZEPTO) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB), Color(0xFF2563EB))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFD414), Color(0xFFFFE054), Color(0xFFFFF19B))
        )
    }
}

@Composable
fun getBrandTextHeaderColor(mode: CommerceMode): Color {
    return if (mode == CommerceMode.ZEPTO) Color.White else Color(0xFF1E293B)
}

// --- 1. MAIN DASHBOARD SCREEN ---
@Composable
fun MainDashboardScreen(viewModel: AppViewModel) {
    val mode by viewModel.currentMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchActive by viewModel.searchActive.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val deliveryAddress by viewModel.deliveryAddress.collectAsState()

    var showAddressDialog by remember { mutableStateOf(false) }

    val brandPrimary = getBrandPrimaryColor(mode)
    val brandAccent = getBrandAccentColor(mode)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HEADER
            var headerVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { headerVisible = true }
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                Column {
                    // Delivery row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delivering to",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Home  ▾",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showAddressDialog = true }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // Profile icon button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { viewModel.navigateTo(ActiveScreen.PROFILE) }
                                .testTag("profile_icon_header"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = deliveryAddress.ifEmpty { "Pimpri, Pune 411018" },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showAddressDialog = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Search Bar with DOXA AI Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.75f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable { viewModel.setSearchActive(true) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    androidx.compose.ui.text.buildAnnotatedString {
                                        append("Search for ")
                                        pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold))
                                        append("\"AUTO\"")
                                        pop()
                                    }.let { annotatedText ->
                                        Text(
                                            text = annotatedText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Assistant",
                                        tint = Color(0xFF06B6D4),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Scanner",
                                        tint = Color(0xFF4B5563),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.25f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEFF1F4), RoundedCornerShape(20.dp))
                                .clickable { viewModel.navigateTo(ActiveScreen.DOXA_AI) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DOXA AI",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            }

            // Body Area with dynamic overlays
            Box(modifier = Modifier.weight(1f)) {
                if (searchActive) {
                    SearchOverlay(
                        viewModel = viewModel,
                        brandPrimary = brandPrimary,
                        brandAccent = brandAccent
                    )
                } else {
                    HomeFeedContent(
                        viewModel = viewModel,
                        selectedCategory = selectedCategory,
                        brandPrimary = brandPrimary,
                        brandAccent = brandAccent,
                        mode = mode
                    )
                }
            }
        }
    }

    // --- ADDRESS EDIT DIALOG ---
    if (showAddressDialog) {
        var tempAddr by remember { mutableStateOf(deliveryAddress) }
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Deliver to New Location", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = tempAddr,
                    onValueChange = { tempAddr = it },
                    label = { Text("Delivery Address Details") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandPrimary,
                        unfocusedBorderColor = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("address_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDeliveryAddress(tempAddr)
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPrimary)
                ) {
                    Text("Update Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel", color = Color(0xFF687280))
                }
            }
        )
    }
}

// --- SUB-WIDGET: DYNAMIC SEARCH FIELD BUTTON ---
@Composable
fun CustomSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onActiveStateChanged: (Boolean) -> Unit,
    mode: CommerceMode,
    viewModel: AppViewModel
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val isCamMicApproved by viewModel.isCameraMicApproved.collectAsState()

    var showScanner by remember { mutableStateOf(false) }
    var showVoice by remember { mutableStateOf(false) }

    // Multi-permissions launcher
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val cam = perms[Manifest.permission.CAMERA] == true
        val mic = perms[Manifest.permission.RECORD_AUDIO] == true
        if (cam && mic) {
            viewModel.setCameraMicApproved(true)
            Toast.makeText(context, "🎙️📷 approved: Voice Search & Courier Scanner Ready!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone & Camera permission is highly recommended.", Toast.LENGTH_LONG).show()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Bar container
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEFF1F4), RoundedCornerShape(24.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onActiveStateChanged(true)
                }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF1A56DB), // Premium Purple
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = {
                        onQueryChanged(it)
                        onActiveStateChanged(it.isNotEmpty() || true)
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF111827),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar_input")
                        .focusable()
                )

                if (query.isEmpty()) {
                    Text(
                        text = "Search groceries, food, medicines, taxis...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = Color(0xFF687280),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            onQueryChanged("")
                            onActiveStateChanged(false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                        .testTag("search_clear_btn")
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isCamMicApproved) {
                                    showVoice = true
                                } else {
                                    permLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                    )
                                }
                            }
                            .testTag("voice_search_trigger"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Search",
                            tint = Color(0xFF1A56DB),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isCamMicApproved) {
                                    showScanner = true
                                } else {
                                    permLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                    )
                                }
                            }
                            .testTag("qr_scanner_trigger"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Scanner",
                            tint = Color(0xFF1A56DB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // --- HIGH FIDELITY VOICE RECOGNITION POPUP ---
    if (showVoice) {
        var voiceStatus by remember { mutableStateOf("Listening to ambient voice input...") }
        var audioLevel1 by remember { mutableStateOf(1.0f) }
        var audioLevel2 by remember { mutableStateOf(1.2f) }
        var audioLevel3 by remember { mutableStateOf(0.8f) }

        LaunchedEffect(Unit) {
            // Simulate voice waveforms bumping
            while (voiceStatus.startsWith("Listening")) {
                delay(300)
                audioLevel1 = Random.nextFloat() * (1.8f - 0.4f) + 0.4f
                audioLevel2 = Random.nextFloat() * (1.8f - 0.4f) + 0.4f
                audioLevel3 = Random.nextFloat() * (1.8f - 0.4f) + 0.4f
            }
        }

        LaunchedEffect(Unit) {
            delay(1800)
            voiceStatus = "Recognizing voice waves..."
            delay(1200)
            voiceStatus = "Match Found: 'Hybrid Banana 🍌'"
            delay(800)
            onQueryChanged("Banana")
            onActiveStateChanged(true)
            showVoice = false
            Toast.makeText(context, "Voice Voice detected: Hybrid Banana", Toast.LENGTH_SHORT).show()
        }

        AlertDialog(
            onDismissRequest = { showVoice = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🗣️ Speaking Search Assistant", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = voiceStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Pulse Audio Waveforms simulation in canvas
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(60.dp)
                    ) {
                        listOf(audioLevel1, audioLevel2, audioLevel3, audioLevel1 * 0.8f, audioLevel2 * 1.2f).forEach { multiplier ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(30.dp * multiplier)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(getBrandPrimaryColor(mode))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Speak clearly like 'Fresh onion', 'milk', 'chips'.",
                        fontSize = 11.sp,
                        color = Color(0xFF687280)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoice = false }) {
                    Text("Close Microphone", color = Color.Red)
                }
            }
        )
    }

    // --- HIGH FIDELITY CAMERA QR BARCODE SCANNER ---
    if (showScanner) {
        var scannerStatus by remember { mutableStateOf("Initializing Courier Drone Lenses...") }
        var laserPositionY by remember { mutableStateOf(0.0f) }

        LaunchedEffect(Unit) {
            // Animate laser scanner line
            while (scannerStatus.startsWith("Initializing") || scannerStatus.startsWith("Scanning")) {
                laserPositionY = 0.0f
                delay(12)
                laserPositionY = 1.0f
                delay(800)
            }
        }

        LaunchedEffect(Unit) {
            delay(1200)
            scannerStatus = "Scanning for Valentine barcode packages..."
            delay(1500)
            scannerStatus = "Target acquired: Valentine 'HUGDAY' Promo Code!"
            delay(1000)
            val codeResult = viewModel.applyCoupon("HUGDAY")
            Toast.makeText(context, codeResult, Toast.LENGTH_LONG).show()
            showScanner = false
        }

        AlertDialog(
            onDismissRequest = { showScanner = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📷 Barcode & Coupon Lens", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = scannerStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Camera frame container box
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(2.dp, getBrandPrimaryColor(mode), RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing corner scan targets
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw simulated scanning lasers & QR code pattern
                            drawRect(
                                color = Color.White.copy(alpha = 0.15f),
                                topLeft = Offset(40f, 40f),
                                size = androidx.compose.ui.geometry.Size(size.width - 80f, size.height - 80f)
                            )
                        }

                        // Pulsing red barcode laser line moving down
                        val infiniteTransition = rememberInfiniteTransition()
                        val laserOffset by infiniteTransition.animateFloat(
                            initialValue = 10f,
                            targetValue = 150f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Divider(
                            color = Color.Green,
                            thickness = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .offset(y = (laserOffset - 80f).dp)
                        )

                        Text("Scan QR / Barcode Card", color = Color(0xFF687280), fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Point camera lens on food pack or voucher card.",
                        fontSize = 10.sp,
                        color = Color(0xFF687280),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showScanner = false }) {
                    Text("Close Camera", color = Color.Red)
                }
            }
        )
    }
}

// --- SUB-WIDGET: HOME FEED CONTENT (NON-SEARCHING MODE) ---
@Composable
fun HomeFeedContent(
    viewModel: AppViewModel,
    selectedCategory: String,
    brandPrimary: Color,
    brandAccent: Color,
    mode: CommerceMode
) {
    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    // Threshold in pixels (approx 320dp) to detect when the services grid on home feed goes off-screen
    val thresholdPx = with(density) { 320.dp.toPx() }
    val showCategoryScrollRow = selectedCategory != "all" || scrollState.value > thresholdPx

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontally scrolling category list shows only when main categories are scrolled away or specific category is chosen
        androidx.compose.animation.AnimatedVisibility(
            visible = showCategoryScrollRow,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                CategoryScrollRow(
                    categories = viewModel.categories,
                    selectedCategoryId = selectedCategory,
                    onCategoryChange = { viewModel.selectCategory(it) },
                    brandColor = brandPrimary
                )
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            // Main vertical content scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp) // padding to avoid bottom cart bar
            ) {
                // Premium Auto-sliding Promotion Carousel (Grocery, Food, Medicine, Tickets)
                PromotionCarousel(brandPrimary = brandPrimary)

                // Dynamic layout rendering based on filtered categories
                val filteredProducts = if (selectedCategory == "all") {
                    viewModel.products
                } else {
                    viewModel.products.filter { it.category == selectedCategory }
                }

                if (selectedCategory == "all") {
                    com.example.ui.ServicesGrid(
                        viewModel = viewModel,
                        columns = 4,
                        onServiceClick = { s -> viewModel.navigateTo(s) }
                    )

                    // Section 1: Fresh Fruits & Vegetables (Fully styled)
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Fresh Fruits & Vegetables 🍎",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "Direct from farms delivered in minutes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Text(
                                text = "See All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A56DB),
                                modifier = Modifier.clickable { viewModel.selectCategory("veg") }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalProductScrollRow(
                            products = viewModel.products.filter { it.category == "veg" },
                            viewModel = viewModel,
                            brandPrimary = brandPrimary,
                            brandAccent = brandAccent
                        )
                    }

                    // Section 2: Popular Restaurants (Premium Swiggy/Zomato style)
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Popular Restaurants Nearby 🍽️",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "Top picks, fast delivery & exclusive discounts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PopularRestaurantsRow(brandPrimary = brandPrimary)
                    }

                    // Section 3: Medicines Near You (Premium Pharmacy scroll)
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Medicines & Wellness Near You 💊",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "Express healthcare & pharmacy essentials",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Text(
                                text = "See All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A56DB),
                                modifier = Modifier.clickable { viewModel.navigateTo(ActiveScreen.MEDICINE) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalProductScrollRow(
                            products = viewModel.products.filter { it.category == "medicine" },
                            viewModel = viewModel,
                            brandPrimary = brandPrimary,
                            brandAccent = brandAccent
                        )
                    }

                    // Section 4: Snacks & Late Night Munchies
                    Column(modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)) {
                        Text(
                            text = "Snacks & Late Night Munchies 🍿",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalProductScrollRow(
                            products = viewModel.products.filter { it.category == "snacks" || it.category == "instant" },
                            viewModel = viewModel,
                            brandPrimary = brandPrimary,
                            brandAccent = brandAccent
                        )
                    }
                } else {
                    // Render Grid for active category
                    val catObj = viewModel.categories.firstOrNull { it.id == selectedCategory }
                    Text(
                        text = "${catObj?.name ?: "Grocery Store"} Items (${filteredProducts.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(550.dp)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts) { item ->
                            GridProductCard(
                                product = item,
                                viewModel = viewModel,
                                brandPrimary = brandPrimary,
                                brandAccent = brandAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-WIDGET: HORIZONTAL CATEGORY SELECTOR ROW ---
@Composable
fun CategoryScrollRow(
    categories: List<CategoryItem>,
    selectedCategoryId: String,
    onCategoryChange: (String) -> Unit,
    brandColor: Color
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedCategoryId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (isSelected) brandColor else Color(0xFFF1F5F9))
                    .clickable { onCategoryChange(category.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("cat_button_${category.id}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = category.labelChar, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    )
                }
            }
        }
    }
}

// --- SUB-WIDGETS: PROMOTION, RESTAURANT AND AI COMMONLY SHARED CODES ---
@Composable
fun PromotionCarousel(brandPrimary: Color) {
    var currentIndex by remember { mutableStateOf(0) }
    
    val banners = listOf(
        Triple(
            "Flat ₹100 Cashback on Groceries 🥦",
            "Use code CROWGROCERY on orders above ₹499. Fresh veggies in 8 mins.",
            Brush.horizontalGradient(listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB)))
        ),
        Triple(
            "Free Delivery on Medicines 💊",
            "No delivery charge on all medicine orders today. Order now!",
            Brush.horizontalGradient(listOf(Color(0xFF0369A1), Color(0xFF0EA5E9)))
        ),
        Triple(
            "Book Taxi in 2 Minutes 🚕",
            "AC cabs & autos available 24x7 near you. First ride discount inside.",
            Brush.horizontalGradient(listOf(Color(0xFF1A56DB), Color(0xFF38BDF8)))
        ),
        Triple(
            "Emergency Ambulance Ready 🚑",
            "24/7 ambulance on standby. Tap SOS for instant dispatch to your location.",
            Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1A3BBF)))
        )
    )
    
    LaunchedEffect(currentIndex) {
        delay(3000)
        currentIndex = (currentIndex + 1) % banners.size
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(115.dp)
            .crowmixShadow(
                elevation = CrowmixElevation.High,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(banners[currentIndex].third)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterStart),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "⚡ CHAMPION DEALS ⚡",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = banners[currentIndex].first,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = banners[currentIndex].second,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Indicator dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                banners.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentIndex) 12.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(if (idx == currentIndex) Color.White else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
fun DoxaAIAssistantCard(viewModel: AppViewModel) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB), Color(0xFF38BDF8))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .crowmixShadow(
                elevation = CrowmixElevation.Medium,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFEFF6FF), Color.White)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DOXA AI Assistant",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Ask any hyperlocal query • GenAI powered",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Online", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable { viewModel.navigateTo(ActiveScreen.DOXA_AI) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Ask DOXA anything... e.g. Order Paracetamol & hire safety taxi",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(ActiveScreen.DOXA_AI) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF1A3BBF), Color(0xFF2563EB)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, "Chat", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.DOXA_AI) },
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A56DB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, "Mic", tint = Color(0xFF1A56DB), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.DOXA_AI) },
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A56DB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, "Camera", tint = Color(0xFF1A56DB), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PopularRestaurantsRow(brandPrimary: Color) {
    val restaurants = listOf(
        Triple("Burger Junction", "Burgers • Fast Food • 4.5 ★ • 18m", "40% OFF"),
        Triple("Punjab Grill", "North Indian • Thali • 4.7 ★ • 25m", "20% OFF"),
        Triple("La Pino'z Pizza", "Pizza • Italian • 4.3 ★ • 20m", "50% OFF"),
        Triple("Starbucks Coffee", "Beverages • Brews • 4.6 ★ • 12m", "Free Delivery")
    )
    
    val imgCharList = listOf("🍔", "🍛", "🍕", "☕")
    val colors = listOf(0xFFFFF9E6, 0xFFFFEBE3, 0xFFFFEBEB, 0xFFE0F2FE)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(restaurants.size) { idx ->
            val rest = restaurants[idx]
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .crowmixShadow(elevation = CrowmixElevation.Low, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEF2F7))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(colors[idx])),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(imgCharList[idx], fontSize = 36.sp)
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color(0xFF00C853), RoundedCornerShape(topEnd = 10.dp, bottomStart = 14.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = rest.third,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = rest.first,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = rest.second,
                        color = Color(0xFF6B7280),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- SUB-WIDGET: PROMOTIONAL CELEBRATE HUG DAY RED BANNER ---
@Composable
fun HugDayCelebrationBanner(brandPrimary: Color, brandAccent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .crowmixShadow(
                elevation = CrowmixElevation.High,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF3366), Color(0xFFFF5E3A), Color(0xFFD61E5C))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VALENTINE'S SPECIAL PICKS ❤️",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Celebrate Hug Day!",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Up to 50% off on Chocolates, Gift baskets & Soft Toys",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🧸☕", fontSize = 28.sp)
            }
        }
    }
}

// --- SUB-WIDGET: HORIZONTAL SCROLL PRODUCT ROW ---
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(12.dp)) {
    val shimmerColors = listOf(Color(0xFFE5E7EB), Color(0xFFF9FAFB), Color(0xFFE5E7EB))
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
        label = "shimmer_offset"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.horizontalGradient(shimmerColors, startX = offset - 300f, endX = offset))
    )
}

@Composable
fun ShimmerProductCard() {
    Card(
        modifier = Modifier.width(160.dp).crowmixShadow(elevation = CrowmixElevation.Low, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ShimmerBox(modifier = Modifier.width(60.dp).height(16.dp), shape = RoundedCornerShape(6.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.width(50.dp).height(14.dp))
        }
    }
}

@Composable
fun HorizontalProductScrollRow(
    products: List<ProductItem>,
    viewModel: AppViewModel,
    brandPrimary: Color,
    brandAccent: Color
) {
    var productsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(600); productsLoaded = true }

    if (!productsLoaded) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(4) { ShimmerProductCard() }
        }
    } else {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(products) { item ->
                val visible = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible.value = true }
                AnimatedVisibility(
                    visible = visible.value,
                    enter = fadeIn(tween(300)) + slideInHorizontally(tween(350)) { it / 2 }
                ) {
                    ProductCard(
                        product = item,
                        viewModel = viewModel,
                        brandPrimary = brandPrimary,
                        brandAccent = brandAccent
                    )
                }
            }
        }
    }
}

// --- SUB-WIDGET: STANDALONE PRODUCT CARD (SCROLLABLE HOME) ---
@Composable
fun ProductCard(
    product: ProductItem,
    viewModel: AppViewModel,
    brandPrimary: Color,
    brandAccent: Color
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val qty = cartItems[product.id] ?: 0

    val discountPercent = product.discountPercent ?: 18

    Card(
        modifier = Modifier
            .width(160.dp)
            .crowmixShadow(
                elevation = CrowmixElevation.Low,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { viewModel.showProductDetail(product) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEFF1F4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Discount badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEFF6FF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$discountPercent% OFF",
                    color = Color(0xFF1A56DB),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Product image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(product.tintColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.labelChar, fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.qtyUnit,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    val original = product.originalPrice ?: (product.price * 1.2)
                    Text(
                        text = "₹${original.toInt()}",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                if (qty > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "—",
                            color = Color(0xFF1A56DB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.removeFromCart(product.id) }
                                .padding(horizontal = 4.dp)
                                .testTag("sub_cart_${product.id}")
                        )
                        Text(
                            text = "$qty",
                            color = Color(0xFF1A56DB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+",
                            color = Color(0xFF1A56DB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.addToCart(product.id) }
                                .padding(horizontal = 4.dp)
                                .testTag("add_cart_${product.id}")
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF1A3BBF), Color(0xFF2563EB)))
                            )
                            .clickable { viewModel.addToCart(product.id) }
                            .testTag("initial_add_cart_${product.id}")
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "+ Add",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-WIDGET: COHE_SIVE GRID PRODUCT CARD ---
@Composable
fun GridProductCard(
    product: ProductItem,
    viewModel: AppViewModel,
    brandPrimary: Color,
    brandAccent: Color
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val qty = cartItems[product.id] ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .crowmixShadow(
                elevation = CrowmixElevation.Low,
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF9CA3AF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { viewModel.showProductDetail(product) }
            .padding(10.dp)
    ) {
        // Product image card with heart icon and ADD overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {
            // Main background card (rounded corners)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(product.tintColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.labelChar, fontSize = 42.sp)
                
                // Blue heart icon on top left
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(18.dp)
                )
            }

            // ADD buttons overlapping on the bottom center
            if (qty > 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(30.dp)
                        .width(76.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2563EB))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "—",
                            color = Color(0xFF2563EB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .clickable { viewModel.removeFromCart(product.id) }
                                .padding(horizontal = 4.dp)
                                .testTag("grid_sub_cart_${product.id}")
                        )
                        Text(
                            text = "$qty",
                            color = Color(0xFF2563EB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+",
                            color = Color(0xFF2563EB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .clickable { viewModel.addToCart(product.id) }
                                .padding(horizontal = 4.dp)
                                .testTag("grid_add_cart_${product.id}")
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(30.dp)
                        .width(68.dp)
                        .clickable { viewModel.addToCart(product.id) }
                        .testTag("grid_initial_add_cart_${product.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2563EB))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ADD",
                            color = Color(0xFF2563EB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Price badge in blue filled pill and secondary crossed price
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF2563EB), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "₹${product.price.toInt()}",
                    color = Color(0xFF111827),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (product.originalPrice != null) {
                Text(
                    text = "₹${product.originalPrice.toInt()}",
                    style = TextStyle(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                )
            }
        }

        // Product Name
        Text(
            text = product.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Qty/Weight
        Text(
            text = product.qtyUnit,
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 1.dp)
        )

        // Calculated / Default flat discount tag
        val discountText = if (product.originalPrice != null && product.originalPrice > product.price) {
            "₹${(product.originalPrice - product.price).toInt()} OFF"
        } else {
            "₹10 OFF"
        }
        Text(
            text = discountText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB),
            modifier = Modifier.padding(top = 2.dp)
        )

        // Dotted divider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(16) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFF9CA3AF).copy(alpha = 0.5f))
                )
            }
        }

        // Star rating and simulated reviews/delivery time line
        val ratingCode = product.id.hashCode()
        val ratingVal = String.format("%.1f", 4.0 + (kotlin.math.abs(ratingCode) % 10) / 10.0)
        val reviewsCount = 100 + (kotlin.math.abs(ratingCode) % 400)
        val deliveryMins = 10 + (kotlin.math.abs(ratingCode) % 15)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$ratingVal($reviewsCount) | $deliveryMins mins",
                fontSize = 9.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// --- SUB-WIDGET: DYNAMIC SEARCH OVERLAY ---
@Composable
fun SearchOverlay(
    viewModel: AppViewModel,
    brandPrimary: Color,
    brandAccent: Color
) {
    val query by viewModel.searchQuery.collectAsState()
    val matchingItems = viewModel.products.filter {
        it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
    }

    val popularSearches = listOf("Onion", "Potato", "Milk", "Noodles", "Silk", "Dettol")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        if (query.isEmpty()) {
            Text(
                text = "Popular Searches 🔥",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF334155),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Flow grid row representation for recommended words
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                popularSearches.forEach { searchWord ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { viewModel.setQuery(searchWord) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = searchWord, fontSize = 12.sp, color = Color(0xFF475569))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search across thousands of farm fresh goods & late night munchies instantly!",
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = Color(0xFF687280),
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
            }
        } else {
            Text(
                text = "Search Results for '$query' (${matchingItems.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF687280),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (matchingItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No results found", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Try searching for groceries, medicines,\nfood or services.", fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matchingItems) { item ->
                        SearchItemRow(
                            product = item,
                            viewModel = viewModel,
                            brandPrimary = brandPrimary
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-WIDGET: ROW CARD REPRESENTATION WITHIN SEARCH OVERLAY ---
@Composable
fun SearchItemRow(
    product: ProductItem,
    viewModel: AppViewModel,
    brandPrimary: Color
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val qty = cartItems[product.id] ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(product.tintColorHex)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = product.labelChar, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0F172A)
            )
            Text(text = "Unit: ${product.qtyUnit} • ₹${product.price.toInt()}", fontSize = 11.sp, color = Color(0xFF687280))
        }

        if (qty > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(brandPrimary)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "-",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { viewModel.removeFromCart(product.id) }
                )
                Text(text = "$qty", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { viewModel.addToCart(product.id) }
                )
            }
        } else {
            Button(
                onClick = { viewModel.addToCart(product.id) },
                colors = ButtonDefaults.buttonColors(containerColor = brandPrimary),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("ADD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper Extension for Drawing dashed border on cards or chips
fun Modifier.dashedBorder(color: Color, strokeWidth: Float, dashLength: Float, gapLength: Float, cornerRadius: Float) = this.drawBehind {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth, pathEffect = pathEffect),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}

// --- SUB-WIDGET: FULL CART DETAIL SLIDING CARD DIALOG SHEET ---
@Composable
fun CartDrawerSheet(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    brandPrimary: Color,
    brandAccent: Color,
    mode: CommerceMode
) {
    val cartMap by viewModel.cartItems.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val deliveryTip by viewModel.deliveryTip.collectAsState()
    val address by viewModel.deliveryAddress.collectAsState()

    var couponInput by remember { mutableStateOf("") }
    var couponToast by remember { mutableStateOf<String?>(null) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var tempAddress by remember { mutableStateOf(address) }
    var paymentMode by remember { mutableStateOf("UPI") } // "UPI", "COD", "CARD"

    val subtotal = viewModel.getCartSubtotal()
    val crowmixGreen = Color(0xFF0C8346)

    // Slide-up animated full screen container on top of current view for instant presentation!
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f)) // dimmed background
            .clickable(onClick = onDismiss) // dismiss when clicking outside
    ) {
        var cartVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { cartVisible = true }
        AnimatedVisibility(
            visible = cartVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(350, easing = EaseOutQuart)) { it }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f) // high-fidelity height
                    .clickable(enabled = false, onClick = {}) // prevent dismiss propagation when tapping on sheet content
                    .testTag("cart_sheet_container"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), // matching clean background
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Drag handle
                Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFD1D5DB))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Header (White background with black text matching the image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBasket,
                                    contentDescription = "My Basket",
                                    tint = crowmixGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MY BASKET",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            val loadedCount = cartMap.values.sum()
                            Text(
                                text = "$loadedCount ${if (loadedCount == 1) "item" else "items"} loaded",
                                color = Color(0xFF687280),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                             )
                        }
                    }
                }

                // Scrollable main content!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // GREEN DELIVERING BANNER inside basket matching the image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(crowmixGreen)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DELIVERING IN 10 MINUTES FLAT",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "crowmix instant fulfillment centers are active near sector 62.",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DELIVERY LOCATION SECTION
                    Text(
                        text = "DELIVERY LOCATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF687280),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location Pin",
                                    tint = Color(0xFFEF4444), // red location pin
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "DELIVERY LOCATION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF687280)
                                    )
                                    Text(
                                        text = address,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            TextButton(
                                onClick = { showAddressDialog = true }
                            ) {
                                Text(
                                    text = "CHANGE",
                                    color = crowmixGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SELECTED GROCERIES SECTION
                    Text(
                        text = "SELECTED GROCERIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF687280),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (cartMap.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛒", fontSize = 48.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Your basket is empty!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Add groceries, medicines or food\nto get started.", fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF1A3BBF), Color(0xFF2563EB))))
                                    .clickable { onDismiss() }
                                    .padding(horizontal = 32.dp, vertical = 14.dp)
                            ) {
                                Text("Browse Products", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                cartMap.forEach { (prodId, qty) ->
                                    val itemObj = viewModel.products.firstOrNull { it.id == prodId }
                                    if (itemObj != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp, horizontal = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(itemObj.tintColorHex)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = itemObj.labelChar, fontSize = 26.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = itemObj.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                    Text(
                                                        text = "${itemObj.qtyUnit} • ₹${itemObj.price.toInt()} EACH",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF687280)
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "₹${(itemObj.price * qty).toInt()}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF0F172A),
                                                    modifier = Modifier.padding(end = 12.dp)
                                                )

                                                // Clean high-fidelity green stepper matching image
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(crowmixGreen)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { viewModel.removeFromCart(itemObj.id) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Text("−", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(
                                                        text = "$qty",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 12.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    )
                                                    IconButton(
                                                        onClick = { viewModel.addToCart(itemObj.id) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // APPLY PROMO COUPON
                    Text(
                        text = "APPLY PROMO COUPON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF687280),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Coupon Entry Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = "Coupon Icon",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)),
                                    decorationBox = { innerTextField ->
                                        if (couponInput.isEmpty()) {
                                            Text(
                                                text = "ENTER COUPON (e.g. INSTANT5)",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (couponInput.isNotBlank()) {
                                                val result = viewModel.applyCoupon(couponInput)
                                                couponToast = result
                                                couponInput = ""
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "APPLY",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            if (couponToast != null) {
                                Text(
                                    text = couponToast ?: "",
                                    fontSize = 11.sp,
                                    color = if (couponToast?.startsWith("SUCCESS") == true) crowmixGreen else Color.Red,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            // Quick dashed coupons beneath
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                if (appliedCoupon != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEAF9EC))
                                            .border(1.dp, crowmixGreen, RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "COUPON '${appliedCoupon?.code}' APPLIED!",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = crowmixGreen
                                            )
                                            Text(
                                                text = appliedCoupon?.discountDescription ?: "",
                                                fontSize = 10.sp,
                                                color = crowmixGreen.copy(alpha = 0.8f)
                                            )
                                        }
                                        TextButton(onClick = { viewModel.removeCoupon() }) {
                                            Text("Remove", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("INSTANT50", "FREESHIP").forEach { code ->
                                            val descText = if (code == "INSTANT50") "INSTANT50 (₹50 OFF)" else "FREESHIP"
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFBFDFA), RoundedCornerShape(6.dp))
                                                    .dashedBorder(
                                                        color = crowmixGreen,
                                                        strokeWidth = 2f,
                                                        dashLength = 8f,
                                                        gapLength = 6f,
                                                        cornerRadius = 12f
                                                    )
                                                    .clickable {
                                                        val result = viewModel.applyCoupon(code)
                                                        couponToast = result
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = descText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = crowmixGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SUPPORT RIDERS SECTION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SUPPORT RIDERS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF687280)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEAF9EC), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, crowmixGreen, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "100% GOES TO RIDER",
                                        color = crowmixGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Our local agents deliver safely in all weather conditions. Appreciate their speedy service.",
                                fontSize = 11.sp,
                                color = Color(0xFF687280)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tipOptions = listOf(
                                    Pair(10.0, "Nice"),
                                    Pair(20.0, "Generous"),
                                    Pair(30.0, "Polite"),
                                    Pair(50.0, "Superstar")
                                )
                                tipOptions.forEach { (valAmt, label) ->
                                    val isSel = deliveryTip == valAmt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) crowmixGreen else Color.White)
                                            .border(1.dp, if (isSel) crowmixGreen else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isSel) {
                                                    viewModel.setDeliveryTip(0.0) // deselect
                                                } else {
                                                    viewModel.setDeliveryTip(valAmt)
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "+₹${valAmt.toInt()}",
                                                fontWeight = FontWeight.Black,
                                                color = if (isSel) Color.White else Color(0xFF1E293B),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = label,
                                                color = if (isSel) Color.White.copy(alpha = 0.82f) else Color(0xFF687280),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SELECT PAYMENT MODE SECTION
                    Text(
                        text = "SELECT PAYMENT MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF687280),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pmOptions = listOf(
                            Triple("UPI", "INSTANT UPI EXPRESS", "PhonePe, GPay, Paytm, Cred"),
                            Triple("COD", "CASH ON DELIVERY", "Pay at door-step via cash/UPI QR"),
                            Triple("CARD", "CREDIT / DEBIT CARDS", "Visa, Mastercard, RuPay, Amex")
                        )
                        pmOptions.forEach { (id, title, subtitleText) ->
                            val isSelected = paymentMode == id
                            val pmIcon = when (id) {
                                "UPI" -> Icons.Default.Payment
                                "COD" -> Icons.Default.Wallet
                                else -> Icons.Default.CreditCard
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) crowmixGreen else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .clickable { paymentMode = id }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = pmIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) crowmixGreen else Color(0xFF687280),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = subtitleText,
                                                fontSize = 10.sp,
                                                color = Color(0xFF687280)
                                            )
                                        }
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { paymentMode = id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF0284C7) // Blue/green radio selector like in the user's image!
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // BILL SUMMARY DETAILS SECTION
                    Text(
                        text = "BILL SUMMARY DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF687280),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isFreeDelivery = subtotal >= 200.0
                            val deliveryPartnerFee = if (isFreeDelivery) 0.0 else 15.0
                            val handlingCharges = 5.0
                            val couponDiscountVal = appliedCoupon?.discountValue ?: 0.0
                            val grandTotalExpected = (subtotal + deliveryPartnerFee + handlingCharges + deliveryTip - couponDiscountVal).coerceAtLeast(0.0)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Total", fontSize = 12.sp, color = Color(0xFF687280))
                                Text("₹${subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Partner Fee", fontSize = 12.sp, color = Color(0xFF687280))
                                Text(
                                    text = if (isFreeDelivery) "FREE" else "₹${deliveryPartnerFee.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFreeDelivery) crowmixGreen else Color(0xFF1E293B)
                                )
                            }

                            // Dynamic Dotted warning message about Free Delivery Unlock
                            if (!isFreeDelivery && subtotal > 0) {
                                val amountNeeded = 200.toInt() - subtotal.toInt()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFFBEB), RoundedCornerShape(6.dp))
                                        .dashedBorder(
                                            color = Color(0xFFF59E0B),
                                            strokeWidth = 2f,
                                            dashLength = 8f,
                                            gapLength = 6f,
                                            cornerRadius = 12f
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Add ₹$amountNeeded more to unlock FREE Delivery Fee!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Handling & Pack Charges", fontSize = 12.sp, color = Color(0xFF687280))
                                Text("₹${handlingCharges.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }

                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Promo Coupon Discount (${appliedCoupon?.code})", fontSize = 12.sp, color = crowmixGreen)
                                    Text("-₹${couponDiscountVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = crowmixGreen)
                                }
                            }

                            if (deliveryTip > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rider Tip", fontSize = 12.sp, color = Color(0xFF687280))
                                    Text("₹${deliveryTip.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "₹${grandTotalExpected.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = crowmixGreen // Bold Green grand total exactly like in image!
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // CURRENT DELIVERY AREA BOTTOM BANNER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111827)) // Dark navy/black
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Area Icon",
                                tint = Color(0xFFF59E0B), // golden map pin
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CURRENT DELIVERY AREA",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = address,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // STICKY BOTTOM CHECKOUT BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    val isFreeDelivery = subtotal >= 200.0
                    val deliveryPartnerFee = if (isFreeDelivery) 0.0 else 15.0
                    val handlingCharges = 5.0
                    val couponDiscountVal = appliedCoupon?.discountValue ?: 0.0
                    val grandTotalExpected = (subtotal + deliveryPartnerFee + handlingCharges + deliveryTip - couponDiscountVal).coerceAtLeast(0.0)

                    Button(
                        onClick = {
                            viewModel.placeOrder()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_checkout_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = crowmixGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "₹${grandTotalExpected.toInt()} TOTAL",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = when (paymentMode) {
                                        "UPI" -> "UPI PAY"
                                        "COD" -> "CASH ON DELIVERY"
                                        else -> "CREDIT / DEBIT CARD"
                                    },
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PLACE ORDER",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Interactive Address dialog to edit inline
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = {
                Text(
                    text = "Update Delivery Location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                OutlinedTextField(
                    value = tempAddress,
                    onValueChange = { tempAddress = it },
                    placeholder = { Text("Address details...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = crowmixGreen,
                        unfocusedBorderColor = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDeliveryAddress(tempAddress)
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = crowmixGreen)
                ) {
                    Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("CANCEL", color = Color(0xFF687280))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// --- 2. ORDER TRACKING & COUNTDOWN SCREEN (HIGH FIDELITY UPGRADE) ---
@Composable
fun OrderTrackingScreen(viewModel: AppViewModel) {
    val mode by viewModel.currentMode.collectAsState()
    val deliveryStep by viewModel.deliveryStep.collectAsState()
    val etaSeconds by viewModel.deliveryEtaSeconds.collectAsState()
    val trackingProgress by viewModel.trackingProgress.collectAsState()
    val tipAmount by viewModel.deliveryTip.collectAsState()
    val cancelTimeRemaining by viewModel.cancelTimeRemaining.collectAsState()

    val brandPrimary = getBrandPrimaryColor(mode)
    val brandAccent = getBrandAccentColor(mode)

    val etaMinutes = etaSeconds / 60
    val etaRemainingSeconds = etaSeconds % 60

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isCamMicApproved by viewModel.isCameraMicApproved.collectAsState()
    val isBgActive by viewModel.isBackgroundEngineOn.collectAsState()

    // Interactive message logs state
    var messagesList by remember { mutableStateOf(listOf("Hi, I am starting from the store now!", "I'll make sure to get the products fresh!")) }
    var typedMessage by remember { mutableStateOf("") }

    val trackingPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val cam = perms[Manifest.permission.CAMERA] == true
        val mic = perms[Manifest.permission.RECORD_AUDIO] == true
        if (cam && mic) {
            viewModel.setCameraMicApproved(true)
            Toast.makeText(context, "Permissions granted! Direct VoIP simulation active.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "VoIP calls require Camera & Microphone approvals.", Toast.LENGTH_LONG).show()
        }
    }

    val trackingBgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setBackgroundEngineOn(true)
        Toast.makeText(context, "Background always-on enabled!", Toast.LENGTH_SHORT).show()
    }

    var showRiderCallDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Light background matching user preference
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // TOP APPLET HEADER BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE ULTRASONIC RADAR ACTIVE",
                    style = TextStyle(
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            IconButton(
                onClick = { viewModel.resetOrder() },
                modifier = Modifier.testTag("reset_order_btn")
            ) {
                Icon(Icons.Default.Close, null, tint = Color(0xFF0F172A))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. BRAND ELEVATED TIMER & ETA SECTION (Blinkit / Zepto Style)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), // Slate 50 background
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // Light gray border
            elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = brandPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "⚡ DELIVERING BY 10 MINUTES FLAT ⚡",
                        fontSize = 10.sp,
                        color = if (mode == CommerceMode.ZEPTO) Color(0xFF10B981) else brandAccent,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = String.format("%02d:%02d", etaMinutes, etaRemainingSeconds),
                    style = TextStyle(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A), // Dark slate ETA numbers
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1.5).sp
                    )
                )

                Text(
                    "MINUTES REMAINING",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(16.dp))

                // Active Courier Partner Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)), // Light avatar background
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏍️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ramesh Kumar",
                                color = Color(0xFF0F172A), // Dark text for courier name
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⭐ 4.9 ",
                                    color = Color(0xFFFFD414),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "(1,240+ deliveries)",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // VoIP action button
                    IconButton(
                        onClick = {
                            if (isCamMicApproved) {
                                showRiderCallDialog = true
                            } else {
                                trackingPermLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(brandAccent)
                            .testTag("call_rider_voip_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "VoIP Call",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Show dynamic phone text for compliance: "delivery boy number,name"
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "📞 Rider Phone: +91 80911 23456 (R-8091)",
                        fontSize = 11.sp,
                        color = Color(0xFF475569), // Darker phone label
                        modifier = Modifier.padding(start = 52.dp)
                    )
                }

                if (tipAmount > 0.0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "💖 You tipped Ramesh ₹${tipAmount.toInt()} extra!",
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. LIVE ROUTE & GPS STREET MAP (2D Ultra-high Fidelity Upgrade)
        Text(
            text = "GPS Route Path Progress Map (Live)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A), // Dark slate title for light theme
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)), // Dark card remains for radar-like map view contrast
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF1E2638)),
            elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
        ) {
            val pulsingAngle = rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val w = size.width
                val h = size.height

                // Draw realistic city street layout (Aesthetic grid streets)
                // Draw Horizontal Roads
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, h * 0.25f),
                    end = Offset(w, h * 0.25f),
                    strokeWidth = 24f
                )
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, h * 0.75f),
                    end = Offset(w, h * 0.75f),
                    strokeWidth = 24f
                )
                // Draw Vertical Roads
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(w * 0.35f, 0f),
                    end = Offset(w * 0.35f, h),
                    strokeWidth = 24f
                )
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(w * 0.7f, 0f),
                    end = Offset(w * 0.7f, h),
                    strokeWidth = 24f
                )

                // Define winding waypoint course representing the real streets route
                // Waypoint path coordinates: Store -> intersection 1 -> intersection 2 -> Home
                val pStore = Offset(40f, h * 0.25f)
                val pInt1 = Offset(w * 0.35f, h * 0.25f)
                val pInt2 = Offset(w * 0.35f, h * 0.75f)
                val pInt3 = Offset(w * 0.7f, h * 0.75f)
                val pHome = Offset(w - 40f, h * 0.75f)

                // Draw route line outline
                val routePathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(Color.DarkGray, pStore, pInt1, strokeWidth = 6f, pathEffect = routePathEffect)
                drawLine(Color.DarkGray, pInt1, pInt2, strokeWidth = 6f, pathEffect = routePathEffect)
                drawLine(Color.DarkGray, pInt2, pInt3, strokeWidth = 6f, pathEffect = routePathEffect)
                drawLine(Color.DarkGray, pInt3, pHome, strokeWidth = 6f, pathEffect = routePathEffect)

                // Current traversed coordinates calculation based on trackingProgress
                val currentPos = when {
                    trackingProgress < 0.25f -> {
                        val subP = trackingProgress / 0.25f
                        pStore + (pInt1 - pStore) * subP
                    }
                    trackingProgress < 0.5f -> {
                        val subP = (trackingProgress - 0.25f) / 0.25f
                        pInt1 + (pInt2 - pInt1) * subP
                    }
                    trackingProgress < 0.75f -> {
                        val subP = (trackingProgress - 0.5f) / 0.25f
                        pInt2 + (pInt3 - pInt2) * subP
                    }
                    else -> {
                        val subP = (trackingProgress - 0.75f) / 0.25f
                        pInt3 + (pHome - pInt3) * subP
                    }
                }

                // Draw traversed line highlighting
                if (trackingProgress > 0.01f) {
                    when {
                        trackingProgress < 0.25f -> {
                            drawLine(brandAccent, pStore, currentPos, strokeWidth = 7f)
                        }
                        trackingProgress < 0.5f -> {
                            drawLine(brandAccent, pStore, pInt1, strokeWidth = 7f)
                            drawLine(brandAccent, pInt1, currentPos, strokeWidth = 7f)
                        }
                        trackingProgress < 0.75f -> {
                            drawLine(brandAccent, pStore, pInt1, strokeWidth = 7f)
                            drawLine(brandAccent, pInt1, pInt2, strokeWidth = 7f)
                            drawLine(brandAccent, pInt2, currentPos, strokeWidth = 7f)
                        }
                        else -> {
                            drawLine(brandAccent, pStore, pInt1, strokeWidth = 7f)
                            drawLine(brandAccent, pInt1, pInt2, strokeWidth = 7f)
                            drawLine(brandAccent, pInt2, pInt3, strokeWidth = 7f)
                            drawLine(brandAccent, pInt3, currentPos, strokeWidth = 7f)
                        }
                    }
                }

                // Draw Store Hub Marker
                drawCircle(Color(0xFF10B981), radius = 12f, center = pStore)
                drawCircle(Color.White, radius = 6f, center = pStore)

                // Draw Customer Home Marker
                drawCircle(brandPrimary.copy(alpha = 0.3f), radius = 22f + (pulsingAngle.value % 8f), center = pHome)
                drawCircle(brandPrimary, radius = 12f, center = pHome)
                drawCircle(Color.White, radius = 5f, center = pHome)

                // Draw Rider motorbike pulsing avatar
                drawCircle(Color.White, radius = 14f, center = currentPos)
                drawCircle(brandAccent, radius = 10f, center = currentPos)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🏬 Sector 62 Dispatch Hub", fontSize = 10.sp, color = Color(0xFF64748B))
            Text("🏠 Your Delivery Address", fontSize = 10.sp, color = Color(0xFF64748B))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. ORDER CANCELLATION ENGINE WITH 1-MINUTE TIMER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (cancelTimeRemaining > 0) Color(0xFFFFF1F2) else Color(0xFFF8FAFC)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (cancelTimeRemaining > 0) Color(0xFFFECDD3) else Color(0xFFE2E8F0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (cancelTimeRemaining > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Need to change or cancel?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF9F1239) // Deep maroon title
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Order cancellation is permitted for ${cancelTimeRemaining} seconds.",
                                fontSize = 11.sp,
                                color = Color(0xFFE11D48) // Clean red timer text
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.resetOrder()
                                Toast.makeText(context, "Order Cancelled Successfully ❌", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Cancel Order", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Cancellation Window Closed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF475569) // Dark slate-gray locked title
                            )
                            Text(
                                "Your order is accepted and prepared. Can no longer be cancelled.",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B) // Slate description
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. "BOX TO MESSAGE" CHAT SERVICE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(brandAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💬", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message Ramesh Kumar (Box)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chat logs list container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .heightIn(max = 140.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    messagesList.forEachIndexed { idx, msg ->
                        // Simulate standard caller alignment vs rider alignment
                        val isUser = idx >= 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp,
                                            bottomStart = if (isUser) 8.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 8.dp
                                        )
                                    )
                                    .background(if (isUser) brandPrimary else Color(0xFFE2E8F0))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = if (isUser) Color.White else Color(0xFF0F172A),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick helpful messaging shortcuts
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val shortcuts = listOf("Leave at gate 🚪", "Call on reach 📞", "Keep near door 🏠", "Speed up ⚡")
                    items(shortcuts) { label ->
                        Surface(
                            onClick = {
                                messagesList = messagesList + label
                                Toast.makeText(context, "Sent to Ramesh!", Toast.LENGTH_SHORT).show()
                            },
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message text input container
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedMessage,
                        onValueChange = { typedMessage = it },
                        placeholder = { Text("Note to Ramesh (e.g., Gate 5, Ring bell...)", fontSize = 11.sp, color = Color(0xFF687280)) },
                        textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 11.sp),
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = brandAccent,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (typedMessage.isNotBlank()) {
                                messagesList = messagesList + typedMessage
                                typedMessage = ""
                                Toast.makeText(context, "Note shared!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brandAccent)
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. DELIVERY MILESTONES (Clickable - Updated Manual by Delivery boy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Order Progression",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A) // Dark slate header
            )

            // Dynamic simulator visual indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Rider Simulation Mode 🏍️",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "💡 Tap any step below to manually update order carrier milestone progress.",
            fontSize = 10.sp,
            color = Color(0xFF64748B) // Slate text
        )
        Spacer(modifier = Modifier.height(16.dp))

        val steps = listOf(
            DeliveryStep.ORDER_CONFIRMED to "Order Confirmed At Kitchen Store",
            DeliveryStep.STORE_PREPARATION to "Items Packed & Quality Evaluated",
            DeliveryStep.PARTNER_ASSIGNED to "Ramesh Kumar picked up packages",
            DeliveryStep.ON_THE_WAY to "Rider is speeding down highway bypass",
            DeliveryStep.NEARBY to "Rider is close to gate!",
            DeliveryStep.DELIVERED to "Arrived safely! Enjoy fresh groceries."
        )

        steps.forEach { (step, label) ->
            val isCleared = deliveryStep.ordinal >= step.ordinal
            val isCurrent = deliveryStep == step

            Surface(
                onClick = {
                    viewModel.manuallySetDeliveryStep(step)
                    Toast.makeText(context, "Milestone updated: ${step.name} 🏍️", Toast.LENGTH_SHORT).show()
                },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) brandAccent else {
                                    if (isCleared) Color(0xFF10B981) else Color(0xFFE2E8F0) // Light gray instead of Color(0xFF262F48)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            Text("🏍️", fontSize = 11.sp)
                        } else if (isCleared) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF687280), CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isCurrent) Color(0xFF0F172A) else {
                            if (isCleared) Color(0xFF475569) else Color(0xFF94A3B8)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DOWNLOAD BILL INVOICE SYSTEM
        var showBillInvoiceOverlay by remember { mutableStateOf(false) }

        Button(
            onClick = { showBillInvoiceOverlay = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF10B981)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("download_bill_btn"),
            shape = RoundedCornerShape(20.dp) // Corner radius of 20px (20dp) on bill download button!
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Bill Detail",
                tint = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Bill Invoice Receipt 📄", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showBillInvoiceOverlay) {
            AlertDialog(
                onDismissRequest = { showBillInvoiceOverlay = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL CROWMIX TAX INVOICE",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { showBillInvoiceOverlay = false }) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFF687280))
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Invoice #: ZPT-2026-98103", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF687280))
                                Text("Cashier Agent: Kirana Hub Delhi-S4", fontSize = 10.sp, color = Color(0xFF687280))
                                Text("Payment Status: COD (CASH ON DELIVERY)", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("PURCHASED BASKET SUMMARY:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF687280))

                        // Render each product and quantity currently ordered in the app state
                        val productsState = viewModel.products
                        val currentCart = viewModel.cartItems.collectAsState().value
                        if (currentCart.isEmpty()) {
                            // Fallback to some items if active cart was reset
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Fresh Red Potatoes (1 kg)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("₹54", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Toned Amul Fresh Milk (500 ml)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("₹32", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        } else {
                            currentCart.forEach { (prodId, qty) ->
                                val item = productsState.firstOrNull { it.id == prodId }
                                if (item != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${item.name} (${item.qtyUnit}) x $qty", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                        Text("₹${(item.price * qty).toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }
                            }
                        }

                        Divider(color = Color(0xFF9CA3AF).copy(alpha = 0.5f))

                        val summarySubtotal = if (viewModel.getCartSubtotal() > 0) viewModel.getCartSubtotal() else 86.0
                        val tip = viewModel.deliveryTip.collectAsState().value
                        val gst = 4.0
                        val grandTotal = if (viewModel.getCartSubtotal() > 0) viewModel.getGrandTotal() else 115.0

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 11.sp, color = Color(0xFF687280))
                            Text("₹${summarySubtotal.toInt()}", fontSize = 11.sp, color = Color(0xFF1E293B))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxes & Service Handling", fontSize = 11.sp, color = Color(0xFF687280))
                            Text("₹${gst.toInt()}", fontSize = 11.sp, color = Color(0xFF1E293B))
                        }
                        if (tip > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rider Tip Benefit", fontSize = 11.sp, color = Color(0xFF687280))
                                Text("₹${tip.toInt()}", fontSize = 11.sp, color = Color(0xFF1E293B))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEAF9EC))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total Billed", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF0C8346))
                            Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0C8346))
                        }

                        Text(
                            text = "Certified Electronic Tax Document. Prepared strictly for direct shipment transit verification.",
                            fontSize = 9.sp,
                            color = Color(0xFF9CA3AF),
                            lineHeight = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showBillInvoiceOverlay = false
                            Toast.makeText(context, "SUCCESS ✅: PDF Receipt saved to internal Downloads folder!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (mode == CommerceMode.ZEPTO) Color(0xFF1A56DB) else Color(0xFF0C8346)),
                        shape = RoundedCornerShape(20.dp), // Corner radius of 20px (20dp) as requested!
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save PDF Receipt offline", color = Color.White)
                    }
                }
            )
        }

        // CANCEL / REORDER RESET SYSTEM (BACK BUTTON)
        Button(
            onClick = { viewModel.resetOrder() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("cancel_reorder_reset_btn"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Back to Store Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// --- CORE UTILITY: DYNAMIC PERMISSIONS CONTROL PANEL ---
@Composable
fun PermissionsControlCenter(
    viewModel: AppViewModel,
    brandPrimary: Color,
    brandAccent: Color
) {
    val context = LocalContext.current
    val isGpsActive by viewModel.isGpsDetected.collectAsState()
    val isCamMicActive by viewModel.isCameraMicApproved.collectAsState()
    val isBgActive by viewModel.isBackgroundEngineOn.collectAsState()

    // 1. Location permission launcher
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            viewModel.setGpsDetected(true)
            Toast.makeText(context, "📍 GPS Radar: Precise Live Location Approved!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Location permission denied. Defaulting to mock Mumbai.", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Camera & Mic permission launcher
    val camMicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val camera = perms[Manifest.permission.CAMERA] == true
        val record = perms[Manifest.permission.RECORD_AUDIO] == true
        if (camera && record) {
            viewModel.setCameraMicApproved(true)
            Toast.makeText(context, "🎙️📸 Approved: Voice Search & Courier Scanner Engaged!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Camera or Microphone permission was denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Background Always Active / Notifications launcher
    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setBackgroundEngineOn(true)
        if (isGranted) {
            Toast.makeText(context, "⚡ Background Engine Always-On Status: RUNNING!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Background always-on activated. Simulated tracking panel running.", Toast.LENGTH_SHORT).show()
        }
        
        // Trigger battery optimizations bypass intent
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore if settings screen is not supported or context lacks permissions
        }
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("permissions_consent_center"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Medium)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(brandPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Guard",
                            tint = brandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🛡️ Device Permissions & Guard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Active: " + listOf(
                                if (isGpsActive) "Location" else null,
                                if (isCamMicActive) "Voice/Cam" else null,
                                if (isBgActive) "Background" else null
                            ).filterNotNull().let { if (it.isEmpty()) "None Active" else it.joinToString(", ") },
                            fontSize = 11.sp,
                            color = Color(0xFF687280)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Hide" else "Manage Keys",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandPrimary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Info",
                        tint = brandPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "To grant live order tracking, voice assistance with delivery drivers, and background system wake, please declare approvals below:",
                        fontSize = 11.sp,
                        color = Color(0xFF687280),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. LOCATION ROW
                    PermissionRow(
                        icon = Icons.Default.LocationOn,
                        title = "Live GPS Location Tracking",
                        description = "Required to auto-detect address and plot current partner positions on live maps.",
                        isGranted = isGpsActive,
                        onGrantClick = {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        brandPrimary = brandPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. CAMERA/MIC ROW
                    PermissionRow(
                        icon = Icons.Default.Mic,
                        title = "Camera & Audio Call Permission",
                        description = "Enables Coupon Scan lenses, searching using voice microphone signals, and rider VoIP calls.",
                        isGranted = isCamMicActive,
                        onGrantClick = {
                            camMicLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                )
                            )
                        },
                        brandPrimary = brandPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. BACKGROUND SERVICE ROW
                    PermissionRow(
                        icon = Icons.Default.Notifications,
                        title = "Background Always-On Engine",
                        description = "Bypasses system battery limitations using foreground threads and keeps tracking live.",
                        isGranted = isBgActive,
                        onGrantClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        brandPrimary = brandPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    brandPrimary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isGranted) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFF9CA3AF).copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF10B981) else Color(0xFF687280),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = Color(0xFF687280),
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ACTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF10B981)
                )
            }
        } else {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = brandPrimary),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = "Grant",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
