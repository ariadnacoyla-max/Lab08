
package com.example.lab08

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.lab08.ui.theme.Lab08Theme
import java.util.Calendar

private val Lila = Color(0xFF7952B3)
private val LilaClaro = Color(0xFFF2EAFE)
private val Fondo = Color(0xFFFAF7FF)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "task_db"
        ).build()

        val viewModel = TaskViewModel(db.taskDao())

        setContent {
            Lab08Theme {
                TaskScreen(viewModel)
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {

    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()

    var newTaskDescription by remember { mutableStateOf("") }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }

    // Solicitar permiso para mostrar notificaciones
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(
                    context,
                    "Activa las notificaciones para recibir recordatorios",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun scheduleTaskReminder(task: Task) {

        if (!hasNotificationPermission()) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
            return
        }

        val now = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->

                TimePickerDialog(
                    context,
                    { _, hour, minute ->

                        val reminderCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val reminderTime = reminderCalendar.timeInMillis

                        if (reminderTime <= System.currentTimeMillis()) {
                            Toast.makeText(
                                context,
                                "Selecciona una fecha y hora futuras",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            ReminderScheduler.scheduleReminder(
                                context,
                                task,
                                reminderTime
                            )

                            Toast.makeText(
                                context,
                                "Recordatorio programado 💜",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    Scaffold(
        containerColor = Fondo
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Text(
                text = "Mis tareas 💜",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Lila
            )

            Text(
                text = "Organiza tu día a tu manera",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = LilaClaro
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Pendientes: ${tasks.count { !it.isCompleted }}",
                        color = Lila,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Completadas: ${tasks.count { it.isCompleted }}",
                        color = Lila,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Nueva tarea") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newTaskDescription.isNotBlank()) {
                        viewModel.addTask(newTaskDescription)
                        newTaskDescription = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lila
                )
            ) {
                Text("＋ Agregar tarea")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tus tareas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Lila
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id }) { task ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = {
                                        if (!task.isCompleted) {
                                            ReminderScheduler.cancelReminder(
                                                context,
                                                task.id
                                            )
                                        }
                                        viewModel.toggleTaskCompletion(task)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Lila
                                    )
                                )

                                Text(
                                    text = task.description,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium,
                                    textDecoration =
                                        if (task.isCompleted)
                                            TextDecoration.LineThrough
                                        else
                                            TextDecoration.None
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        taskToEdit = task
                                        editedDescription = task.description
                                    }
                                ) {
                                    Text("✏️ Editar", color = Lila)
                                }

                                TextButton(
                                    onClick = {
                                        ReminderScheduler.cancelReminder(
                                            context,
                                            task.id
                                        )
                                        viewModel.deleteTask(task)
                                    }
                                ) {
                                    Text(
                                        "🗑️ Eliminar",
                                        color = Color(0xFFD44A66)
                                    )
                                }
                            }

                            if (!task.isCompleted) {
                                OutlinedButton(
                                    onClick = {
                                        scheduleTaskReminder(task)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "🔔 Programar recordatorio",
                                        color = Lila
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (tasks.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        tasks.forEach { task ->
                            ReminderScheduler.cancelReminder(
                                context,
                                task.id
                            )
                        }
                        viewModel.deleteAllTasks()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Eliminar todas las tareas",
                        color = Lila
                    )
                }
            }
        }
    }

    if (taskToEdit != null) {
        AlertDialog(
            onDismissRequest = {
                taskToEdit = null
            },
            title = {
                Text("Editar tarea 💜")
            },
            text = {
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = {
                        editedDescription = it
                    },
                    label = {
                        Text("Descripción de la tarea")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToEdit?.let { task ->
                            ReminderScheduler.cancelReminder(
                                context,
                                task.id
                            )
                            viewModel.editTask(
                                task,
                                editedDescription
                            )
                        }
                        taskToEdit = null
                    },
                    enabled = editedDescription.isNotBlank()
                ) {
                    Text("Guardar", color = Lila)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        taskToEdit = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
