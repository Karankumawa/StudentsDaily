# Project Plan

StudentsDaily - An all-in-one student daily utility companion featuring a Personal Expense Tracker and daily student tools.

Key Features & Specifications:
1. Personal Expense Tracker:
   - Light-green "financial" color motif (Material 3 styling with custom category colors like Red for Food, Blue for Transport, Purple for Academic/Books, Orange for Entertainment, Green for Utilities, etc.).
   - "Total Spent" dashboard card at the top with expense breakdowns (daily, weekly, monthly budget progress).
   - Scrollable list of recent transactions with category icons, category-colored tags, date/time, and expense amounts.
   - Floating Action Button (FAB) triggering a BottomSheet/Dialog to quickly add new expenses with category, amount, title, date, and optional note.
   
2. Student Daily & Weekly Work Suite:
   - Timetable & Class Schedule: Weekly view of upcoming lectures, labs, assignment deadlines, and room details.
   - Task & Assignment Tracker: Daily to-do list with priorities, due dates, and completion status.
   - Quick Notes & Habits Tracker: Daily habit check-ins and quick notes for lecture points.
   - Comprehensive Dashboard navigation (Bottom Navigation / Navigation Rail) integrating Expense Tracker, Timetable, Tasks, and Habit Hub seamlessly.

## Project Brief

# Project Brief: StudentsDaily

StudentsDaily is an all-in-one student daily utility companion designed to assist students with personal expense tracking and daily academic task management through an intuitive, light-green financial motif UI.

---

## Features

1. **Expense Tracker & Financial Dashboard**: Displays a "Total Spent" card with budget progress breakdowns, a scrollable transaction list with custom category-colored tags (Food, Transport, Academic, etc.), and a Floating Action Button (FAB) bottom sheet for quick expense entry.
2. **Timetable & Class Schedule**: A weekly overview displaying upcoming lectures, laboratory sessions, assignment deadlines, and room details.
3. **Task & Assignment Tracker**: Daily to-do management featuring task priorities, due dates, and completion status toggles.
4. **Habit Hub & Quick Notes**: Daily habit check-ins and quick note-taking capabilities for rapid lecture point capture.
5. **Unified Adaptive Navigation**: Seamless navigation across Expense Tracker, Timetable, Tasks, and Habit Hub using screen-adapted navigation components (Bottom Bar / Navigation Rail).

---

## High-Level Tech Stack

* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose with Material Design 3 (custom light-green financial color motif)
* **Navigation Strategy**: Jetpack Navigation 3 (State-Driven Navigation)
* **Adaptive Strategy**: Compose Material Adaptive Library (supporting phone and foldable/tablet viewports with Navigation Rail / Bottom Navigation adaptations)
* **Architecture & State Management**: Unidirectional Data Flow (UDF) with Jetpack `ViewModel`
* **Concurrency & Async**: Kotlin Coroutines & `StateFlow`

## Implementation Steps
**Total Duration:** 45h 8m 10s

### Task_1_DataAndDomainSetup: Define domain data models (Expense, Category, TimetableItem, TaskItem, Habit, Note), repository/data sources, and ViewModels managing state for all features.
- **Status:** COMPLETED
- **Updates:** Defined domain models (ExpenseCategory, Expense, TimetableItem, TaskItem, Habit, Note), state repository with Student daily sample data, and ViewModels (ExpenseViewModel, TimetableViewModel, TaskViewModel, HabitNoteViewModel, StudentsDailyViewModel). Build and unit tests passed cleanly.
- **Acceptance Criteria:**
  - Data models for Expense, Timetable, Task, Habit, and Note created
  - ViewModels with UDF state management set up for all screens
  - project builds successfully

### Task_2_ThemeAndNavigation: Set up custom Material 3 light-green financial theme and setup adaptive navigation bar/rail and top app bar for screen navigation.
- **Status:** COMPLETED
- **Updates:** Configured Material 3 light-green financial theme with custom expense category colors in Color.kt/Theme.kt. Created adaptive NavigationBar/NavigationRail navigation framework with top app bar and Jetpack Navigation 3 routing for Expenses, Timetable, Tasks, and Habits/Notes screens. App builds cleanly.
- **Acceptance Criteria:**
  - Light-green M3 financial color palette and typography configured
  - Adaptive bottom bar and navigation rail navigation working across screens
  - project builds successfully

### Task_3_UIComponentsAndScreens: Implement UI screens for Expense Tracker (Total spent, category breakdown, quick expense entry FAB bottom sheet), Timetable (weekly class schedule & deadlines), Task Tracker (priorities & completion toggles), and Habit Hub/Notes.
- **Status:** COMPLETED
- **Updates:** Implemented full ExpensesScreen with Total Spent card, category badges (Red/Blue/Purple/etc.), scrollable transaction list, light-green FAB, and Add Expense Bottom Sheet. Built TimetableScreen with day selector and class details, TasksScreen with priority tags and completion toggles, and HabitsNotesScreen with habit check-ins and quick notes. Built and unit tested successfully.
- **Acceptance Criteria:**
  - Expense Tracker UI with Total Spent card, category badges, and quick entry bottom sheet implemented
  - Timetable UI with weekly schedule and lecture/lab details implemented
  - Task Tracker UI with priorities and state toggles implemented
  - Habit Hub check-ins and Quick Notes UI implemented
  - project builds successfully

### Task_4_RunAndVerify: Run and verify application stability, instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Verified build with `./gradlew assembleDebug` and unit tests with `./gradlew testDebugUnitTest` (6 passed, 0 failed). All core student daily features (Expenses screen with light-green financial motif, Total Spent card, category-colored badges & FAB bottom sheet, Timetable screen, Tasks screen with priorities, Habits & Notes hub, adaptive NavigationBar/Rail, unique adaptive app icon) have been successfully built and verified.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Verify all 4 core tabs (Expenses, Timetable, Tasks, Habits/Notes) function properly
- **Duration:** 45h 8m 10s

