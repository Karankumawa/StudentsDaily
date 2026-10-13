package com.example.studentsdaily.ui.viewmodel

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
    val filteredExpenses: List<Expense> = emptyList(),
    val monthlyBudget: Double = 300.00
)

class ExpenseViewModel(
    private val repository: StudentsDailyRepository = StudentsDailyRepository.getInstance()
) : ViewModel() {

    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _selectedCategoryFilter

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.expenses,
        repository.monthlyBudget,
        _selectedCategoryFilter
    ) { expenses, budget, categoryFilter ->
        val total = expenses.sumOf { it.amount }
        val categoryMap = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val filtered = if (categoryFilter == null) {
            expenses
        } else {
            expenses.filter { it.category == categoryFilter }
        }
        ExpenseUiState(
            expenses = expenses,
            totalExpenseAmount = total,
            expensesByCategory = categoryMap,
            selectedCategoryFilter = categoryFilter,
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

    fun updateMonthlyBudget(newBudget: Double) {
        if (newBudget > 0) {
            repository.updateMonthlyBudget(newBudget)
        }
    }

    fun addExpense(title: String, amount: Double, category: ExpenseCategory, date: String, note: String = "") {
        if (title.isBlank() || amount <= 0) return
        val expense = Expense(
            title = title.trim(),
            amount = amount,
            category = category,
            date = date.ifBlank { "Today" },
            note = note.trim()
        )
        repository.addExpense(expense)
    }

    fun updateExpense(id: String, title: String, amount: Double, category: ExpenseCategory, date: String, note: String = "") {
        if (title.isBlank() || amount <= 0) return
        val updated = Expense(
            id = id,
            title = title.trim(),
            amount = amount,
            category = category,
            date = date.ifBlank { "Today" },
            note = note.trim()
        )
        repository.updateExpense(updated)
    }

    fun deleteExpense(id: String) {
        repository.deleteExpense(id)
    }
}
