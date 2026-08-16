package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
  @Query("SELECT * FROM todos ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
  fun getAllTodosFlow(): Flow<List<TodoEntity>>

  @Query("SELECT * FROM todos WHERE id = :id")
  suspend fun getTodoById(id: Long): TodoEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTodo(todo: TodoEntity): Long

  @Update
  suspend fun updateTodo(todo: TodoEntity)

  @Delete
  suspend fun deleteTodo(todo: TodoEntity)

  @Query("DELETE FROM todos WHERE isCompleted = 1")
  suspend fun deleteCompletedTodos()

  @Query("UPDATE todos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
  suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, completedAt: Long?)
}
