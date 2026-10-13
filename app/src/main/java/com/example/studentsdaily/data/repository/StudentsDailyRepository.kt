package com.example.studentsdaily.data.repository

import android.content.Context
import com.example.studentsdaily.data.local.AuthPreferences
import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.model.TimetableItem
import com.example.studentsdaily.data.model.TimetableType
import com.example.studentsdaily.data.remote.SupabaseAuthResult
import com.example.studentsdaily.data.remote.SupabaseManager
import com.example.studentsdaily.data.remote.SupabaseSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class StudentsDailyRepository private constructor() {

    private val supabaseManager = SupabaseManager.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var appContext: Context? = null

    private val _currentSession = MutableStateFlow<SupabaseSession?>(null)
    val currentSession: StateFlow<SupabaseSession?> = _currentSession.asStateFlow()

    private val _isGuestMode = MutableStateFlow(true)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

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

    fun initializeWithContext(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val savedSession = AuthPreferences.loadSession(context)
        if (savedSession != null) {
            _currentSession.value = savedSession
            _isGuestMode.value = false
            refreshUserDataFromSupabase()
        }
    }

    // =========================================================================
    // AUTHENTICATION OPERATIONS
    // =========================================================================

    fun signUp(email: String, password: String, fullName: String) {
        _authUiState.value = AuthUiState.Loading
        repositoryScope.launch {
            when (val result = supabaseManager.signUp(email, password, fullName)) {
                is SupabaseAuthResult.Success -> {
                    val session = result.session
                    _currentSession.value = session
                    _isGuestMode.value = false
                    appContext?.let { AuthPreferences.saveSession(it, session) }
                    _authUiState.value = AuthUiState.Success("Account created successfully!")
                    refreshUserDataFromSupabase()
                }
                is SupabaseAuthResult.Error -> {
                    _authUiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        _authUiState.value = AuthUiState.Loading
        repositoryScope.launch {
            when (val result = supabaseManager.signIn(email, password)) {
                is SupabaseAuthResult.Success -> {
                    val session = result.session
                    _currentSession.value = session
                    _isGuestMode.value = false
                    appContext?.let { AuthPreferences.saveSession(it, session) }
                    _authUiState.value = AuthUiState.Success("Welcome back, ${session.user.fullName.ifBlank { session.user.email }}!")
                    refreshUserDataFromSupabase()
                }
                is SupabaseAuthResult.Error -> {
                    _authUiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun signOut() {
        appContext?.let { AuthPreferences.clearSession(it) }
        _currentSession.value = null
        _isGuestMode.value = true
        _expenses.value = initialExpenses()
        _timetableItems.value = initialTimetableItems()
        _tasks.value = initialTasks()
        _habits.value = initialHabits()
        _notes.value = initialNotes()
        _authUiState.value = AuthUiState.Idle
    }

    fun clearAuthError() {
        if (_authUiState.value is AuthUiState.Error || _authUiState.value is AuthUiState.Success) {
            _authUiState.value = AuthUiState.Idle
        }
    }

    private fun refreshUserDataFromSupabase() {
        val session = _currentSession.value ?: return
        repositoryScope.launch {
            val remoteExpenses = supabaseManager.fetchExpenses(session.user.id, session.accessToken)
            val remoteTimetable = supabaseManager.fetchTimetableItems(session.user.id, session.accessToken)
            val remoteTasks = supabaseManager.fetchTasks(session.user.id, session.accessToken)
            val remoteHabits = supabaseManager.fetchHabits(session.user.id, session.accessToken)
            val remoteNotes = supabaseManager.fetchNotes(session.user.id, session.accessToken)

            if (remoteExpenses.isEmpty() && remoteTimetable.isEmpty() && remoteTasks.isEmpty() && remoteHabits.isEmpty() && remoteNotes.isEmpty()) {
                seedInitialUserDataToSupabase(session.user.id, session.accessToken)
            } else {
                _expenses.value = remoteExpenses
                _timetableItems.value = remoteTimetable
                _tasks.value = remoteTasks
                _habits.value = remoteHabits
                _notes.value = remoteNotes
            }
        }
    }

    private suspend fun seedInitialUserDataToSupabase(userId: String, token: String) {
        val exp = initialExpenses()
        exp.forEach { supabaseManager.insertExpense(userId, token, it) }
        _expenses.value = exp

        val tt = initialTimetableItems()
        tt.forEach { supabaseManager.insertTimetableItem(userId, token, it) }
        _timetableItems.value = tt

        val tk = initialTasks()
        tk.forEach { supabaseManager.insertTask(userId, token, it) }
        _tasks.value = tk

        val hb = initialHabits()
        hb.forEach { supabaseManager.insertHabit(userId, token, it) }
        _habits.value = hb

        val nt = initialNotes()
        nt.forEach { supabaseManager.insertNote(userId, token, it) }
        _notes.value = nt
    }

    // =========================================================================
    // EXPENSE OPERATIONS
    // =========================================================================
    fun addExpense(expense: Expense) {
        _expenses.value = _expenses.value + expense
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.insertExpense(session.user.id, session.accessToken, expense)
            }
        }
    }

    fun deleteExpense(id: String) {
        _expenses.value = _expenses.value.filterNot { it.id == id }
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.deleteExpense(session.user.id, session.accessToken, id)
            }
        }
    }

    // =========================================================================
    // TASK OPERATIONS
    // =========================================================================
    fun addTask(task: TaskItem) {
        _tasks.value = _tasks.value + task
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.insertTask(session.user.id, session.accessToken, task)
            }
        }
    }

    fun toggleTaskCompletion(id: String) {
        var updatedTask: TaskItem? = null
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) {
                val newCompleted = !task.isCompleted
                task.copy(isCompleted = newCompleted).also { updatedTask = it }
            } else {
                task
            }
        }
        val session = _currentSession.value
        val taskToUpdate = updatedTask
        if (session != null && taskToUpdate != null) {
            repositoryScope.launch {
                supabaseManager.updateTaskCompletion(session.user.id, session.accessToken, id, taskToUpdate.isCompleted)
            }
        }
    }

    fun deleteTask(id: String) {
        _tasks.value = _tasks.value.filterNot { it.id == id }
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.deleteTask(session.user.id, session.accessToken, id)
            }
        }
    }

    // =========================================================================
    // TIMETABLE OPERATIONS
    // =========================================================================
    fun addTimetableItem(item: TimetableItem) {
        _timetableItems.value = _timetableItems.value + item
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.insertTimetableItem(session.user.id, session.accessToken, item)
            }
        }
    }

    fun deleteTimetableItem(id: String) {
        _timetableItems.value = _timetableItems.value.filterNot { it.id == id }
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.deleteTimetableItem(session.user.id, session.accessToken, id)
            }
        }
    }

    // =========================================================================
    // HABIT OPERATIONS
    // =========================================================================
    fun addHabit(habit: Habit) {
        _habits.value = _habits.value + habit
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.insertHabit(session.user.id, session.accessToken, habit)
            }
        }
    }

    fun toggleHabitCompletion(id: String) {
        var updatedHabit: Habit? = null
        _habits.value = _habits.value.map { habit ->
            if (habit.id == id) {
                val newCompleted = !habit.isCompletedToday
                val newStreak = if (newCompleted) habit.streak + 1 else (habit.streak - 1).coerceAtLeast(0)
                habit.copy(isCompletedToday = newCompleted, streak = newStreak).also { updatedHabit = it }
            } else {
                habit
            }
        }
        val session = _currentSession.value
        val habitToUpdate = updatedHabit
        if (session != null && habitToUpdate != null) {
            repositoryScope.launch {
                supabaseManager.updateHabitCompletion(session.user.id, session.accessToken, id, habitToUpdate.isCompletedToday, habitToUpdate.streak)
            }
        }
    }

    fun deleteHabit(id: String) {
        _habits.value = _habits.value.filterNot { it.id == id }
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.deleteHabit(session.user.id, session.accessToken, id)
            }
        }
    }

    // =========================================================================
    // NOTE OPERATIONS
    // =========================================================================
    fun addNote(note: Note) {
        _notes.value = _notes.value + note
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.insertNote(session.user.id, session.accessToken, note)
            }
        }
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filterNot { it.id == id }
        val session = _currentSession.value
        if (session != null) {
            repositoryScope.launch {
                supabaseManager.deleteNote(session.user.id, session.accessToken, id)
            }
        }
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
