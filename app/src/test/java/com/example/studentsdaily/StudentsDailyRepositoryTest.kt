package com.example.studentsdaily

import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun testInitialDataLoaded() {
        assertTrue(repository.expenses.value.isNotEmpty())
        assertTrue(repository.timetableItems.value.isNotEmpty())
        assertTrue(repository.tasks.value.isNotEmpty())
        assertTrue(repository.habits.value.isNotEmpty())
        assertTrue(repository.notes.value.isNotEmpty())
    }

    @Test
    fun testExpenseAddAndDelete() {
        val initialCount = repository.expenses.value.size
        val newExpense = Expense(
            title = "Test Coffee",
            amount = 4.50,
            category = ExpenseCategory.FOOD,
            date = "2025-03-02"
        )
        repository.addExpense(newExpense)
        assertEquals(initialCount + 1, repository.expenses.value.size)

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
    fun testNoteAddAndDelete() {
        val newNote = Note(
            title = "Test Note Title",
            content = "Test Note Content",
            dateString = "2025-03-02",
            categoryTag = "Test"
        )
        repository.addNote(newNote)
        assertTrue(repository.notes.value.any { it.id == newNote.id })

        repository.deleteNote(newNote.id)
        assertFalse(repository.notes.value.any { it.id == newNote.id })
    }
}
