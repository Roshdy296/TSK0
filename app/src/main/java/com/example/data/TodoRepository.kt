package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
  val allTodos: Flow<List<TodoEntity>> = todoDao.getAllTodosFlow()

  suspend fun insert(todo: TodoEntity): Long = todoDao.insertTodo(todo)

  suspend fun update(todo: TodoEntity) = todoDao.updateTodo(todo)

  suspend fun delete(todo: TodoEntity) = todoDao.deleteTodo(todo)

  suspend fun deleteCompleted() = todoDao.deleteCompletedTodos()

  suspend fun toggleComplete(id: Long, isCompleted: Boolean) {
    val completedAt = if (isCompleted) System.currentTimeMillis() else null
    todoDao.updateCompletionStatus(id, isCompleted, completedAt)
  }
}
