package com.example.studentsdaily.ui.navigation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.studentsdaily.ui.screens.ExpensesScreen
import com.example.studentsdaily.ui.screens.HabitsNotesScreen
import com.example.studentsdaily.ui.screens.TasksScreen
import com.example.studentsdaily.ui.screens.TimetableScreen
import com.example.studentsdaily.ui.theme.StudentsDailyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsDailyMainApp(
    modifier: Modifier = Modifier
) {
    // Navigation 3 BackStack
    val backStack = rememberNavBackStack(ScreenRoute.Expenses)
    val currentRoute = backStack.lastOrNull() as? ScreenRoute ?: ScreenRoute.Expenses

    // Adaptive window width size class
    @Suppress("DEPRECATION")
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    @Suppress("DEPRECATION")
    val isCompactWidth = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    val dateFormatted = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = "StudentsDaily Branding",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Hello, Student! 🎓",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentRoute.title} • $dateFormatted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isCompactWidth) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    ScreenRoute.ALL.forEach { route ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    backStack.add(route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = route.icon,
                                    contentDescription = route.title
                                )
                            },
                            label = {
                                Text(
                                    text = route.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (currentRoute == route) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isCompactWidth) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    ScreenRoute.ALL.forEach { route ->
                        NavigationRailItem(
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    backStack.add(route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = route.icon,
                                    contentDescription = route.title
                                )
                            },
                            label = { Text(route.title) }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        }
                    }
                ) { key ->
                    NavEntry(key = key) { currentKey ->
                        when (currentKey) {
                            is ScreenRoute.Expenses -> ExpensesScreen()
                            is ScreenRoute.Timetable -> TimetableScreen()
                            is ScreenRoute.Tasks -> TasksScreen()
                            is ScreenRoute.HabitsNotes -> HabitsNotesScreen()
                            else -> ExpensesScreen()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun StudentsDailyMainAppPhonePreview() {
    StudentsDailyTheme {
        StudentsDailyMainApp()
    }
}

@Preview(showBackground = true, widthDp = 840, heightDp = 600)
@Composable
fun StudentsDailyMainAppTabletPreview() {
    StudentsDailyTheme {
        StudentsDailyMainApp()
    }
}
