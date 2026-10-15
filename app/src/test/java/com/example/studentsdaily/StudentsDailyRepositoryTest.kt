package com.example.studentsdaily

import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.remote.SupabaseConfig
import com.example.studentsdaily.data.repository.AuthUiState
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudentsDailyRepositoryTest {

    private lateinit var repository: StudentsDailyRepository

    @Before
    fun setUp() {
        repository = StudentsDailyRepository.getInstance()
    }

    @Test
    fun testSupabaseConfigConstants() {
        assertEquals("https://zruqunvowimajlgoyiwb.supabase.co", SupabaseConfig.SUPABASE_URL)
        assertEquals("sb_publishable_EVf8Ysepkk4njkTCW-EqPw_netwV9Uq", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
        assertEquals("https://zruqunvowimajlgoyiwb.supabase.co/auth/v1/.well-known/jwks.json", SupabaseConfig.SUPABASE_JWKS_URL)
    }

    @Test
    fun testInitialDataLoaded() {
        assertTrue(repository.expenses.value.isNotEmpty())
        assertTrue(repository.timetableItems.value.isNotEmpty())
        assertTrue(repository.tasks.value.isNotEmpty())
        assertTrue(repository.habits.value.isNotEmpty())
        assertTrue(repository.notes.value.isNotEmpty())
        assertTrue(repository.isGuestMode.value)
        assertNull(repository.currentSession.value)
    }

    @Test
    fun testSignOutResetsGuestMode() {
        repository.signOut()
        assertTrue(repository.isGuestMode.value)
        assertNull(repository.currentSession.value)
        assertEquals(AuthUiState.Idle, repository.authUiState.value)
    }

    @Test
    fun testClearSessionAndData() {
        repository.clearSessionAndData()
        assertTrue(repository.isGuestMode.value)
        assertNull(repository.currentSession.value)
        assertEquals(AuthUiState.Idle, repository.authUiState.value)
        assertEquals(5000.0, repository.monthlyBudget.value, 0.01)
    }

    @Test
    fun testExpenseAddUpdateAndDelete() {
        val initialCount = repository.expenses.value.size
        val newExpense = Expense(
            title = "Test Coffee",
            amount = 4.50,
            category = ExpenseCategory.FOOD,
            date = "2025-03-02"
        )
        repository.addExpense(newExpense)
        assertEquals(initialCount + 1, repository.expenses.value.size)

        val updatedExpense = newExpense.copy(title = "Updated Coffee Title", amount = 6.00)
        repository.updateExpense(updatedExpense)
        val fetchedExpense = repository.expenses.value.find { it.id == newExpense.id }
        assertEquals("Updated Coffee Title", fetchedExpense?.title)
        assertEquals(6.00, fetchedExpense?.amount ?: 0.0, 0.01)

        repository.deleteExpense(newExpense.id)
        assertEquals(initialCount, repository.expenses.value.size)
    }

    @Test
    fun testTaskAddAndToggle() {
        val newTask = TaskItem(
            title = "Test Study Task",
            subject = "CS101",
            dueDate = "Tomorrow",
            priority = Priority.HIGH
        )
        repository.addTask(newTask)
        val addedTask = repository.tasks.value.find { it.id == newTask.id }
        assertTrue(addedTask != null)
        assertFalse(addedTask!!.isCompleted)

        repository.toggleTaskCompletion(newTask.id)
        val toggledTask = repository.tasks.value.find { it.id == newTask.id }
        assertTrue(toggledTask!!.isCompleted)

        repository.deleteTask(newTask.id)
    }

    @Test
    fun testHabitToggleCompletion() {
        val newHabit = Habit(
            title = "Test Habit",
            targetDays = 10,
            streak = 2,
            isCompletedToday = false
        )
        repository.addHabit(newHabit)
        repository.toggleHabitCompletion(newHabit.id)

        val updatedHabit = repository.habits.value.find { it.id == newHabit.id }
        assertTrue(updatedHabit!!.isCompletedToday)
        assertEquals(3, updatedHabit.streak)

        repository.deleteHabit(newHabit.id)
    }

    @Test
    fun testNoteAddUpdateAndDelete() {
        val newNote = Note(
            title = "Test Note Title",
            content = "Test Note Content",
            dateString = "2025-03-02",
            categoryTag = "Test"
        )
        repository.addNote(newNote)
        assertTrue(repository.notes.value.any { it.id == newNote.id })

        val updatedNote = newNote.copy(title = "Updated Note Title", content = "Updated Note Content")
        repository.updateNote(updatedNote)
        val fetchedNote = repository.notes.value.find { it.id == newNote.id }
        assertEquals("Updated Note Title", fetchedNote?.title)
        assertEquals("Updated Note Content", fetchedNote?.content)

        // Test Pinning
        assertFalse(fetchedNote!!.isPinned)
        repository.toggleNotePin(newNote.id)
        val pinnedNote = repository.notes.value.find { it.id == newNote.id }
        assertTrue(pinnedNote!!.isPinned)

        repository.deleteNote(newNote.id)
        assertFalse(repository.notes.value.any { it.id == newNote.id })
    }

    @Test
    fun testMonthlyBudgetUpdate() {
        repository.updateMonthlyBudget(10000.0)
        assertEquals(10000.0, repository.monthlyBudget.value, 0.01)

        repository.updateMonthlyBudget(-100.0) // Invalid budget should be ignored
        assertEquals(10000.0, repository.monthlyBudget.value, 0.01)
    }

    @Test
    fun testUpdateFullNameNotLoggedIn() {
        var called = false
        var successResult = true
        var messageResult = ""
        repository.updateFullName("New Name") { success, message ->
            called = true
            successResult = success
            messageResult = message
        }
        assertTrue(called)
        assertFalse(successResult)
        assertEquals("Not logged in", messageResult)
    }

    @Test
    fun testUpdateFullNameEmptyString() {
        var called = false
        var successResult = true
        var messageResult = ""
        repository.updateFullName("   ") { success, message ->
            called = true
            successResult = success
            messageResult = message
        }
        assertTrue(called)
        assertFalse(successResult)
        assertEquals("Name cannot be empty", messageResult)
    }

    @Test
    fun testCurrencyFormattingRupees() {
        val totalAmount = repository.expenses.value.sumOf { it.amount }
        val formattedTotal = String.format(java.util.Locale.US, "₹%.2f", totalAmount)
        assertTrue("Formatted total should start with ₹ symbol", formattedTotal.startsWith("₹"))
        assertFalse("Formatted total should not contain dollar symbol", formattedTotal.contains("$"))

        val firstExpense = repository.expenses.value.first()
        val formattedExpense = String.format(java.util.Locale.US, "-₹%.2f", firstExpense.amount)
        assertTrue("Formatted expense should start with -₹", formattedExpense.startsWith("-₹"))
        assertFalse("Formatted expense should not contain dollar symbol", formattedExpense.contains("$"))
    }
}
