package com.example.studentsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HabitNoteUiState(
    val habits: List<Habit> = emptyList(),
    val notes: List<Note> = emptyList(),
    val completedHabitsToday: Int = 0,
    val totalHabits: Int = 0
)

class HabitNoteViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    val uiState: StateFlow<HabitNoteUiState> = combine(
        repository.habits,
        repository.notes
    ) { habits, notes ->
        val sortedNotes = notes.sortedWith(
            compareByDescending<Note> { it.isPinned }
        )
        HabitNoteUiState(
            habits = habits,
            notes = sortedNotes,
            completedHabitsToday = habits.count { it.isCompletedToday },
            totalHabits = habits.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitNoteUiState()
    )

    fun toggleHabitCompletion(id: String) {
        repository.toggleHabitCompletion(id)
    }

    fun addHabit(title: String, targetDays: Int) {
        if (title.isBlank() || targetDays <= 0) return
        val habit = Habit(
            title = title.trim(),
            targetDays = targetDays
        )
        repository.addHabit(habit)
    }

    fun deleteHabit(id: String) {
        repository.deleteHabit(id)
    }

    fun addNote(title: String, content: String, categoryTag: String, dateString: String = "Today") {
        if (title.isBlank()) return
        val note = Note(
            title = title.trim(),
            content = content.trim(),
            categoryTag = categoryTag.ifBlank { "General" },
            dateString = dateString
        )
        repository.addNote(note)
    }

    fun updateNote(id: String, title: String, content: String, categoryTag: String, dateString: String = "Today") {
        if (title.isBlank()) return
        val existing = repository.notes.value.find { it.id == id }
        val updatedNote = Note(
            id = id,
            title = title.trim(),
            content = content.trim(),
            categoryTag = categoryTag.ifBlank { "General" },
            dateString = dateString,
            isPinned = existing?.isPinned ?: false
        )
        repository.updateNote(updatedNote)
    }

    fun toggleNotePin(id: String) {
        repository.toggleNotePin(id)
    }

    fun deleteNote(id: String) {
        repository.deleteNote(id)
    }
}
