package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TodoEntity
import com.example.ui.components.AdBanner
import com.example.ui.components.AddEditTodoBottomSheet
import com.example.ui.components.StatsSummaryCard
import com.example.ui.components.TodoItemCard
import com.example.util.GoogleCalendarSyncHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
  viewModel: TodoViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  var showAddEditSheet by remember { mutableStateOf(false) }
  var todoToEdit by remember { mutableStateOf<TodoEntity?>(null) }
  var isSearchExpanded by remember { mutableStateOf(false) }
  var showSortMenu by remember { mutableStateOf(false) }
  var showMoreMenu by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
          if (isSearchExpanded) {
            OutlinedTextField(
              value = uiState.searchQuery,
              onValueChange = { viewModel.setSearchQuery(it) },
              placeholder = { Text("Search tasks...") },
              singleLine = true,
              trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                  IconButton(onClick = { viewModel.setSearchQuery("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
                .testTag("search_tasks_input"),
              shape = RoundedCornerShape(12.dp)
            )
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlaylistAddCheck,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
              Column {
                Text(
                  text = "Task Flow",
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "${uiState.activeTasks} pending tasks",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }
        },
        actions = {
          IconButton(
            onClick = {
              isSearchExpanded = !isSearchExpanded
              if (!isSearchExpanded) viewModel.setSearchQuery("")
            },
            modifier = Modifier.testTag("toggle_search_button")
          ) {
            Icon(
              imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
              contentDescription = if (isSearchExpanded) "Close search" else "Search"
            )
          }

          // Sort Menu
          Box {
            IconButton(
              onClick = { showSortMenu = true },
              modifier = Modifier.testTag("sort_menu_button")
            ) {
              Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort options")
            }
            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = { showSortMenu = false }
            ) {
              Text(
                text = "Sort By",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              TodoSortOption.values().forEach { option ->
                DropdownMenuItem(
                  text = {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(option.label)
                      if (uiState.sortOption == option) {
                        Icon(
                          imageVector = Icons.Default.DoneAll,
                          contentDescription = "Selected",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  },
                  onClick = {
                    viewModel.setSortOption(option)
                    showSortMenu = false
                  },
                  modifier = Modifier.testTag("sort_option_${option.name}")
                )
              }
            }
          }

          // More Options Menu
          Box {
            IconButton(
              onClick = { showMoreMenu = true },
              modifier = Modifier.testTag("more_options_button")
            ) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
              expanded = showMoreMenu,
              onDismissRequest = { showMoreMenu = false }
            ) {
              DropdownMenuItem(
                text = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.CalendarMonth,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                    Text("Open Google Calendar")
                  }
                },
                onClick = {
                  showMoreMenu = false
                  val opened = GoogleCalendarSyncHelper.openGoogleCalendar(context)
                  if (!opened) {
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar("Unable to open Calendar app")
                    }
                  }
                },
                modifier = Modifier.testTag("open_google_calendar_menu_item")
              )

              DropdownMenuItem(
                text = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(
                      imageVector = if (uiState.isAdBannerVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                    Text(if (uiState.isAdBannerVisible) "Hide Ad Banner" else "Show Ad Banner")
                  }
                },
                onClick = {
                  viewModel.toggleAdBanner(!uiState.isAdBannerVisible)
                  showMoreMenu = false
                },
                modifier = Modifier.testTag("toggle_ad_banner_menu_item")
              )

              if (uiState.completedTasks > 0) {
                DropdownMenuItem(
                  text = {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                      )
                      Text(
                        "Clear Completed",
                        color = MaterialTheme.colorScheme.error
                      )
                    }
                  },
                  onClick = {
                    showMoreMenu = false
                    showClearConfirmDialog = true
                  },
                  modifier = Modifier.testTag("clear_completed_menu_item")
                )
              }
            }
          }
        }
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          todoToEdit = null
          showAddEditSheet = true
        },
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("New Task") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_task_fab")
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Ads Banner at top of content
      AnimatedVisibility(
        visible = uiState.isAdBannerVisible,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          AdBanner(
            isAdVisible = uiState.isAdBannerVisible,
            onCloseAd = { viewModel.toggleAdBanner(false) }
          )
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("todo_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Focus & Stats Card
        item(key = "stats_card") {
          StatsSummaryCard(
            totalCount = uiState.totalTasks,
            completedCount = uiState.completedTasks
          )
        }

        // Status Tabs (All / Active / Completed)
        item(key = "status_tabs") {
          TabRow(
            selectedTabIndex = uiState.filterStatus.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
          ) {
            TodoFilterStatus.values().forEach { status ->
              Tab(
                selected = uiState.filterStatus == status,
                onClick = { viewModel.setFilterStatus(status) },
                text = {
                  Text(
                    text = status.label,
                    fontWeight = if (uiState.filterStatus == status) FontWeight.Bold else FontWeight.Normal
                  )
                },
                modifier = Modifier.testTag("tab_${status.name.lowercase()}")
              )
            }
          }
        }

        // Category Horizontal Chips
        item(key = "category_chips") {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
          ) {
            items(uiState.categories) { category ->
              val isSelected = uiState.selectedCategory == category
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.setSelectedCategory(category) },
                label = { Text(category) },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_chip_$category")
              )
            }
          }
        }

        // Empty state or Task list
        if (uiState.todos.isEmpty()) {
          item(key = "empty_state") {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                  modifier = Modifier.size(64.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.PlaylistAddCheck,
                      contentDescription = null,
                      modifier = Modifier.size(32.dp),
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                }
                Text(
                  text = when {
                    uiState.searchQuery.isNotBlank() -> "No matching tasks found"
                    uiState.filterStatus == TodoFilterStatus.COMPLETED -> "No completed tasks yet"
                    uiState.filterStatus == TodoFilterStatus.ACTIVE -> "All tasks completed! Well done!"
                    else -> "No tasks in this category"
                  },
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Tap '+ New Task' to add something to your list.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        } else {
          items(
            items = uiState.todos,
            key = { it.id }
          ) { todo ->
            TodoItemCard(
              todo = todo,
              onToggleComplete = { viewModel.toggleComplete(todo) },
              onEditClick = {
                todoToEdit = todo
                showAddEditSheet = true
              },
              onDeleteClick = {
                viewModel.deleteTodo(todo)
                coroutineScope.launch {
                  val result = snackbarHostState.showSnackbar(
                    message = "Task \"${todo.title}\" deleted",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                  )
                  if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                  }
                }
              }
            )
          }
        }
      }
    }
  }

  // Add / Edit Bottom Sheet
  if (showAddEditSheet) {
    AddEditTodoBottomSheet(
      todoToEdit = todoToEdit,
      onDismiss = {
        showAddEditSheet = false
        todoToEdit = null
      },
      onSave = { title, description, priority, category, dueDate ->
        if (todoToEdit == null) {
          viewModel.addTodo(title, description, priority, category, dueDate)
        } else {
          viewModel.updateTodo(todoToEdit!!, title, description, priority, category, dueDate)
        }
        showAddEditSheet = false
        todoToEdit = null
      }
    )
  }

  // Clear Completed Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear Completed Tasks?") },
      text = { Text("This will permanently remove all finished tasks from your list.") },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.clearCompleted()
            showClearConfirmDialog = false
          },
          modifier = Modifier.testTag("confirm_clear_completed_button")
        ) {
          Text("Clear", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
