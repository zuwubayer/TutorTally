package com.uwu.tutortally

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uwu.tutortally.ui.theme.Milonga
import com.uwu.tutortally.ui.theme.TutorTallyButtonGrey
import com.uwu.tutortally.ui.theme.TutorTallyDarkGrey
import com.uwu.tutortally.ui.theme.TutorTallyGreen
import com.uwu.tutortally.ui.theme.TutorTallyOrange
import com.uwu.tutortally.ui.theme.TutorTallyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class StudentViewModel(application: Application) : ViewModel() {
    private val studentDao = TutorTallyDatabase.getDatabase(application).studentDao()
    val allStudentsWithLogs: Flow<List<StudentWithLogs>> = studentDao.getStudentsWithLogs()

    private val _message = MutableSharedFlow<String>()

    fun addStudent(name: String, place: String, classes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                studentDao.insertStudent(Student(name = name, place = place, classesPerMonth = classes))
                _message.emit("Student '$name' added successfully!")
            } catch (e: Exception) {
                _message.emit("Error adding student: ${e.localizedMessage}")
            }
        }
    }

    fun logClassForStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val log = ClassLog(studentId = student.id, timestamp = System.currentTimeMillis(), cycleNumber = student.currentCycle)
                studentDao.insertClassLog(log)
                _message.emit("Class logged for ${student.name}!")
            } catch (e: Exception) {
                _message.emit("Error logging class: ${e.localizedMessage}")
            }
        }
    }

    fun startNewCycle(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedStudent = student.copy(currentCycle = student.currentCycle + 1)
                studentDao.updateStudent(updatedStudent)
                _message.emit("New cycle started for ${student.name}!")
            } catch (e: Exception) {
                _message.emit("Error starting new cycle: ${e.localizedMessage}")
            }
        }
    }

    fun deleteLog(log: ClassLog) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                studentDao.deleteClassLog(log)
                _message.emit("Class log deleted.")
            } catch (e: Exception) {
                _message.emit("Error deleting log: ${e.localizedMessage}")
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                studentDao.deleteStudent(student)
                _message.emit("Student '${student.name}' deleted.")
            } catch (e: Exception) {
                _message.emit("Error deleting student: ${e.localizedMessage}")
            }
        }
    }
}
class StudentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) { @Suppress("UNCHECKED_CAST") return StudentViewModel(application) as T }; throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            TutorTallyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    val studentViewModel: StudentViewModel = viewModel(factory = StudentViewModelFactory(context.applicationContext as Application))
                    StudentListScreen(studentViewModel)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentListScreen(viewModel: StudentViewModel) {
    val studentList by viewModel.allStudentsWithLogs.collectAsState(initial = null)
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Tally", fontWeight = FontWeight.Bold, fontFamily = Milonga) },
                actions = {
                    Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Icon")
                        Spacer(Modifier.width(4.dp))
                        Text("Add Student")
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            Footer()
        }
    ) { paddingValues ->
        when (val list = studentList) {
            null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            emptyList<StudentWithLogs>() -> {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    GoalReachedBanner(student = null)
                    EmptyState(modifier = Modifier.weight(1f), onAddStudentClicked = { showDialog = true })
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
                    val completedStudent = list.firstOrNull { studentWithLogs ->
                        val currentLogs = studentWithLogs.logs.filter { it.cycleNumber == studentWithLogs.student.currentCycle }
                        currentLogs.size >= studentWithLogs.student.classesPerMonth
                    }
                    GoalReachedBanner(student = completedStudent?.student)
                    LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(
                            items = list,
                            key = { it.student.id }
                        ) { studentWithLogs ->
                            Box(modifier = Modifier.animateItemPlacement()) {
                                StudentCard(
                                    studentWithLogs = studentWithLogs,
                                    onLogClass = { viewModel.logClassForStudent(it) },
                                    onStartNewCycle = { viewModel.startNewCycle(it) },
                                    onDeleteLog = { viewModel.deleteLog(it) },
                                    onDeleteStudent = { viewModel.deleteStudent(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddStudentDialog(onDismiss = { showDialog = false }, onAddStudent = { name, place, classes -> viewModel.addStudent(name, place, classes); showDialog = false })
    }
}

@Composable
fun Footer(@SuppressLint("ModifierParameter") modifier: Modifier = Modifier.background(Color.White)) {
    Text(text = "Tutor Tally © 2025 - by Jubayer Makki", fontSize = 12.sp, color = Color.Gray, modifier = modifier.fillMaxWidth().padding(16.dp).background(Color.White), textAlign = TextAlign.Center)
}

@Composable
fun GoalReachedBanner(student: Student?) {
    AnimatedVisibility(
        visible = student != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (student != null) {
            Row(modifier = Modifier.fillMaxWidth().background(TutorTallyOrange.copy(alpha = 0.1f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Celebration, contentDescription = "Goal Reached", tint = TutorTallyOrange)
                Spacer(Modifier.width(8.dp))
                Text(text = "Monthly goal reached for ${student.name}! Well done!", fontWeight = FontWeight.Bold, color = TutorTallyOrange.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun StudentCard(
    studentWithLogs: StudentWithLogs,
    onLogClass: (Student) -> Unit,
    onStartNewCycle: (Student) -> Unit,
    onDeleteLog: (ClassLog) -> Unit,
    onDeleteStudent: (Student) -> Unit
) {
    val student = studentWithLogs.student
    val logs = studentWithLogs.logs
    val currentLogs by remember(logs, student.currentCycle) {
        mutableStateOf(logs.filter { it.cycleNumber == student.currentCycle })
    }
    val progress by remember(currentLogs) {
        mutableIntStateOf(currentLogs.size)
    }
    val isComplete = remember(progress, student.classesPerMonth) {
        progress >= student.classesPerMonth
    }
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteStudentConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val animatedProgress by animateFloatAsState(
        targetValue = if (student.classesPerMonth > 0) progress.toFloat() / student.classesPerMonth.toFloat() else 0f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation"
    )
    val animatedColor by animateColorAsState(
        targetValue = if (isComplete) TutorTallyGreen else MaterialTheme.colorScheme.primary,
        label = "colorAnimation"
    )

    if (showDeleteStudentConfirmation) {
        AlertDialog(onDismissRequest = { showDeleteStudentConfirmation = false }, title = { Text("Delete Student") }, text = { Text("Are you sure you want to permanently delete ${student.name}? All class history will be lost.") },
            confirmButton = { Button(onClick = { onDeleteStudent(student); showDeleteStudentConfirmation = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showDeleteStudentConfirmation = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (student.place.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(text = student.place, fontSize = 14.sp, color = TutorTallyDarkGrey)
                    }
                }
                IconButton(
                    onClick = { if (isComplete) onStartNewCycle(student) else onLogClass(student) },
                    modifier = Modifier.size(48.dp).shadow(elevation = 4.dp, shape = CircleShape).background(color = animatedColor, shape = CircleShape)
                ) {
                    Icon(imageVector = if (isComplete) Icons.Default.Refresh else Icons.Default.Check, contentDescription = "Log or Reset", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(24.dp).background(color = Color.White, shape = CircleShape))
                Box(
                    modifier = Modifier.fillMaxWidth(animatedProgress).height(24.dp)
                        .background(brush = Brush.horizontalGradient(colors = listOf(animatedColor.copy(alpha = 0.7f), animatedColor)), shape = CircleShape)
                )
                
                Text(
                    text = "$progress / ${student.classesPerMonth}",
                    modifier = Modifier.align(Alignment.Center),
                    color = if (animatedProgress < 0.6f) Color.Black.copy(alpha = 0.6f) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 100)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 100))
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = Color.LightGray)
                    Text("Class History:", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)) {
                        val logsByCycle = remember(logs) {
                            logs.groupBy { it.cycleNumber }.toSortedMap(compareByDescending { it })
                        }
                        LazyColumn {
                            if (logs.isEmpty()) {
                                item { Text(text = "No classes logged yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)) }
                            } else {
                                logsByCycle.forEach { (cycle, logsInCycle) ->
                                    if (cycle < student.currentCycle) {
                                        item(key = "cycle_separator_$cycle") { CycleCompletedSeparator(cycle) }
                                    }
                                    items(
                                        items = logsInCycle.sortedByDescending { it.timestamp },
                                        key = { it.id }
                                    ) {
                                        log -> ClassLogItem(log = log, onDelete = { onDeleteLog(log) })
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                        Button(onClick = { val shareText = buildLogString(studentWithLogs); val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, shareText); type = "text/plain" }; val shareIntent = Intent.createChooser(sendIntent, "Export Log"); context.startActivity(shareIntent) }) { Text("Export Log") }
                        Button(onClick = { showDeleteStudentConfirmation = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red)) { Text("Delete Student") }
                    }
                }
            }
        }
    }
}

@Composable
fun CycleCompletedSeparator(cycleNumber: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(text = " Cycle $cycleNumber Completed ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontSize = 12.sp)
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ClassLogItem(log: ClassLog, onDelete: () -> Unit) {
    var showDeleteLogConfirmation by remember { mutableStateOf(false) }
    if (showDeleteLogConfirmation) {
        AlertDialog(onDismissRequest = { showDeleteLogConfirmation = false }, title = { Text("Delete Class Log") }, text = { Text("Are you sure you want to delete the class logged on ${SimpleDateFormat("MMMM dd 'at' hh:mm a, EEE", Locale.getDefault()).format(Date(log.timestamp))}? This action cannot be undone.") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteLogConfirmation = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showDeleteLogConfirmation = false }) { Text("Cancel") } }
        )
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(SimpleDateFormat("MMMM dd 'at' hh:mm a, EEE", Locale.getDefault()).format(Date(log.timestamp)), fontSize = 14.sp)
        IconButton(onClick = { showDeleteLogConfirmation = true }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete Log", tint = Color.Red) }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onAddStudentClicked: () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "No students icon", modifier = Modifier.size(80.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(text = "No students yet!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(text = "Ready to get started? Add your first student\nto begin tracking their progress.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAddStudentClicked, contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)) { Text("Add Your First Student", fontSize = 16.sp) }
        }
    }
}

@Composable
fun AddStudentDialog(onDismiss: () -> Unit, onAddStudent: (String, String, Int) -> Unit) {
    var studentName by remember { mutableStateOf("") }
    var tuitionPlace by remember { mutableStateOf("") }
    var classesPerMonth by remember { mutableStateOf("") }
    val hasUserInput = studentName.isNotEmpty() || tuitionPlace.isNotEmpty() || classesPerMonth.isNotEmpty()
    Dialog(onDismissRequest = { if (!hasUserInput) onDismiss() }) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add New Student", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                TextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Student Name") }, placeholder = { Text("e.g., Bishal Borno", color = Color.LightGray) }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent), singleLine = true)
                Spacer(Modifier.height(16.dp))
                TextField(value = tuitionPlace, onValueChange = { tuitionPlace = it }, label = { Text("Tuition Place") }, placeholder = { Text("e.g., Student's Home", color = Color.LightGray) }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent), singleLine = true)
                Spacer(Modifier.height(16.dp))
                TextField(value = classesPerMonth, onValueChange = { classesPerMonth = it.filter(Char::isDigit) }, label = { Text("Classes Per Month") }, placeholder = { Text("e.g., 8", color = Color.LightGray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent), singleLine = true)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TutorTallyButtonGrey, contentColor = Color.Black)) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { val classes = classesPerMonth.toIntOrNull() ?: 0; if (studentName.isNotBlank() && classes > 0) { onAddStudent(studentName, tuitionPlace, classes) } }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Add Student") }
                }
            }
        }
    }
}

private fun buildLogString(studentWithLogs: StudentWithLogs): String {
    val student = studentWithLogs.student
    val logs = studentWithLogs.logs
    val stringBuilder = StringBuilder()
    stringBuilder.append("Tutor Tally Log for: ${student.name}\n")
    stringBuilder.append("Location: ${student.place}\n")
    stringBuilder.append("----------------------------------\n\n")
    if (logs.isEmpty()) {
        stringBuilder.append("No classes logged yet.")
    } else {
        val logsByCycle = logs.groupBy { it.cycleNumber }.toSortedMap()
        logsByCycle.forEach { (cycle, logsInCycle) ->
            stringBuilder.append("--- Cycle $cycle ---\n")
            logsInCycle.sortedBy { it.timestamp }.forEach { log ->                stringBuilder.append("- ${SimpleDateFormat("MMMM dd 'at' hh:mm a, EEE", Locale.getDefault()).format(Date(log.timestamp))}\n")            }
            stringBuilder.append("\n")
        }
    }
    return stringBuilder.toString()
}

