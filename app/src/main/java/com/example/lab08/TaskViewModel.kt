
package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    // Estado para la lista de tareas
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    // Cargar las tareas al iniciar
    init {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    // Agregar una nueva tarea
    fun addTask(description: String) {
        if (description.isBlank()) return

        val newTask = Task(description = description.trim())

        viewModelScope.launch {
            dao.insertTask(newTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    // Cambiar el estado de una tarea
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isCompleted = !task.isCompleted
            )

            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    // NUEVO: Editar la descripción de una tarea
    fun editTask(task: Task, newDescription: String) {
        if (newDescription.isBlank()) return

        viewModelScope.launch {
            val updatedTask = task.copy(
                description = newDescription.trim()
            )

            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    // NUEVO: Eliminar una tarea individualmente
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            _tasks.value = dao.getAllTasks()
        }
    }

    // Eliminar todas las tareas
    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            _tasks.value = emptyList()
        }
    }
}

