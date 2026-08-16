package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TodoDatabase
import com.example.data.TodoEntity
import com.example.data.TodoPriority
import com.example.data.TodoRepository
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoFilterStatus(val label: String) {
  ALL("All"),
  ACTIVE("Active"),
  COMPLETED("Completed")
}

enum class TodoSortOption(val label: String) {
  DUE_DATE("Due Date"),
  PRIORITY("Priority"),
  CREATED_AT("Recently Added"),
  ALPHABETICAL("Title (A-Z)")
}

private data class FilterConfig(
  val status: TodoFilterStatus,
  val category: String,
  val query: String,
  val sort: TodoSortOption,
  val adVisible: Boolean
)

data class TodoUiState(
  val todos: List<TodoEntity> = emptyList(),
  val filterStatus: TodoFilterStatus = TodoFilterStatus.ALL,
  val selectedCategory: String = "All",
  val searchQuery: String = "",
  val sortOption: TodoSortOption = TodoSortOption.PRIORITY,
  val isAdBannerVisible: Boolean = true,
  val totalTasks: Int = 0,
  val completedTasks: Int = 0,
  val activeTasks: Int = 0,
  val categories: List<String> = listOf("All", "Personal", "Work", "Shopping", "Fitness", "Ideas", "Other")
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: TodoRepository

  private val _filterStatus = MutableStateFlow(TodoFilterStatus.ALL)
  private val _selectedCategory = MutableStateFlow("All")
  private val _searchQuery = MutableStateFlow("")
  private val _sortOption = MutableStateFlow(TodoSortOption.PRIORITY)
  private val _isAdBannerVisible = MutableStateFlow(true)
  private val _lastDeletedTodo = MutableStateFlow<TodoEntity?>(null)

  init {
    val database = TodoDatabase.getDatabase(application)
    repository = TodoRepository(database.todoDao())

    // Populate starter items if brand new
    viewModelScope.launch {
      repository.allTodos.collect { list ->
        if (list.isEmpty()) {
          seedStarterTodos()
        }
      }
    }
  }

  private suspend fun seedStarterTodos() {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 18)
      set(Calendar.MINUTE, 0)
    }

    val starterItems = listOf(
      TodoEntity(
        title = "Prepare presentation for team sync",
        description = "Include quarterly progress milestones and budget projections.",
        isCompleted = false,
        priority = TodoPriority.HIGH.level,
        category = "Work",
        dueDate = cal.timeInMillis
      ),
      TodoEntity(
        title = "Buy fresh fruits and groceries",
        description = "Apples, spinach, almond milk, and whole wheat bread.",
        isCompleted = false,
        priority = TodoPriority.MEDIUM.level,
        category = "Shopping",
        dueDate = cal.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
      ),
      TodoEntity(
        title = "Morning 30-min cardio workout",
        description = "Light jog in the park or interval treadmill session.",
        isCompleted = true,
        priority = TodoPriority.LOW.level,
        category = "Fitness",
        completedAt = System.currentTimeMillis()
      ),
      TodoEntity(
        title = "Brainstorm weekend road trip ideas",
        description = "Look up hiking trails and cabin rentals near the lake.",
        isCompleted = false,
        priority = TodoPriority.LOW.level,
        category = "Ideas",
        dueDate = null
      )
    )

    starterItems.forEach { repository.insert(it) }
  }

  private val filterConfig = combine(
    _filterStatus,
    _selectedCategory,
    _searchQuery,
    _sortOption,
    _isAdBannerVisible
  ) { status, category, query, sort, adVisible ->
    FilterConfig(status, category, query, sort, adVisible)
  }

  val uiState: StateFlow<TodoUiState> = combine(
    repository.allTodos,
    filterConfig
  ) { allList: List<TodoEntity>, config: FilterConfig ->
    val filteredList = allList.filter { todo ->
      val matchesStatus = when (config.status) {
        TodoFilterStatus.ALL -> true
        TodoFilterStatus.ACTIVE -> !todo.isCompleted
        TodoFilterStatus.COMPLETED -> todo.isCompleted
      }

      val matchesCategory = config.category == "All" || todo.category.equals(config.category, ignoreCase = true)

      val matchesQuery = config.query.isBlank() ||
        todo.title.contains(config.query, ignoreCase = true) ||
        todo.description.contains(config.query, ignoreCase = true)

      matchesStatus && matchesCategory && matchesQuery
    }.sortedWith { a, b ->
      when (config.sort) {
        TodoSortOption.PRIORITY -> {
          if (a.isCompleted != b.isCompleted) {
            a.isCompleted.compareTo(b.isCompleted)
          } else {
            b.priority.compareTo(a.priority)
          }
        }
        TodoSortOption.DUE_DATE -> {
          if (a.dueDate == null && b.dueDate == null) 0
          else if (a.dueDate == null) 1
          else if (b.dueDate == null) -1
          else a.dueDate.compareTo(b.dueDate)
        }
        TodoSortOption.CREATED_AT -> b.createdAt.compareTo(a.createdAt)
        TodoSortOption.ALPHABETICAL -> a.title.compareTo(b.title, ignoreCase = true)
      }
    }

    val total = allList.size
    val completed = allList.count { it.isCompleted }
    val active = total - completed

    TodoUiState(
      todos = filteredList,
      filterStatus = config.status,
      selectedCategory = config.category,
      searchQuery = config.query,
      sortOption = config.sort,
      isAdBannerVisible = config.adVisible,
      totalTasks = total,
      completedTasks = completed,
      activeTasks = active
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = TodoUiState()
  )

  fun setFilterStatus(status: TodoFilterStatus) {
    _filterStatus.value = status
  }

  fun setSelectedCategory(category: String) {
    _selectedCategory.value = category
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSortOption(sort: TodoSortOption) {
    _sortOption.value = sort
  }

  fun toggleAdBanner(visible: Boolean) {
    _isAdBannerVisible.value = visible
  }

  fun toggleComplete(todo: TodoEntity) {
    viewModelScope.launch {
      repository.toggleComplete(todo.id, !todo.isCompleted)
    }
  }

  fun addTodo(title: String, description: String, priority: Int, category: String, dueDate: Long?) {
    viewModelScope.launch {
      val newTodo = TodoEntity(
        title = title,
        description = description,
        priority = priority,
        category = category,
        dueDate = dueDate
      )
      repository.insert(newTodo)
    }
  }

  fun updateTodo(todo: TodoEntity, title: String, description: String, priority: Int, category: String, dueDate: Long?) {
    viewModelScope.launch {
      val updated = todo.copy(
        title = title,
        description = description,
        priority = priority,
        category = category,
        dueDate = dueDate
      )
      repository.update(updated)
    }
  }

  fun deleteTodo(todo: TodoEntity) {
    viewModelScope.launch {
      _lastDeletedTodo.value = todo
      repository.delete(todo)
    }
  }

  fun undoDelete() {
    val lastDeleted = _lastDeletedTodo.value ?: return
    viewModelScope.launch {
      repository.insert(lastDeleted)
      _lastDeletedTodo.value = null
    }
  }

  fun clearCompleted() {
    viewModelScope.launch {
      repository.deleteCompleted()
    }
  }
}
