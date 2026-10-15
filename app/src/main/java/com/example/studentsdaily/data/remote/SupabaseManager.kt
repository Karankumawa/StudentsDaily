package com.example.studentsdaily.data.remote

import com.example.studentsdaily.data.model.Expense
import com.example.studentsdaily.data.model.ExpenseCategory
import com.example.studentsdaily.data.model.Habit
import com.example.studentsdaily.data.model.Note
import com.example.studentsdaily.data.model.Priority
import com.example.studentsdaily.data.model.TaskItem
import com.example.studentsdaily.data.model.TimetableItem
import com.example.studentsdaily.data.model.TimetableType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseUser(
    val id: String,
    val email: String,
    val fullName: String = ""
)

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String = "",
    val user: SupabaseUser
)

sealed interface SupabaseAuthResult {
    data class Success(val session: SupabaseSession?, val message: String = "") : SupabaseAuthResult
    data class Error(val message: String) : SupabaseAuthResult
}

class SupabaseManager private constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun signUp(email: String, password: String, fullName: String): SupabaseAuthResult =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/auth/v1/signup"
                val jsonBody = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password.trim())
                    put("data", JSONObject().apply {
                        put("full_name", fullName.trim())
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(responseBody)
                    val accessToken = jsonObj.optString("access_token", "")
                    val refreshToken = jsonObj.optString("refresh_token", "")
                    val userObj = jsonObj.optJSONObject("user") ?: jsonObj

                    if (userObj != null) {
                        val userId = userObj.optString("id", "")
                        val userEmail = userObj.optString("email", email)
                        val metadata = userObj.optJSONObject("user_metadata")
                        val metaName = metadata?.optString("full_name", fullName) ?: fullName

                        val confirmedAt = if (userObj.isNull("confirmed_at")) "" else userObj.optString("confirmed_at", "")
                        val emailConfirmedAt = if (userObj.isNull("email_confirmed_at")) "" else userObj.optString("email_confirmed_at", "")

                        val isConfirmationRequired = (confirmedAt.isBlank() && emailConfirmedAt.isBlank()) || accessToken.isBlank()

                        if (isConfirmationRequired) {
                            SupabaseAuthResult.Success(
                                session = null,
                                message = "Account created! A confirmation email has been sent to your address. Please confirm it before logging in."
                            )
                        } else {
                            val user = SupabaseUser(id = userId, email = userEmail, fullName = metaName)
                            val session = SupabaseSession(accessToken = accessToken, refreshToken = refreshToken, user = user)
                            SupabaseAuthResult.Success(
                                session = session,
                                message = "Account created successfully!"
                            )
                        }
                    } else {
                        SupabaseAuthResult.Error("Sign up succeeded but user profile was empty.")
                    }
                } else {
                    val errorMsg = parseErrorMessage(responseBody)
                    SupabaseAuthResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                SupabaseAuthResult.Error(e.localizedMessage ?: "Network error during sign up")
            }
        }

    suspend fun signIn(email: String, password: String): SupabaseAuthResult =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/auth/v1/token?grant_type=password"
                val jsonBody = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password.trim())
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(responseBody)
                    val accessToken = jsonObj.optString("access_token", "")
                    val refreshToken = jsonObj.optString("refresh_token", "")
                    val userObj = jsonObj.optJSONObject("user")

                    val userId = userObj?.optString("id", "") ?: ""
                    val userEmail = userObj?.optString("email", email) ?: email
                    val metadata = userObj?.optJSONObject("user_metadata")
                    val metaName = metadata?.optString("full_name", "") ?: ""

                    val user = SupabaseUser(id = userId, email = userEmail, fullName = metaName)
                    val session = SupabaseSession(accessToken = accessToken, refreshToken = refreshToken, user = user)

                    SupabaseAuthResult.Success(session = session, message = "Sign in successful!")
                } else {
                    val isUnconfirmed = responseBody.contains("email_not_confirmed", ignoreCase = true) ||
                            responseBody.contains("email not confirmed", ignoreCase = true)
                    val errorMsg = if (isUnconfirmed) {
                        "Email not confirmed. Please check your email inbox or disable 'Confirm email' in Supabase Auth Dashboard."
                    } else {
                        parseErrorMessage(responseBody)
                    }
                    SupabaseAuthResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                SupabaseAuthResult.Error(e.localizedMessage ?: "Network error during sign in")
            }
        }

    // =========================================================================
    // PROFILE OPERATIONS
    // =========================================================================
    suspend fun updateProfileName(userId: String, token: String, fullName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/profiles?id=eq.$userId"
                val json = JSONObject().apply {
                    put("full_name", fullName.trim())
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .patch(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    // =========================================================================
    // EXPENSES CRUD
    // =========================================================================
    suspend fun fetchExpenses(userId: String, token: String): List<Expense> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/expenses?user_id=eq.$userId&select=*"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val list = mutableListOf<Expense>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val title = obj.optString("title")
                    val amount = obj.optDouble("amount", 0.0)
                    val catStr = obj.optString("category", "FOOD")
                    val dateStr = obj.optString("date_string", "Today")
                    val note = obj.optString("note", "")

                    val category = try {
                        ExpenseCategory.valueOf(catStr.uppercase())
                    } catch (_: Exception) {
                        ExpenseCategory.FOOD
                    }

                    list.add(Expense(id = id, title = title, amount = amount, category = category, date = dateStr, note = note))
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

    suspend fun insertExpense(userId: String, token: String, expense: Expense): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/expenses"
                val json = JSONObject().apply {
                    put("id", expense.id)
                    put("user_id", userId)
                    put("title", expense.title)
                    put("amount", expense.amount)
                    put("category", expense.category.name)
                    put("date_string", expense.date)
                    put("note", expense.note)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .post(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun updateExpense(userId: String, token: String, expense: Expense): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/expenses?id=eq.${expense.id}&user_id=eq.$userId"
                val json = JSONObject().apply {
                    put("title", expense.title)
                    put("amount", expense.amount)
                    put("category", expense.category.name)
                    put("date_string", expense.date)
                    put("note", expense.note)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .patch(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun deleteExpense(userId: String, token: String, expenseId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/expenses?id=eq.$expenseId&user_id=eq.$userId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    // =========================================================================
    // TIMETABLE ITEMS CRUD
    // =========================================================================
    suspend fun fetchTimetableItems(userId: String, token: String): List<TimetableItem> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/timetable_items?user_id=eq.$userId&select=*"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val list = mutableListOf<TimetableItem>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val subject = obj.optString("subject")
                    val instructor = obj.optString("instructor", "")
                    val room = obj.optString("room", "")
                    val dayInt = obj.optInt("day_of_week", 1)
                    val startTime = obj.optString("start_time", "09:00")
                    val endTime = obj.optString("end_time", "10:00")
                    val typeStr = obj.optString("type", "LECTURE")

                    val type = try {
                        TimetableType.valueOf(typeStr.uppercase())
                    } catch (_: Exception) {
                        TimetableType.LECTURE
                    }

                    list.add(
                        TimetableItem(
                            id = id,
                            subject = subject,
                            instructor = instructor,
                            room = room,
                            dayOfWeek = dayOfWeekFromInt(dayInt),
                            startTime = startTime,
                            endTime = endTime,
                            type = type
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

    suspend fun insertTimetableItem(userId: String, token: String, item: TimetableItem): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/timetable_items"
                val json = JSONObject().apply {
                    put("id", item.id)
                    put("user_id", userId)
                    put("subject", item.subject)
                    put("instructor", item.instructor)
                    put("room", item.room)
                    put("day_of_week", dayOfWeekToInt(item.dayOfWeek))
                    put("start_time", item.startTime)
                    put("end_time", item.endTime)
                    put("type", item.type.name)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .post(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun deleteTimetableItem(userId: String, token: String, itemId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/timetable_items?id=eq.$itemId&user_id=eq.$userId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    // =========================================================================
    // TASKS CRUD
    // =========================================================================
    suspend fun fetchTasks(userId: String, token: String): List<TaskItem> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/tasks?user_id=eq.$userId&select=*"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val list = mutableListOf<TaskItem>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val title = obj.optString("title")
                    val subject = obj.optString("subject", "")
                    val dueDate = obj.optString("due_date", "Today")
                    val prioStr = obj.optString("priority", "MEDIUM")
                    val isCompleted = obj.optBoolean("is_completed", false)

                    val priority = try {
                        Priority.valueOf(prioStr.uppercase())
                    } catch (_: Exception) {
                        Priority.MEDIUM
                    }

                    list.add(
                        TaskItem(
                            id = id,
                            title = title,
                            subject = subject,
                            dueDate = dueDate,
                            priority = priority,
                            isCompleted = isCompleted
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

    suspend fun insertTask(userId: String, token: String, task: TaskItem): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/tasks"
                val json = JSONObject().apply {
                    put("id", task.id)
                    put("user_id", userId)
                    put("title", task.title)
                    put("subject", task.subject)
                    put("due_date", task.dueDate)
                    put("priority", task.priority.name)
                    put("is_completed", task.isCompleted)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .post(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun updateTaskCompletion(userId: String, token: String, taskId: String, isCompleted: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/tasks?id=eq.$taskId&user_id=eq.$userId"
                val json = JSONObject().apply {
                    put("is_completed", isCompleted)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .patch(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun deleteTask(userId: String, token: String, taskId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/tasks?id=eq.$taskId&user_id=eq.$userId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    // =========================================================================
    // HABITS CRUD
    // =========================================================================
    suspend fun fetchHabits(userId: String, token: String): List<Habit> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/habits?user_id=eq.$userId&select=*"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val list = mutableListOf<Habit>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val title = obj.optString("title")
                    val targetDays = obj.optInt("target_days", 30)
                    val streak = obj.optInt("streak", 0)
                    val isCompletedToday = obj.optBoolean("is_completed_today", false)

                    list.add(
                        Habit(
                            id = id,
                            title = title,
                            targetDays = targetDays,
                            streak = streak,
                            isCompletedToday = isCompletedToday
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

    suspend fun insertHabit(userId: String, token: String, habit: Habit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/habits"
                val json = JSONObject().apply {
                    put("id", habit.id)
                    put("user_id", userId)
                    put("title", habit.title)
                    put("target_days", habit.targetDays)
                    put("streak", habit.streak)
                    put("is_completed_today", habit.isCompletedToday)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .post(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun updateHabitCompletion(
        userId: String,
        token: String,
        habitId: String,
        isCompletedToday: Boolean,
        streak: Int
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/habits?id=eq.$habitId&user_id=eq.$userId"
                val json = JSONObject().apply {
                    put("is_completed_today", isCompletedToday)
                    put("streak", streak)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .patch(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun deleteHabit(userId: String, token: String, habitId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/habits?id=eq.$habitId&user_id=eq.$userId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    // =========================================================================
    // NOTES CRUD
    // =========================================================================
    suspend fun fetchNotes(userId: String, token: String): List<Note> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/notes?user_id=eq.$userId&select=*"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val list = mutableListOf<Note>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val title = obj.optString("title")
                    val content = obj.optString("content", "")
                    val dateStr = obj.optString("date_string", "Today")
                    val tag = obj.optString("category_tag", "General")
                    val isPinned = obj.optBoolean("is_pinned", false) || obj.optBoolean("isPinned", false)

                    list.add(
                        Note(
                            id = id,
                            title = title,
                            content = content,
                            dateString = dateStr,
                            categoryTag = tag,
                            isPinned = isPinned
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

    suspend fun insertNote(userId: String, token: String, note: Note): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/notes"
                val json = JSONObject().apply {
                    put("id", note.id)
                    put("user_id", userId)
                    put("title", note.title)
                    put("content", note.content)
                    put("date_string", note.dateString)
                    put("category_tag", note.categoryTag)
                    put("is_pinned", note.isPinned)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .post(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun updateNote(userId: String, token: String, note: Note): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/notes?id=eq.${note.id}&user_id=eq.$userId"
                val json = JSONObject().apply {
                    put("title", note.title)
                    put("content", note.content)
                    put("date_string", note.dateString)
                    put("category_tag", note.categoryTag)
                    put("is_pinned", note.isPinned)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Prefer", "return=representation")
                    .patch(json.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    suspend fun deleteNote(userId: String, token: String, noteId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/notes?id=eq.$noteId&user_id=eq.$userId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }

    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val rawMsg = when {
                json.has("error_description") -> json.getString("error_description")
                json.has("msg") -> json.getString("msg")
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> "Supabase request failed."
            }
            if (rawMsg.contains("email not confirmed", ignoreCase = true) ||
                rawMsg.contains("email_not_confirmed", ignoreCase = true) ||
                responseBody.contains("email_not_confirmed", ignoreCase = true)
            ) {
                "Email not confirmed. Please check your email inbox or disable 'Confirm email' in Supabase Auth Dashboard."
            } else {
                rawMsg
            }
        } catch (_: Exception) {
            if (responseBody.contains("email_not_confirmed", ignoreCase = true) ||
                responseBody.contains("email not confirmed", ignoreCase = true)
            ) {
                "Email not confirmed. Please check your email inbox or disable 'Confirm email' in Supabase Auth Dashboard."
            } else if (responseBody.isNotBlank()) {
                responseBody
            } else {
                "Unknown Supabase error."
            }
        }
    }

    private fun dayOfWeekToInt(dayStr: String): Int {
        return when (dayStr.lowercase().trim()) {
            "monday", "mon", "1" -> 1
            "tuesday", "tue", "2" -> 2
            "wednesday", "wed", "3" -> 3
            "thursday", "thu", "4" -> 4
            "friday", "fri", "5" -> 5
            "saturday", "sat", "6" -> 6
            "sunday", "sun", "7" -> 7
            else -> dayStr.toIntOrNull() ?: 1
        }
    }

    private fun dayOfWeekFromInt(dayInt: Int): String {
        return when (dayInt) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Monday"
        }
    }

    companion object {
        @Volatile
        private var instance: SupabaseManager? = null

        fun getInstance(): SupabaseManager {
            return instance ?: synchronized(this) {
                instance ?: SupabaseManager().also { instance = it }
            }
        }
    }
}
