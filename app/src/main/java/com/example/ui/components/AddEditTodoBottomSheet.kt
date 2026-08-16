package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TodoEntity
import com.example.data.TodoPriority
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

val availableCategories = listOf("Personal", "Work", "Shopping", "Fitness", "Ideas", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoBottomSheet(
  todoToEdit: TodoEntity? = null,
  onDismiss: () -> Unit,
  onSave: (title: String, description: String, priority: Int, category: String, dueDate: Long?) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  var title by remember { mutableStateOf(todoToEdit?.title ?: "") }
  var description by remember { mutableStateOf(todoToEdit?.description ?: "") }
  var selectedPriority by remember { mutableIntStateOf(todoToEdit?.priority ?: TodoPriority.MEDIUM.level) }
  var selectedCategory by remember { mutableStateOf(todoToEdit?.category ?: "Personal") }
  var dueDate by remember { mutableStateOf(todoToEdit?.dueDate) }
  var titleError by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (todoToEdit == null) "Add New Task" else "Edit Task",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        IconButton(
          onClick = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
          }
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }

      // Title Input
      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          if (it.isNotBlank()) titleError = false
        },
        label = { Text("Task Title *") },
        placeholder = { Text("e.g., Review project proposal") },
        isError = titleError,
        supportingText = {
          if (titleError) Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("todo_title_input"),
        shape = RoundedCornerShape(12.dp)
      )

      // Description / Notes Input
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Notes / Description (Optional)") },
        placeholder = { Text("Add additional details, links, or sub-tasks...") },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("todo_description_input"),
        shape = RoundedCornerShape(12.dp)
      )

      // Category Selection
      Column {
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(availableCategories) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
              selected = isSelected,
              onClick = { selectedCategory = category },
              label = { Text(category) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("category_chip_$category")
            )
          }
        }
      }

      // Priority Selection
      Column {
        Text(
          text = "Priority Level",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PriorityOption(
            title = "Low",
            level = TodoPriority.LOW.level,
            color = PriorityLow,
            isSelected = selectedPriority == TodoPriority.LOW.level,
            onClick = { selectedPriority = TodoPriority.LOW.level },
            modifier = Modifier.weight(1f)
          )
          PriorityOption(
            title = "Medium",
            level = TodoPriority.MEDIUM.level,
            color = PriorityMedium,
            isSelected = selectedPriority == TodoPriority.MEDIUM.level,
            onClick = { selectedPriority = TodoPriority.MEDIUM.level },
            modifier = Modifier.weight(1f)
          )
          PriorityOption(
            title = "High",
            level = TodoPriority.HIGH.level,
            color = PriorityHigh,
            isSelected = selectedPriority == TodoPriority.HIGH.level,
            onClick = { selectedPriority = TodoPriority.HIGH.level },
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Due Date Picker Row
      Column {
        Text(
          text = "Due Date",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Quick 'Today' button
          OutlinedButton(
            onClick = {
              val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
              }
              dueDate = cal.timeInMillis
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("quick_date_today")
          ) {
            Text("Today")
          }

          // Quick 'Tomorrow' button
          OutlinedButton(
            onClick = {
              val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
              }
              dueDate = cal.timeInMillis
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("quick_date_tomorrow")
          ) {
            Text("Tomorrow")
          }

          // Custom Date Picker Dialog
          OutlinedButton(
            onClick = {
              val cal = Calendar.getInstance()
              if (dueDate != null) cal.timeInMillis = dueDate!!
              DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                  val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                  }
                  dueDate = picked.timeInMillis
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
              ).show()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("pick_custom_date_button")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (dueDate != null) SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueDate!!))
              else "Pick Date",
              fontSize = 12.sp
            )
          }

          if (dueDate != null) {
            IconButton(
              onClick = { dueDate = null },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear date",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Cancel")
        }

        Button(
          onClick = {
            if (title.isBlank()) {
              titleError = true
            } else {
              onSave(
                title.trim(),
                description.trim(),
                selectedPriority,
                selectedCategory,
                dueDate
              )
              scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            }
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("save_todo_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (todoToEdit == null) "Create Task" else "Save Changes")
        }
      }
    }
  }
}

@Composable
private fun PriorityOption(
  title: String,
  level: Int,
  color: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clickable { onClick() }
      .testTag("priority_chip_$title"),
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
  ) {
    Row(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(color, shape = CircleShape)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
          color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
    }
  }
}
