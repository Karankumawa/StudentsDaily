package com.example.studentsdaily.data.repository

import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.model.TimetableItem
import com.example.studentsdaily.data.model.TimetableType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudentsDailyRepository private constructor() {

    private val _expenses = MutableStateFlow<List<Expense>>(initialExpenses())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _timetableItems = MutableStateFlow<List<TimetableItem>>(initialTimetableItems())
    val timetableItems: StateFlow<List<TimetableItem>> = _timetableItems.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskItem>>(initialTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(initialHabits())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(initialNotes())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Expense operations
    fun addExpense(expense: Expense) {
        _expenses.value = _expenses.value + expense
    }

    fun deleteExpense(id: String) {
        _expenses.value = _expenses.value.filterNot { it.id == id }
    }

    // Task operations
    fun addTask(task: TaskItem) {
        _tasks.value = _tasks.value + task
    }

    fun toggleTaskCompletion(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) {
                task.copy(isCompleted = !task.isCompleted)
            } else {
                task
            }
        }
    }

    fun deleteTask(id: String) {
        _tasks.value = _tasks.value.filterNot { it.id == id }
    }

    // Timetable operations
    fun addTimetableItem(item: TimetableItem) {
        _timetableItems.value = _timetableItems.value + item
    }

    fun deleteTimetableItem(id: String) {
        _timetableItems.value = _timetableItems.value.filterNot { it.id == id }
    }

    // Habit operations
    fun addHabit(habit: Habit) {
        _habits.value = _habits.value + habit
    }

    fun toggleHabitCompletion(id: String) {
        _habits.value = _habits.value.map { habit ->
            if (habit.id == id) {
                val newCompleted = !habit.isCompletedToday
                val newStreak = if (newCompleted) habit.streak + 1 else (habit.streak - 1).coerceAtLeast(0)
                habit.copy(isCompletedToday = newCompleted, streak = newStreak)
            } else {
                habit
            }
        }
    }

    fun deleteHabit(id: String) {
        _habits.value = _habits.value.filterNot { it.id == id }
    }

    // Note operations
    fun addNote(note: Note) {
        _notes.value = _notes.value + note
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filterNot { it.id == id }
    }

    companion object {
        @Volatile
        private var instance: StudentsDailyRepository? = null

        fun getInstance(): StudentsDailyRepository {
            return instance ?: synchronized(this) {
                instance ?: StudentsDailyRepository().also { instance = it }
            }
        }

        private fun initialExpenses(): List<Expense> = listOf(
            Expense(title = "Campus Cafeteria Lunch", amount = 12.50, category = ExpenseCategory.FOOD, date = "2025-03-01", note = "Burger and juice"),
            Expense(title = "Monthly Bus Pass", amount = 25.00, category = ExpenseCategory.TRANSPORT, date = "2025-03-01", note = "Student discount applied"),
            Expense(title = "Data Structures Textbook", amount = 65.00, category = ExpenseCategory.ACADEMIC, date = "2025-02-28", note = "Required for CS101"),
            Expense(title = "Weekend Movie Ticket", amount = 14.00, category = ExpenseCategory.ENTERTAINMENT, date = "2025-02-27", note = "IMAX screen"),
            Expense(title = "Campus Gym Pass", amount = 20.00, category = ExpenseCategory.HEALTH, date = "2025-02-25", note = "Monthly membership"),
            Expense(title = "Dorm Printing & Wi-Fi", amount = 15.00, category = ExpenseCategory.UTILITIES, date = "2025-02-20", note = "Printing credits")
        )

        private fun initialTimetableItems(): List<TimetableItem> = listOf(
            TimetableItem(subject = "CS101: Intro to Computer Science", instructor = "Dr. Alan Turing", room = "Hall 301", dayOfWeek = "Monday", startTime = "09:00", endTime = "10:30", type = TimetableType.LECTURE),
            TimetableItem(subject = "CS101 Programming Lab", instructor = "TA Alex", room = "Lab B2", dayOfWeek = "Monday", startTime = "11:00", endTime = "12:30", type = TimetableType.LAB),
            TimetableItem(subject = "MATH201: Linear Algebra", instructor = "Dr. Ada Lovelace", room = "Hall 102", dayOfWeek = "Tuesday", startTime = "10:00", endTime = "11:30", type = TimetableType.LECTURE),
            TimetableItem(subject = "PHYS102: Physics Midterm Exam", instructor = "Prof. Feynman", room = "Auditorium A", dayOfWeek = "Wednesday", startTime = "14:00", endTime = "16:00", type = TimetableType.EXAM),
            TimetableItem(subject = "ENG105: Technical Communication", instructor = "Prof. Hemingway", room = "Room 204", dayOfWeek = "Thursday", startTime = "13:00", endTime = "14:30", type = TimetableType.LECTURE),
            TimetableItem(subject = "CS101 Assignment 2 Submission", instructor = "Dr. Alan Turing", room = "Online Portal", dayOfWeek = "Friday", startTime = "23:59", endTime = "23:59", type = TimetableType.DEADLINE)
        )

        private fun initialTasks(): List<TaskItem> = listOf(
            TaskItem(title = "Submit CS101 Recursion Assignment", subject = "CS101", dueDate = "2025-03-05", priority = Priority.HIGH, isCompleted = false),
            TaskItem(title = "Read Chapter 4 of Linear Algebra", subject = "MATH201", dueDate = "2025-03-06", priority = Priority.MEDIUM, isCompleted = true),
            TaskItem(title = "Prepare Physics Lab Report", subject = "PHYS102", dueDate = "2025-03-08", priority = Priority.HIGH, isCompleted = false),
            TaskItem(title = "Buy Notebooks and Study Supplies", subject = "General", dueDate = "2025-03-10", priority = Priority.LOW, isCompleted = false)
        )

        private fun initialHabits(): List<Habit> = listOf(
            Habit(title = "Review Daily Class Notes", targetDays = 30, streak = 5, isCompletedToday = true),
            Habit(title = "Morning Exercise / Jogging", targetDays = 21, streak = 3, isCompletedToday = false),
            Habit(title = "Read 15 Pages of Literature", targetDays = 14, streak = 7, isCompletedToday = true),
            Habit(title = "Drink 2 Liters of Water", targetDays = 30, streak = 12, isCompletedToday = false)
        )

        private fun initialNotes(): List<Note> = listOf(
            Note(title = "CS101 Binary Search Trees", content = "Binary Search Tree properties: Left child < Root < Right child. Search, Insertion, and Deletion average time complexity is O(log n).", dateString = "2025-03-01", categoryTag = "Lecture"),
            Note(title = "Group Project Ideas", content = "1. Student Daily Companion App\n2. Smart Schedule Optimizer\n3. Campus Budget Assistant", dateString = "2025-02-28", categoryTag = "Project"),
            Note(title = "Midterm Study Checklist", content = "- Solve Linear Algebra practice matrices\n- Review Physics kinematics equations\n- Re-watch CS101 recursion video lectures", dateString = "2025-02-27", categoryTag = "Study")
        )
    }
}
