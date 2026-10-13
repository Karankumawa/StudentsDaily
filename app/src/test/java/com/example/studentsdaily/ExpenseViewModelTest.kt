package com.example.studentsdaily

import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.ui.screens.formatExpenseDisplayDate
import com.example.studentsdaily.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ExpenseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ExpenseViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDateParsingAndFormatting() {
        val todayStr = formatExpenseDisplayDate("Today")
        assertEquals("Today", todayStr)

        val yesterdayStr = formatExpenseDisplayDate("Yesterday")
        assertEquals("Yesterday", yesterdayStr)

        val customDateStr = formatExpenseDisplayDate("18 May 2025")
        assertTrue("Formatted custom date should contain 2025", customDateStr.contains("2025"))
    }

    @Test
    fun testExpenseSortingOrder() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {}
        }

        val expenses = viewModel.uiState.value.expenses
        assertTrue("Initial expenses should not be empty", expenses.isNotEmpty())

        for (i in 0 until expenses.size - 1) {
            val currentTs = ExpenseViewModel.parseExpenseTimestamp(expenses[i].date, expenses[i].timestamp)
            val nextTs = ExpenseViewModel.parseExpenseTimestamp(expenses[i + 1].date, expenses[i + 1].timestamp)
            assertTrue("Current expense ($currentTs) should be >= next expense ($nextTs)", currentTs >= nextTs)
        }

        collectJob.cancel()
    }

    @Test
    fun testSearchAndCategoryFiltering() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {}
        }

        viewModel.setSearchQuery("Cafeteria")
        val searchResults = viewModel.uiState.value.filteredExpenses
        assertTrue("Search for 'Cafeteria' should return matching expense", searchResults.any { it.title.contains("Cafeteria") })

        viewModel.clearFilters()
        assertEquals(viewModel.uiState.value.expenses.size, viewModel.uiState.value.filteredExpenses.size)

        viewModel.setCategoryFilter(ExpenseCategory.FOOD)
        val foodExpenses = viewModel.uiState.value.filteredExpenses
        assertTrue("Filtered expenses should all be FOOD category", foodExpenses.all { it.category == ExpenseCategory.FOOD })

        collectJob.cancel()
    }

    @Test
    fun testAddAndUpdateExpense() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {}
        }

        val initialCount = viewModel.uiState.value.expenses.size
        viewModel.addExpense("Snacks", 50.0, ExpenseCategory.FOOD, "Today", "Evening samosa")

        val updatedState = viewModel.uiState.value
        assertEquals(initialCount + 1, updatedState.expenses.size)

        val added = updatedState.expenses.find { it.title == "Snacks" }
        assertTrue(added != null)
        assertEquals(50.0, added!!.amount, 0.01)

        // Clean up
        viewModel.deleteExpense(added.id)
        assertEquals(initialCount, viewModel.uiState.value.expenses.size)

        collectJob.cancel()
    }
}
