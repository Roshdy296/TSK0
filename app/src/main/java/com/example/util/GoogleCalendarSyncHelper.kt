package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.data.TodoEntity
import java.util.Calendar

object GoogleCalendarSyncHelper {

  /**
   * Opens the system Calendar app / Google Calendar event creation intent
   * with the todo title, description, category, and target due date pre-filled.
   */
  fun addEventToGoogleCalendar(context: Context, todo: TodoEntity): Boolean {
    return try {
      val startTimeMillis = todo.dueDate ?: (System.currentTimeMillis() + 3600 * 1000)
      val endTimeMillis = startTimeMillis + 3600 * 1000 // 1 hour duration

      val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, todo.title)
        putExtra(
          CalendarContract.Events.DESCRIPTION,
          buildString {
            if (todo.description.isNotBlank()) {
              append(todo.description)
              append("\n\n")
            }
            append("Category: ${todo.category}\n")
            append("Priority: ${if (todo.priority >= 3) "High" else if (todo.priority == 2) "Medium" else "Low"}\n")
            append("Created from Task Flow")
          }
        )
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
        putExtra(CalendarContract.Events.EVENT_LOCATION, todo.category)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Opens the Google Calendar app directly to today's schedule or calendar view.
   */
  fun openGoogleCalendar(context: Context): Boolean {
    return try {
      val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = builder.build()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }
}
