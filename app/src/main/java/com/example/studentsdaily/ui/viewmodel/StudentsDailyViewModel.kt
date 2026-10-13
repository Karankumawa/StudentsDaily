package com.example.studentsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.model.TimetableItem
import com.example.studentsdaily.data.model.TimetableType
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainDashboardUiState(
    val totalExpense: Double = 0.0,
    val pendingTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val todayHabitsCompleted: Int = 0,
    val totalHabits: Int = 0,
    val totalNotes: Int = 0,
    val recentTasks: List<TaskItem> = emptyList(),
    val recentExpenses: List<Expense> = emptyList()
)

class StudentsDailyViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository.expenses
    val timetableItems: StateFlow<List<TimetableItem>> = repository.timetableItems
    val tasks: StateFlow<List<TaskItem>> = repository.tasks
    val habits: StateFlow<List<Habit>> = repository.habits
    val notes: StateFlow<List<Note>> = repository.notes

    val dashboardUiState: StateFlow<MainDashboardUiState> = combine(
        expenses,
        tasks,
        habits,
        notes
    ) { expList, taskList, habitList, noteList ->
        MainDashboardUiState(
            totalExpense = expList.sumOf { it.amount },
            pendingTasksCount = taskList.count { !it.isCompleted },
            completedTasksCount = taskList.count { it.isCompleted },
            todayHabitsCompleted = habitList.count { it.isCompletedToday },
            totalHabits = habitList.size,
            totalNotes = noteList.size,
            recentTasks = taskList.take(5),
            recentExpenses = expList.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainDashboardUiState()
    )

    fun addExpense(title: String, amount: Double, category: ExpenseCategory, date: String, note: String = "") {
        if (title.isBlank() || amount <= 0) return
        repository.addExpense(Expense(title = title.trim(), amount = amount, category = category, date = date.ifBlank { "Today" }, note = note.trim()))
    }

    fun deleteExpense(id: String) = repository.deleteExpense(id)

    fun addTask(title: String, subject: String, dueDate: String, priority: Priority) {
        if (title.isBlank()) return
        repository.addTask(TaskItem(title = title.trim(), subject = subject.trim(), dueDate = dueDate.ifBlank { "Today" }, priority = priority))
    }

    fun toggleTaskCompletion(id: String) = repository.toggleTaskCompletion(id)

    fun deleteTask(id: String) = repository.deleteTask(id)

    fun addTimetableItem(subject: String, instructor: String, room: String, dayOfWeek: String, startTime: String, endTime: String, type: TimetableType) {
        if (subject.isBlank()) return
        repository.addTimetableItem(TimetableItem(subject = subject.trim(), instructor = instructor.trim(), room = room.trim(), dayOfWeek = dayOfWeek, startTime = startTime, endTime = endTime, type = type))
    }

    fun deleteTimetableItem(id: String) = repository.deleteTimetableItem(id)

    fun toggleHabitCompletion(id: String) = repository.toggleHabitCompletion(id)

    fun addHabit(title: String, targetDays: Int) {
        if (title.isBlank() || targetDays <= 0) return
        repository.addHabit(Habit(title = title.trim(), targetDays = targetDays))
    }

    fun deleteHabit(id: String) = repository.deleteHabit(id)

    fun addNote(title: String, content: String, categoryTag: String, dateString: String = "Today") {
        if (title.isBlank()) return
        repository.addNote(Note(title = title.trim(), content = content.trim(), categoryTag = categoryTag.ifBlank { "General" }, dateString = dateString))
    }

    fun deleteNote(id: String) = repository.deleteNote(id)
}
