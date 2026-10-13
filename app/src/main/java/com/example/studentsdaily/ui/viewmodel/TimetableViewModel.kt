package com.example.studentsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentsdaily.data.model.TimetableItem
import com.example.studentsdaily.data.model.TimetableType
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TimetableUiState(
    val timetableItems: List<TimetableItem> = emptyList(),
    val selectedDay: String = "All",
    val filteredItems: List<TimetableItem> = emptyList()
)

class TimetableViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    private val _selectedDay = MutableStateFlow("All")
    val selectedDay: StateFlow<String> = _selectedDay

    val uiState: StateFlow<TimetableUiState> = combine(
        repository.timetableItems,
        _selectedDay
    ) { items, day ->
        val filtered = if (day == "All") {
            items
        } else {
            items.filter { it.dayOfWeek.equals(day, ignoreCase = true) }
        }
        TimetableUiState(
            timetableItems = items,
            selectedDay = day,
            filteredItems = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimetableUiState()
    )

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    fun addTimetableItem(
        subject: String,
        instructor: String,
        room: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        type: TimetableType
    ) {
        if (subject.isBlank()) return
        val item = TimetableItem(
            subject = subject.trim(),
            instructor = instructor.trim(),
            room = room.trim(),
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            type = type
        )
        repository.addTimetableItem(item)
    }

    fun deleteTimetableItem(id: String) {
        repository.deleteTimetableItem(id)
    }
}
