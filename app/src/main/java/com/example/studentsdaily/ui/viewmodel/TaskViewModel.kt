package com.example.studentsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

data class TaskUiState(
    val tasks: List<TaskItem> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val filteredTasks: List<TaskItem> = emptyList(),
    val pendingCount: Int = 0,
    val completedCount: Int = 0
)

class TaskViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    val uiState: StateFlow<TaskUiState> = combine(
        repository.tasks,
        _filter
    ) { tasks, filter ->
        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
        TaskUiState(
            tasks = tasks,
            filter = filter,
            filteredTasks = filtered,
            pendingCount = tasks.count { !it.isCompleted },
            completedCount = tasks.count { it.isCompleted }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun addTask(title: String, subject: String, dueDate: String, priority: Priority) {
        if (title.isBlank()) return
        val task = TaskItem(
            title = title.trim(),
            subject = subject.trim(),
            dueDate = dueDate.ifBlank { "Today" },
            priority = priority
        )
        repository.addTask(task)
    }

    fun toggleTaskCompletion(id: String) {
        repository.toggleTaskCompletion(id)
    }

    fun deleteTask(id: String) {
        repository.deleteTask(id)
    }
}
