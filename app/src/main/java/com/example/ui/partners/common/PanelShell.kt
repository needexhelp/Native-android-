package com.example.ui.partners.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelShell(
    partnerType: PartnerType,
    partnerName: String,
    identityCode: String,
    isPanelOnline: Boolean,
    isNotifOn: Boolean,
    offlineReason: String,
    onPanelToggle: (Boolean, String?) -> Unit,
    onNotifToggle: (Boolean, String?) -> Unit,
    onSignOut: () -> Unit,
    content: @Composable (currentTab: PanelTab) -> Unit
) {
    var currentTab by remember { mutableStateOf(PanelTab.ORDERS) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showPanelOffDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = PanelColors.Card) {
                // Header
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(partnerName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(identityCode, fontSize = 12.sp, color = PanelColors.TextSecondary)
                }
                HorizontalDivider()

                // Nav items
                PanelTab.entries.forEach { tab ->
                    NavigationDrawerItem(
                        label = { Text(tab.label) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PanelColors.TealLight,
                            selectedTextColor = PanelColors.TealDark,
                            selectedIconColor = PanelColors.TealDark
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Sign Out", color = PanelColors.RedDark) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out", tint = PanelColors.RedDark) },
                    selected = false,
                    onClick = onSignOut
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(partnerType.displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            // Identity code badge
                            Text(
                                text = identityCode,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(PanelColors.Blue, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                color = PanelColors.BlueDark
                            )
                            Spacer(Modifier.width(6.dp))
                            // Notification toggle button
                            NotificationToggleButton(
                                isOn = isNotifOn,
                                partnerType = partnerType,
                                onToggle = onNotifToggle
                            )
                            Spacer(Modifier.width(8.dp))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = PanelColors.Card)
                    )

                    // Status bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isPanelOnline) PanelColors.TealLight else PanelColors.Red)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isPanelOnline) "● Online — Accepting orders"
                                   else "● Offline${if (offlineReason.isNotBlank()) " — $offlineReason" else ""}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPanelOnline) PanelColors.TealDark else PanelColors.RedDeep
                        )
                        Switch(
                            checked = isPanelOnline,
                            onCheckedChange = { goingOnline ->
                                if (!goingOnline && partnerType.requiresOffReason) {
                                    showPanelOffDialog = true
                                } else {
                                    onPanelToggle(goingOnline, null)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PanelColors.Teal,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = PanelColors.TextDisabled
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                content(currentTab)
            }
        }
    }

    // Panel OFF reason dialog
    if (showPanelOffDialog) {
        OffReasonDialog(
            title = "Going offline?",
            subtitle = "Select a reason so customers are informed",
            reasons = partnerType.offReasons,
            onConfirm = { reason ->
                showPanelOffDialog = false
                onPanelToggle(false, reason)
            },
            onDismiss = { showPanelOffDialog = false }
        )
    }
}

enum class PanelTab(val label: String, val icon: ImageVector) {
    ORDERS("Orders", Icons.Filled.List),
    HISTORY("Order History", Icons.Filled.History),
    PAYMENTS("Payments", Icons.Filled.AccountBalanceWallet),
    PROFILE("My Profile", Icons.Filled.Person)
}
