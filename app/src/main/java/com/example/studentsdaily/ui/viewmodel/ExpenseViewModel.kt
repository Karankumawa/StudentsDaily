package com.example.studentsdaily.ui.viewmodel

import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.repository.StudentsDailyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val totalExpenseAmount: Double = 0.0,
    val expensesByCategory: Map<ExpenseCategory, Double> = emptyMap(),
    val selectedCategoryFilter: ExpenseCategory? = null,
    val searchQuery: String = "",
    val filteredExpenses: List<Expense> = emptyList(),
    val monthlyBudget: Double = 3000.00
)

class ExpenseViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _selectedCategoryFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.expenses,
        repository.monthlyBudget,
        _selectedCategoryFilter,
        _searchQuery
    ) { expenses, budget, categoryFilter, search ->
        // Sort all expenses in descending order by parsed timestamp / fallback timestamp
        val sortedExpenses = expenses.sortedWith(
            compareByDescending<Expense> { parseExpenseTimestamp(it.date, it.timestamp) }
                .thenByDescending { it.timestamp }
        )

        val total = sortedExpenses.sumOf { it.amount }
        val categoryMap = sortedExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val query = search.trim().lowercase()
        val filtered = sortedExpenses.filter { expense ->
            val matchesCategory = categoryFilter == null || expense.category == categoryFilter
            val matchesSearch = query.isEmpty() ||
                    expense.title.lowercase().contains(query) ||
                    expense.category.categoryName.lowercase().contains(query) ||
                    expense.note.lowercase().contains(query) ||
                    expense.date.lowercase().contains(query)
            matchesCategory && matchesSearch
        }

        ExpenseUiState(
            expenses = sortedExpenses,
            totalExpenseAmount = total,
            expensesByCategory = categoryMap,
            selectedCategoryFilter = categoryFilter,
            searchQuery = search,
            filteredExpenses = filtered,
            monthlyBudget = budget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFilters() {
        _selectedCategoryFilter.value = null
        _searchQuery.value = ""
    }

    fun updateMonthlyBudget(newBudget: Double) {
        if (newBudget > 0) {
            repository.updateMonthlyBudget(newBudget)
        }
    }

    fun addExpense(title: String, amount: Double, category: ExpenseCategory, date: String, note: String = "") {
        if (title.isBlank() || amount <= 0) return
        val formattedDate = date.trim().ifBlank { "Today" }
        val expense = Expense(
            title = title.trim(),
            amount = amount,
            category = category,
            date = formattedDate,
            note = note.trim(),
            timestamp = System.currentTimeMillis()
        )
        repository.addExpense(expense)
    }

    fun updateExpense(id: String, title: String, amount: Double, category: ExpenseCategory, date: String, note: String = "") {
        if (title.isBlank() || amount <= 0) return
        val existing = repository.expenses.value.find { it.id == id }
        val formattedDate = date.trim().ifBlank { "Today" }
        val updated = Expense(
            id = id,
            title = title.trim(),
            amount = amount,
            category = category,
            date = formattedDate,
            note = note.trim(),
            timestamp = existing?.timestamp ?: System.currentTimeMillis()
        )
        repository.updateExpense(updated)
    }

    fun deleteExpense(id: String) {
        repository.deleteExpense(id)
    }

    companion object {
        fun parseExpenseTimestamp(dateStr: String, fallbackTimestamp: Long = 0L): Long {
            val trimmed = dateStr.trim()
            if (trimmed.equals("Today", ignoreCase = true)) {
                return if (fallbackTimestamp > 0) fallbackTimestamp else System.currentTimeMillis()
            }
            if (trimmed.equals("Yesterday", ignoreCase = true)) {
                val yesterdayMillis = System.currentTimeMillis() - 86400000L
                return if (fallbackTimestamp > 0) fallbackTimestamp else yesterdayMillis
            }

            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd", Locale.US),
                SimpleDateFormat("dd MMM yyyy", Locale.US),
                SimpleDateFormat("dd MMMM yyyy", Locale.US),
                SimpleDateFormat("dd/MM/yyyy", Locale.US),
                SimpleDateFormat("MM/dd/yyyy", Locale.US),
                SimpleDateFormat("MMM dd, yyyy", Locale.US)
            )

            for (format in formats) {
                try {
                    val parsedDate = format.parse(trimmed)
                    if (parsedDate != null) {
                        return parsedDate.time
                    }
                } catch (_: Exception) {
                    // Ignore format mismatch
                }
            }

            return if (fallbackTimestamp > 0) fallbackTimestamp else System.currentTimeMillis()
        }
    }
}
