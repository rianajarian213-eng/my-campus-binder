package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AddCourseDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.CourseBinderDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.MyBindersScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BinderCreamBg
import com.example.ui.theme.BinderEspresso
import com.example.ui.theme.BinderGoldRing
import com.example.ui.theme.BinderPaperCard
import com.example.ui.theme.BinderPaperSand
import com.example.ui.theme.BinderTextPrimary
import com.example.ui.theme.BinderTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BinderViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: BinderViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isSearching by viewModel.isGlobalSearching.collectAsState()
    val selectedCourseId by viewModel.selectedCourseId.collectAsState()
    val currentCourse by viewModel.currentCourse.collectAsState()
    val selectedPageId by viewModel.selectedPageId.collectAsState()
    val isCreatingPage by viewModel.isCreatingPage.collectAsState()

    var showAddCourseDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BinderCreamBg
    ) {
        when {
            // 1. Global Search Screen
            isSearching -> {
                GlobalSearchScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.setGlobalSearching(false) }
                )
            }

            // 2. Note Editor (Inside Binder)
            selectedCourseId != null && (isCreatingPage || selectedPageId != null) -> {
                NoteEditorScreen(
                    courseId = selectedCourseId!!,
                    pageId = selectedPageId,
                    viewModel = viewModel,
                    onBack = { viewModel.closePageEditor() }
                )
            }

            // 3. Course Binder Detail (Physical Binder Book View)
            selectedCourseId != null && currentCourse != null -> {
                CourseBinderDetailScreen(
                    course = currentCourse!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeCourseBinder() }
                )
            }

            // 4. Main 5-Tab Application with Bottom Navigation
            else -> {
                Scaffold(
                    bottomBar = {
                        BinderBottomNavigationBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    },
                    containerColor = BinderCreamBg
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            ScreenTab.HOME -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onOpenAddCourse = { showAddCourseDialog = true }
                                )
                            }
                            ScreenTab.BINDERS -> {
                                MyBindersScreen(
                                    viewModel = viewModel,
                                    onOpenAddCourse = { showAddCourseDialog = true }
                                )
                            }
                            ScreenTab.RECORD -> {
                                com.example.ui.screens.RecordScreen(viewModel = viewModel)
                            }
                            ScreenTab.AI_ASSISTANT -> {
                                AiAssistantScreen(viewModel = viewModel)
                            }
                            ScreenTab.PROFILE -> {
                                ProfileScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }

        // Add Course Dialog modal
        if (showAddCourseDialog) {
            AddCourseDialog(
                viewModel = viewModel,
                onDismiss = { showAddCourseDialog = false }
            )
        }
    }
}

@Composable
fun BinderBottomNavigationBar(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("main_bottom_nav"),
        containerColor = BinderPaperCard,
        tonalElevation = 6.dp
    ) {
        val navItems = listOf(
            Triple(ScreenTab.HOME, "Beranda", Icons.Default.Home),
            Triple(ScreenTab.BINDERS, "Binder Saya", Icons.Default.Book),
            Triple(ScreenTab.RECORD, "Rekam", Icons.Default.Mic),
            Triple(ScreenTab.AI_ASSISTANT, "AI Resume", Icons.Default.AutoAwesome),
            Triple(ScreenTab.PROFILE, "Profil", Icons.Default.Person)
        )

        navItems.forEach { (tab, label, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = BinderEspresso,
                    indicatorColor = BinderEspresso,
                    unselectedIconColor = BinderTextSecondary,
                    unselectedTextColor = BinderTextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
