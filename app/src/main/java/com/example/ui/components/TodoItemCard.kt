package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TodoEntity
import com.example.data.TodoPriority
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityLowContainer
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityMediumContainer
import com.example.util.GoogleCalendarSyncHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodoItemCard(
  todo: TodoEntity,
  onToggleComplete: (Boolean) -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isExpanded by remember { mutableStateOf(false) }

  val priorityColor = when (todo.priority) {
    TodoPriority.HIGH.level -> PriorityHigh
    TodoPriority.MEDIUM.level -> PriorityMedium
    else -> PriorityLow
  }

  val priorityContainerColor = when (todo.priority) {
    TodoPriority.HIGH.level -> PriorityHighContainer
    TodoPriority.MEDIUM.level -> PriorityMediumContainer
    else -> PriorityLowContainer
  }

  val priorityLabel = when (todo.priority) {
    TodoPriority.HIGH.level -> "High"
    TodoPriority.MEDIUM.level -> "Medium"
    else -> "Low"
  }

  val cardAlpha = if (todo.isCompleted) 0.65f else 1.0f

  Card(
    modifier = modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .testTag("todo_item_card_${todo.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (todo.isCompleted) 0.dp else 1.5.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Custom Checkbox
        Box(
          modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (todo.isCompleted) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
              width = 1.5.dp,
              color = if (todo.isCompleted) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
              shape = RoundedCornerShape(8.dp)
            )
            .clickable { onToggleComplete(!todo.isCompleted) }
            .testTag("todo_checkbox_${todo.id}"),
          contentAlignment = Alignment.Center
        ) {
          if (todo.isCompleted) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Completed",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and meta info
        Column(
          modifier = Modifier
            .weight(1f)
            .clickable { isExpanded = !isExpanded }
        ) {
          Text(
            text = todo.title,
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.SemiBold,
              textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
              color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
              else MaterialTheme.colorScheme.onSurface
            ),
            maxLines = if (isExpanded) 10 else 2,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Meta Row: Category, Priority, Due Date
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Category Badge
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ) {
              Text(
                text = todo.category,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
              )
            }

            // Priority Badge
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = priorityContainerColor.copy(alpha = 0.7f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .background(priorityColor, shape = CircleShape)
                )
                Text(
                  text = priorityLabel,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = priorityColor
                  )
                )
              }
            }

            // Due Date Badge
            if (todo.dueDate != null) {
              val isOverdue = !todo.isCompleted && todo.dueDate < System.currentTimeMillis()
              val dueDateText = formatDueDate(todo.dueDate)

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isOverdue) PriorityHighContainer.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = if (isOverdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = dueDateText,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                      color = if (isOverdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }
              }
            }
          }
        }

        // Action icons
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              GoogleCalendarSyncHelper.addEventToGoogleCalendar(context, todo)
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("todo_calendar_sync_button_${todo.id}")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = "Sync to Google Calendar",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onEditClick,
            modifier = Modifier
              .size(32.dp)
              .testTag("todo_edit_button_${todo.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Task",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
              .size(32.dp)
              .testTag("todo_delete_button_${todo.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Task",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Expandable Notes
      if (todo.description.isNotBlank()) {
        AnimatedVisibility(visible = isExpanded) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, start = 38.dp, end = 8.dp)
          ) {
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = todo.description,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(8.dp)
              )
            }
          }
        }
      }
    }
  }
}

private fun formatDueDate(timestamp: Long): String {
  val now = Calendar.getInstance()
  val due = Calendar.getInstance().apply { timeInMillis = timestamp }

  val isSameDay = now.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
    now.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

  now.add(Calendar.DAY_OF_YEAR, 1)
  val isTomorrow = now.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
    now.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

  return when {
    isSameDay -> "Today"
    isTomorrow -> "Tomorrow"
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
  }
}
