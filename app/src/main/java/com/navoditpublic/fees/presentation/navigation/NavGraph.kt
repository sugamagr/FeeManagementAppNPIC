package com.navoditpublic.fees.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.navoditpublic.fees.presentation.screens.dashboard.DashboardViewModel
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.presentation.screens.dashboard.DashboardScreen
import com.navoditpublic.fees.presentation.screens.students.list.StudentsScreen
import com.navoditpublic.fees.presentation.screens.students.list.StudentListByClassScreen
import com.navoditpublic.fees.presentation.screens.students.detail.StudentDetailScreen
import com.navoditpublic.fees.presentation.screens.students.add_edit.AddEditStudentScreen
import com.navoditpublic.fees.presentation.screens.fee_collection.FeeCollectionScreen
import com.navoditpublic.fees.presentation.screens.fee_collection.collect.CollectFeeScreen
import com.navoditpublic.fees.presentation.screens.ledger.StudentLedgerScreen
import com.navoditpublic.fees.presentation.screens.receipts.StudentReceiptsScreen
import com.navoditpublic.fees.presentation.screens.ledger.main.LedgerMainScreen
import com.navoditpublic.fees.presentation.screens.ledger.main.LedgerClassScreen
import com.navoditpublic.fees.presentation.screens.reports.ReportsScreen
import com.navoditpublic.fees.presentation.screens.settings.AboutScreen
import com.navoditpublic.fees.presentation.screens.settings.HowToUseScreen
import com.navoditpublic.fees.presentation.screens.settings.SettingsScreen
import com.navoditpublic.fees.presentation.screens.settings.school_profile.SchoolProfileScreen
import com.navoditpublic.fees.presentation.screens.settings.sessions.AcademicSessionsScreen
import com.navoditpublic.fees.presentation.screens.settings.fee_structure.FeeStructureScreen
import com.navoditpublic.fees.presentation.screens.settings.transport.TransportRoutesScreen
import com.navoditpublic.fees.presentation.screens.settings.audit.AuditLogScreen
import com.navoditpublic.fees.presentation.screens.settings.classes.ClassesSectionsScreen
import com.navoditpublic.fees.presentation.screens.receipt.ReceiptDetailScreen
import com.navoditpublic.fees.presentation.screens.reports.daily.DailyCollectionScreen
import com.navoditpublic.fees.presentation.screens.reports.monthly.MonthlyCollectionScreen
import com.navoditpublic.fees.presentation.screens.reports.defaulters.DefaultersScreen
import com.navoditpublic.fees.presentation.screens.reports.classwise.ClassWiseScreen
import com.navoditpublic.fees.presentation.screens.reports.register.ReceiptRegisterScreen
import com.navoditpublic.fees.presentation.screens.reports.custom.CustomReportScreen
import com.navoditpublic.fees.presentation.screens.reports.dues.CustomDuesReportScreen
import com.navoditpublic.fees.presentation.screens.reports.dues.SavedViewsScreen
import com.navoditpublic.fees.presentation.screens.transport.TransportQuickScreen
import kotlinx.coroutines.launch

// Local composition for drawer state access
val LocalDrawerState = compositionLocalOf<androidx.compose.material3.DrawerState> { 
    error("No DrawerState provided") 
}

data class BottomNavItem(
    val graphRoute: String,      // The navigation graph route
    val startRoute: String,      // Start destination within the graph
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenterFab: Boolean = false
)

val bottomNavItems = listOf(
    BottomNavItem(
        graphRoute = Screen.HomeGraph.route,
        startRoute = Screen.Dashboard.route,
        title = "Home",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    BottomNavItem(
        graphRoute = Screen.StudentsGraph.route,
        startRoute = Screen.Students.route,
        title = "Students",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    ),
    BottomNavItem(
        graphRoute = Screen.CollectGraph.route,
        startRoute = Screen.CollectFee.createRoute(),
        title = "Collect",
        selectedIcon = Icons.Filled.Payment,
        unselectedIcon = Icons.Outlined.Payment,
        isCenterFab = true // This will be the elevated FAB - goes directly to collect form
    ),
    BottomNavItem(
        graphRoute = Screen.LedgerGraph.route,
        startRoute = Screen.Ledger.route,
        title = "Ledger",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    BottomNavItem(
        graphRoute = Screen.ReportsGraph.route,
        startRoute = Screen.Reports.route,
        title = "Reports",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )
)

@Composable
fun FeesNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Get current session for drawer header
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val dashboardState by dashboardViewModel.state.collectAsState()
    val currentSession = dashboardState.currentSession?.sessionName
    
    // Determine if bottom nav should be shown (on main tab screens)
    val mainTabRoutes = bottomNavItems.map { it.startRoute }
    val showBottomNav = currentDestination?.route in mainTabRoutes
    
    // Determine if drawer gesture should be enabled (only on main screens)
    val enableDrawerGesture = showBottomNav
    
    CompositionLocalProvider(LocalDrawerState provides drawerState) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = enableDrawerGesture,
            drawerContent = {
                AppDrawerContent(
                    navController = navController,
                    drawerState = drawerState,
                    currentSession = currentSession,
                    currentRoute = currentDestination?.route
                )
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomNav) {
                        ModernBottomNavBar(
                            navController = navController,
                            currentRoute = currentDestination?.route
                        )
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.HomeGraph.route,
                    modifier = Modifier.padding(paddingValues),
                    enterTransition = {
                        fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    // ==================== HOME TAB GRAPH ====================
                    navigation(
                        startDestination = Screen.Dashboard.route,
                        route = Screen.HomeGraph.route
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(navController = navController)
                        }
                        
                        // Receipt Detail (accessible from dashboard)
                        composable(
                            route = Screen.ReceiptDetail.route,
                            arguments = listOf(navArgument("receiptId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val receiptId = backStackEntry.arguments?.getLong("receiptId") ?: return@composable
                            ReceiptDetailScreen(
                                receiptId = receiptId,
                                navController = navController
                            )
                        }
                        
                        // Fee Collection (Recent Receipts - accessible from dashboard)
                        composable(Screen.FeeCollection.route) {
                            FeeCollectionScreen(navController = navController)
                        }
                        
                        // Transport Quick Management (accessible from dashboard)
                        composable(
                            route = Screen.TransportQuick.route,
                            arguments = listOf(
                                navArgument("studentId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                },
                                navArgument("action") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { backStackEntry ->
                            val studentId = backStackEntry.arguments?.getLong("studentId") ?: -1L
                            val action = backStackEntry.arguments?.getString("action") ?: ""
                            TransportQuickScreen(
                                navController = navController,
                                preSelectedStudentId = if (studentId > 0) studentId else null,
                                preSelectedAction = action.ifBlank { null }
                            )
                        }
                    }
                    
                    // ==================== STUDENTS TAB GRAPH ====================
                    navigation(
                        startDestination = Screen.Students.route,
                        route = Screen.StudentsGraph.route
                    ) {
                        composable(Screen.Students.route) {
                            StudentsScreen(navController = navController)
                        }
                        
                        composable(
                            route = Screen.StudentListByClass.route,
                            arguments = listOf(
                                navArgument("className") { type = NavType.StringType },
                                navArgument("section") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val className = backStackEntry.arguments?.getString("className") ?: return@composable
                            val section = backStackEntry.arguments?.getString("section") ?: return@composable
                            StudentListByClassScreen(
                                className = className,
                                section = section,
                                navController = navController
                            )
                        }
                        
                        composable(
                            route = Screen.StudentDetail.route,
                            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
                            StudentDetailScreen(
                                studentId = studentId,
                                navController = navController
                            )
                        }
                        
                        composable(
                            route = Screen.AddEditStudent.route,
                            arguments = listOf(
                                navArgument("studentId") { 
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val studentIdString = backStackEntry.arguments?.getString("studentId")
                            val studentId = studentIdString?.toLongOrNull()
                            AddEditStudentScreen(
                                studentId = studentId,
                                navController = navController
                            )
                        }
                        
                        composable(
                            route = Screen.StudentLedger.route,
                            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
                            StudentLedgerScreen(
                                studentId = studentId,
                                navController = navController
                            )
                        }
                        
                        composable(
                            route = Screen.StudentReceipts.route,
                            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
                        ) {
                            StudentReceiptsScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // ==================== COLLECT TAB GRAPH ====================
                    navigation(
                        startDestination = Screen.CollectFee.createRoute(),
                        route = Screen.CollectGraph.route
                    ) {
                        composable(
                            route = Screen.CollectFee.route,
                            arguments = listOf(
                                navArgument("studentId") { 
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val studentIdString = backStackEntry.arguments?.getString("studentId")
                            val studentId = studentIdString?.toLongOrNull()
                            CollectFeeScreen(
                                preSelectedStudentId = studentId,
                                navController = navController
                            )
                        }
                    }
                    
                    // ==================== LEDGER TAB GRAPH ====================
                    navigation(
                        startDestination = Screen.Ledger.route,
                        route = Screen.LedgerGraph.route
                    ) {
                        composable(Screen.Ledger.route) {
                            LedgerMainScreen(navController = navController)
                        }
                        
                        composable(
                            route = Screen.LedgerClass.route,
                            arguments = listOf(navArgument("className") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val className = backStackEntry.arguments?.getString("className") ?: return@composable
                            LedgerClassScreen(
                                className = className,
                                navController = navController
                            )
                        }
                    }
                    
                    // ==================== REPORTS TAB GRAPH ====================
                    navigation(
                        startDestination = Screen.Reports.route,
                        route = Screen.ReportsGraph.route
                    ) {
                        composable(Screen.Reports.route) {
                            ReportsScreen(navController = navController)
                        }
                        
                        // Report Screens - Collection
                        composable(
                            route = Screen.DailyCollectionReport.route,
                            arguments = listOf(
                                navArgument("date") {
                                    type = NavType.LongType
                                    defaultValue = 0L
                                }
                            )
                        ) { backStackEntry ->
                            val date = backStackEntry.arguments?.getLong("date") ?: 0L
                            DailyCollectionScreen(
                                navController = navController,
                                initialDate = if (date > 0L) date else null
                            )
                        }
                        
                        composable(Screen.MonthlyCollectionReport.route) {
                            MonthlyCollectionScreen(navController = navController)
                        }
                        
                        composable(Screen.ReceiptRegister.route) {
                            ReceiptRegisterScreen(navController = navController)
                        }
                        
                        composable(Screen.CustomReport.route) {
                            CustomReportScreen(navController = navController)
                        }
                        
                        composable(Screen.CustomCollectionReport.route) {
                            CustomReportScreen(navController = navController)
                        }
                        
                        // Report Screens - Dues
                        composable(Screen.DefaultersReport.route) {
                            DefaultersScreen(navController = navController)
                        }
                        
                        composable(Screen.ClassWiseReport.route) {
                            ClassWiseScreen(navController = navController)
                        }
                        
                        composable(Screen.ClassWiseDuesReport.route) {
                            ClassWiseScreen(navController = navController)
                        }
                        
                        composable(Screen.TransportDuesReport.route) {
                            com.navoditpublic.fees.presentation.screens.reports.transport.TransportDuesScreen(navController = navController)
                        }
                        
                        composable(Screen.CustomDuesReport.route) {
                            CustomDuesReportScreen(navController = navController)
                        }
                        
                        composable(Screen.SavedDuesViews.route) {
                            SavedViewsScreen(navController = navController)
                        }
                    }
                    
                    // ==================== SETTINGS (Global - not part of any tab) ====================
                    composable(Screen.Settings.route) {
                        SettingsScreen(navController = navController)
                    }
                    
                    composable(Screen.SchoolProfile.route) {
                        SchoolProfileScreen(navController = navController)
                    }
                    
                    composable(Screen.AcademicSessions.route) {
                        AcademicSessionsScreen(navController = navController)
                    }
                    
                    composable(Screen.FeeStructure.route) {
                        FeeStructureScreen(navController = navController)
                    }
                    
                    composable(Screen.TransportRoutes.route) {
                        TransportRoutesScreen(navController = navController)
                    }
                    
                    composable(Screen.AuditLog.route) {
                        AuditLogScreen(navController = navController)
                    }
                    
                    composable(Screen.ClassesAndSections.route) {
                        ClassesSectionsScreen(navController = navController)
                    }
                    
                    composable(Screen.HowToUse.route) {
                        HowToUseScreen(navController = navController)
                    }
                    
                    composable(Screen.About.route) {
                        AboutScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernBottomNavBar(
    navController: NavController,
    currentRoute: String?
) {
    val collectItem = bottomNavItems.find { it.isCenterFab }
    val isCollectSelected = currentRoute == collectItem?.startRoute
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                if (item.isCenterFab) {
                    // Center FAB
                    val scale by animateFloatAsState(
                        targetValue = if (isCollectSelected) 1.08f else 1f,
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        label = "fab_scale"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // Navigate to collect tab graph with state save/restore
                                navController.navigate(item.graphRoute) {
                                    // Pop up to the start destination to avoid building up a large stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .scale(scale)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Saffron, SaffronDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (isCollectSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCollectSelected) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val selected = currentRoute == item.startRoute
                    ModernNavItem(
                        item = item,
                        selected = selected,
                        onClick = {
                            // Navigate to the tab's graph with state save/restore
                            navController.navigate(item.graphRoute) {
                                // Pop up to the start destination to avoid building up a large stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(150),
        label = "item_scale"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selected) Saffron.copy(alpha = 0.15f) 
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title,
                tint = if (selected) Saffron else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

