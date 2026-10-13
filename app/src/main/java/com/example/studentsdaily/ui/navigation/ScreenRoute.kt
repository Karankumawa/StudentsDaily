package com.example.studentsdaily.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenRoute : NavKey {
    val title: String
    val icon: ImageVector

    @Serializable
    data object Intro : ScreenRoute {
        override val title: String = "Welcome"
        override val icon: ImageVector = Icons.Rounded.School
    }

    @Serializable
    data object Expenses : ScreenRoute {
        override val title: String = "Expenses"
        override val icon: ImageVector = Icons.AutoMirrored.Rounded.ReceiptLong
    }

    @Serializable
    data object Timetable : ScreenRoute {
        override val title: String = "Timetable"
        override val icon: ImageVector = Icons.Rounded.CalendarToday
    }

    @Serializable
    data object Tasks : ScreenRoute {
        override val title: String = "Tasks"
        override val icon: ImageVector = Icons.Rounded.CheckCircle
    }

    @Serializable
    data object HabitsNotes : ScreenRoute {
        override val title: String = "Habits & Notes"
        override val icon: ImageVector = Icons.AutoMirrored.Rounded.Notes
    }

    @Serializable
    data object Profile : ScreenRoute {
        override val title: String = "Profile"
        override val icon: ImageVector = Icons.Rounded.Person
    }

    @Serializable
    data object Login : ScreenRoute {
        override val title: String = "Account"
        override val icon: ImageVector = Icons.Rounded.AccountCircle
    }

    @Serializable
    data class NoteDetail(val noteId: String = "") : ScreenRoute {
        override val title: String = "Note Detail"
        @kotlinx.serialization.Transient
        override val icon: ImageVector = Icons.AutoMirrored.Rounded.Notes
    }

    companion object {
        val NAVIGATION_ITEMS: List<ScreenRoute> = listOf(Expenses, Timetable, Tasks, HabitsNotes, Profile)
        val ALL: List<ScreenRoute> = listOf(Intro, Expenses, Timetable, Tasks, HabitsNotes, Profile, Login, NoteDetail())
    }
}
