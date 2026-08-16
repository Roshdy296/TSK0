package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TodoPriority(val level: Int, val label: String) {
  LOW(0, "Low"),
  MEDIUM(1, "Medium"),
  HIGH(2, "High")
}

@Entity(tableName = "todos")
data class TodoEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val description: String = "",
  val isCompleted: Boolean = false,
  val priority: Int = TodoPriority.MEDIUM.level,
  val category: String = "Personal",
  val dueDate: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null
)
