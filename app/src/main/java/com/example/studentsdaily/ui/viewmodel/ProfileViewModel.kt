package com.example.studentsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentsdaily.data.remote.SupabaseSession
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileStats(
    val expensesCount: Int = 0,
    val expensesAmount: Double = 0.0,
    val tasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val completedTasksPct: Int = 0,
    val habitsCount: Int = 0,
    val habitStreak: Int = 0,
    val timetableCount: Int = 0,
    val notesCount: Int = 0
)

data class UserState(
    val session: SupabaseSession?,
    val isGuest: Boolean,
    val isEditingName: Boolean,
    val nameInput: String,
    val updateMessage: String?
)

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val userId: String = "",
    val isGuest: Boolean = true,
    val isEditingName: Boolean = false,
    val nameInput: String = "",
    val updateMessage: String? = null,
    val isUpdating: Boolean = false,
    val totalExpensesCount: Int = 0,
    val totalExpensesAmount: Double = 0.0,
    val totalTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val completedTasksPercentage: Int = 0,
    val totalHabitsCount: Int = 0,
    val totalHabitStreak: Int = 0,
    val totalTimetableCount: Int = 0,
    val totalNotesCount: Int = 0
)

class ProfileViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    private val _isEditingName = MutableStateFlow(false)
    private val _nameInput = MutableStateFlow("")
    private val _updateMessage = MutableStateFlow<String?>(null)
    private val _isUpdating = MutableStateFlow(false)

    private val statsFlow: StateFlow<ProfileStats> = combine(
        repository.expenses,
        repository.tasks,
        repository.habits,
        repository.timetableItems,
        repository.notes
    ) { expenses, tasks, habits, timetable, notes ->
        val tasksCount = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pct = if (tasksCount > 0) (completed * 100) / tasksCount else 0
        ProfileStats(
            expensesCount = expenses.size,
            expensesAmount = expenses.sumOf { it.amount },
            tasksCount = tasksCount,
            completedTasksCount = completed,
            completedTasksPct = pct,
            habitsCount = habits.size,
            habitStreak = habits.sumOf { it.streak },
            timetableCount = timetable.size,
            notesCount = notes.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileStats()
    )

    private val userStateFlow = combine(
        repository.currentSession,
        repository.isGuestMode,
        _isEditingName,
        _nameInput,
        _updateMessage
    ) { session, isGuest, isEditing, nameInput, msg ->
        UserState(
            session = session,
            isGuest = isGuest,
            isEditingName = isEditing,
            nameInput = nameInput,
            updateMessage = msg
        )
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        userStateFlow,
        statsFlow,
        _isUpdating
    ) { userState, stats, updating ->
        val session = userState.session
        val user = session?.user
        val rawName = user?.fullName?.ifBlank { user.email.substringBefore("@") } ?: "Guest Student"
        val email = user?.email ?: "guest@studentsdaily.app"
        val uid = user?.id ?: "GUEST-DEMO-MODE"

        ProfileUiState(
            fullName = rawName,
            email = email,
            userId = uid,
            isGuest = userState.isGuest,
            isEditingName = userState.isEditingName,
            nameInput = if (userState.isEditingName) userState.nameInput else rawName,
            updateMessage = userState.updateMessage,
            isUpdating = updating,
            totalExpensesCount = stats.expensesCount,
            totalExpensesAmount = stats.expensesAmount,
            totalTasksCount = stats.tasksCount,
            completedTasksCount = stats.completedTasksCount,
            completedTasksPercentage = stats.completedTasksPct,
            totalHabitsCount = stats.habitsCount,
            totalHabitStreak = stats.habitStreak,
            totalTimetableCount = stats.timetableCount,
            totalNotesCount = stats.notesCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun startEditingName() {
        _nameInput.value = uiState.value.fullName
        _isEditingName.value = true
    }

    fun cancelEditingName() {
        _isEditingName.value = false
        _updateMessage.value = null
    }

    fun onNameInputChange(newInput: String) {
        _nameInput.value = newInput
    }

    fun saveName() {
        val newName = _nameInput.value.trim()
        if (newName.isBlank()) return
        _isUpdating.value = true
        _updateMessage.value = null

        repository.updateFullName(newName) { _, message ->
            _isUpdating.value = false
            _updateMessage.value = message
            _isEditingName.value = false
        }
    }

    fun clearMessage() {
        _updateMessage.value = null
    }

    fun signOut() {
        repository.signOut()
    }
}
