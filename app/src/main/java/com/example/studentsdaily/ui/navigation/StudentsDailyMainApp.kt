package com.example.studentsdaily.ui.navigation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.studentsdaily.data.local.AuthPreferences
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import com.example.studentsdaily.ui.screens.ExpensesScreen
import com.example.studentsdaily.ui.screens.HabitsNotesScreen
import com.example.studentsdaily.ui.screens.IntroScreen
import com.example.studentsdaily.ui.screens.LoginScreen
import com.example.studentsdaily.ui.screens.NoteDetailScreen
import com.example.studentsdaily.ui.screens.ProfileScreen
import com.example.studentsdaily.ui.screens.TasksScreen
import com.example.studentsdaily.ui.screens.TimetableScreen
import com.example.studentsdaily.ui.theme.StudentsDailyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsDailyMainApp(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember {
        StudentsDailyRepository.getInstance().apply {
            initializeWithContext(context)
        }
    }

    val currentSession by repository.currentSession.collectAsState()

    // Determine startup route
    val hasCompletedIntro = remember { AuthPreferences.isIntroCompleted(context) }
    val initialRoute = remember {
        val session = repository.currentSession.value
        if (!hasCompletedIntro) {
            ScreenRoute.Intro
        } else if (session == null) {
            ScreenRoute.Login
        } else {
            ScreenRoute.Expenses
        }
    }

    // Navigation 3 BackStack
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull() as? ScreenRoute ?: initialRoute

    // Adaptive window width size class
    @Suppress("DEPRECATION")
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    @Suppress("DEPRECATION")
    val isCompactWidth = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    val dateFormatted = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val greetingText = remember(currentSession) {
        val user = currentSession?.user
        if (user != null) {
            val name = user.fullName.ifBlank { user.email.substringBefore("@") }
            "Hello, $name! 🎓"
        } else {
            "Hello, Student! 🎓"
        }
    }

    val showTopBar = currentRoute != ScreenRoute.Intro
    val showBottomBar = isCompactWidth && currentRoute in ScreenRoute.NAVIGATION_ITEMS
    val showNavRail = !isCompactWidth && currentRoute in ScreenRoute.NAVIGATION_ITEMS

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (currentRoute != ScreenRoute.Profile) {
                                        backStack.add(ScreenRoute.Profile)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
                                    text = greetingText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentRoute.title} • $dateFormatted",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentSession != null) {
                            AssistChip(
                                onClick = {
                                    if (currentRoute != ScreenRoute.Profile) {
                                        backStack.add(ScreenRoute.Profile)
                                    }
                                },
                                label = {
                                    Text(
                                        currentSession?.user?.fullName?.ifBlank {
                                            currentSession?.user?.email?.substringBefore("@")
                                        } ?: "Profile",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "User Profile",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.padding(end = 8.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        } else {
                            AssistChip(
                                onClick = {
                                    if (currentRoute != ScreenRoute.Profile) {
                                        backStack.add(ScreenRoute.Profile)
                                    }
                                },
                                label = { Text("Profile") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = "Profile",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (currentRoute != ScreenRoute.Login) {
                                        backStack.add(ScreenRoute.Login)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Login,
                                    contentDescription = "Sign In",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    ScreenRoute.NAVIGATION_ITEMS.forEach { route ->
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
            if (showNavRail) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    ScreenRoute.NAVIGATION_ITEMS.forEach { route ->
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
                            is ScreenRoute.Intro -> IntroScreen(
                                onCompleteIntro = {
                                    if (backStack.size > 0) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                    val session = repository.currentSession.value
                                    if (session != null) {
                                        backStack.add(ScreenRoute.Expenses)
                                    } else {
                                        backStack.add(ScreenRoute.Login)
                                    }
                                }
                            )
                            is ScreenRoute.Expenses -> ExpensesScreen()
                            is ScreenRoute.Timetable -> TimetableScreen()
                            is ScreenRoute.Tasks -> TasksScreen()
                            is ScreenRoute.HabitsNotes -> HabitsNotesScreen(
                                onNoteClick = { noteId ->
                                    backStack.add(ScreenRoute.NoteDetail(noteId))
                                }
                            )
                            is ScreenRoute.NoteDetail -> NoteDetailScreen(
                                noteId = currentKey.noteId,
                                onBack = {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                }
                            )
                            is ScreenRoute.Profile -> ProfileScreen(
                                onNavigateToLogin = {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                    backStack.add(ScreenRoute.Login)
                                }
                            )
                            is ScreenRoute.Login -> LoginScreen(
                                onContinueAsGuest = {
                                    if (backStack.size > 0) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                    if (backStack.isEmpty()) {
                                        backStack.add(ScreenRoute.Expenses)
                                    }
                                },
                                onAuthSuccess = {
                                    if (backStack.size > 0) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                    if (backStack.isEmpty()) {
                                        backStack.add(ScreenRoute.Expenses)
                                    } else {
                                        backStack.add(ScreenRoute.Profile)
                                    }
                                }
                            )
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
