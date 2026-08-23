package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.navigation.NavItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationLayout(
    user: User?,
    currentNav: NavItem,
    unreadNotificationCount: Int = 0,
    onNavSelected: (NavItem) -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // Tablet & Desktop Layout with Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(230.dp)
                        .background(Color.White)
                        .testTag("app_navigation_rail"),
                    containerColor = Color.White,
                    header = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "InvoiceAI",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                    Text(
                                        text = "Sistem Faktur AI",
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            SubscriptionBadge(tier = user?.packageTier ?: "Gratis")
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp)
                    ) {
                        NavItem.values().forEach { item ->
                            val isSelected = item == currentNav
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                selected = isSelected,
                                onClick = { onNavSelected(item) },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = PrimaryBlueLight,
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    unselectedIconColor = Slate700,
                                    unselectedTextColor = Slate700
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .testTag("nav_item_${item.name.lowercase()}")
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(8.dp))

                        NavigationDrawerItem(
                            label = { Text("Keluar", fontSize = 13.5.sp, fontWeight = FontWeight.Medium) },
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar", modifier = Modifier.size(20.dp)) },
                            selected = false,
                            onClick = onLogoutClick,
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedIconColor = Color(0xFFF53F3F),
                                unselectedTextColor = Color(0xFFF53F3F)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("nav_item_logout")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Main Content for Wide Screen
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = currentNav.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    user?.businessName?.takeIf { it.isNotBlank() }?.let { bName ->
                                        Text(
                                            text = bName,
                                            fontSize = 11.5.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = onNotificationClick,
                                    modifier = Modifier.testTag("notification_bell_btn_wide")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotificationCount > 0) {
                                                Badge(
                                                    containerColor = DangerRed,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (unreadNotificationCount > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                                            contentDescription = "Pemberitahuan",
                                            tint = if (unreadNotificationCount > 0) PrimaryBlue else Slate700
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                SubscriptionBadge(
                                    tier = user?.packageTier ?: "Gratis",
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White
                            )
                        )
                    },
                    containerColor = Slate50
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        content()
                    }
                }
            }
        } else {
            // Mobile (Compact) Layout with Modal Navigation Drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(290.dp)
                            .testTag("app_navigation_drawer"),
                        drawerContainerColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryBlue)
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Logo",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "InvoiceAI",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sistem Faktur & Penjualan AI",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = user?.businessName?.ifEmpty { "Bisnis Anda" } ?: "Bisnis Anda",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = user?.email ?: "user@invoiceai.id",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            SubscriptionBadge(tier = user?.packageTier ?: "Gratis")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp)
                        ) {
                            NavItem.values().forEach { item ->
                                val isSelected = item == currentNav
                                NavigationDrawerItem(
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        onNavSelected(item)
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = PrimaryBlueLight,
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        unselectedIconColor = Slate700,
                                        unselectedTextColor = Slate700
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .padding(vertical = 3.dp)
                                        .testTag("nav_item_${item.name.lowercase()}")
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            HorizontalDivider(color = Slate200)
                            Spacer(modifier = Modifier.height(8.dp))

                            NavigationDrawerItem(
                                label = { Text("Keluar / Logout", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar", modifier = Modifier.size(22.dp)) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onLogoutClick()
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedIconColor = Color(0xFFF53F3F),
                                    unselectedTextColor = Color(0xFFF53F3F)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .testTag("nav_item_logout")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = currentNav.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    user?.businessName?.takeIf { it.isNotBlank() }?.let { bName ->
                                        Text(
                                            text = bName,
                                            fontSize = 11.5.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("nav_drawer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Buka Menu",
                                        tint = Slate900
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = onNotificationClick,
                                    modifier = Modifier.testTag("notification_bell_btn_mobile")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotificationCount > 0) {
                                                Badge(
                                                    containerColor = DangerRed,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (unreadNotificationCount > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                                            contentDescription = "Pemberitahuan",
                                            tint = if (unreadNotificationCount > 0) PrimaryBlue else Slate700
                                        )
                                    }
                                }

                                SubscriptionBadge(
                                    tier = user?.packageTier ?: "Gratis",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White
                            )
                        )
                    },
                    containerColor = Slate50
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        content()
                    }
                }
            }
        }
    }
}
