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
    val searchQuery: String = "",
    val filteredItems: List<TimetableItem> = emptyList()
)

class TimetableViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    private val _selectedDay = MutableStateFlow("All")
    val selectedDay: StateFlow<String> = _selectedDay

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<TimetableUiState> = combine(
        repository.timetableItems,
        _selectedDay,
        _searchQuery
    ) { items, day, query ->
        val q = query.trim().lowercase()
        val filtered = items.filter { item ->
            val matchesDay = day == "All" || item.dayOfWeek.equals(day, ignoreCase = true)
            val matchesSearch = q.isEmpty() ||
                    item.subject.lowercase().contains(q) ||
                    item.instructor.lowercase().contains(q) ||
                    item.room.lowercase().contains(q)
            matchesDay && matchesSearch
        }
        TimetableUiState(
            timetableItems = items,
            selectedDay = day,
            searchQuery = query,
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
