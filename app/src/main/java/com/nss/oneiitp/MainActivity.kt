package com.nss.oneiitp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import com.nss.oneiitp.ui.theme.OneiitpTheme

data class StudentInfo(
    val name: String = "",
    val group_no: Int = 0,
    val roll_no: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OneiitpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    var showSearch by remember { mutableStateOf(false) }

    if (!showSearch) {
        HomeScreen(onTimetableClick = { showSearch = true }, modifier = modifier)
    } else {
        StudentSearchScreen(onBack = { showSearch = false }, modifier = modifier)
    }
}

@Composable
fun HomeScreen(onTimetableClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { onTimetableClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Time-Table",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun StudentSearchScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var rollNumber by remember { mutableStateOf("") }
    var studentInfo by remember { mutableStateOf<StudentInfo?>(null) }
    var timetableData by remember { mutableStateOf<Map<String, String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val database = remember { 
        FirebaseDatabase.getInstance("https://oneiitp-71f82-default-rtdb.firebaseio.com/").reference 
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search Timetable",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = rollNumber,
            onValueChange = { rollNumber = it },
            label = { Text("Enter Roll Number (e.g. 2501AI01)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (rollNumber.isNotBlank()) {
                    val cleanRoll = rollNumber.trim().uppercase()
                    isLoading = true
                    errorMessage = null
                    studentInfo = null
                    timetableData = null
                    
                    database.child(cleanRoll)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            isLoading = false
                            if (snapshot.exists()) {
                                try {
                                    studentInfo = snapshot.child("student").getValue(StudentInfo::class.java)
                                    val timetableSnapshot = snapshot.child("timetable")
                                    if (timetableSnapshot.exists()) {
                                        @Suppress("UNCHECKED_CAST")
                                        timetableData = timetableSnapshot.value as? Map<String, String>
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Data parsing error: ${e.localizedMessage}"
                                }
                            } else {
                                errorMessage = "No record found for roll number: $cleanRoll"
                            }
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            errorMessage = "Connection error: ${exception.localizedMessage}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        }

        studentInfo?.let { info ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Personal Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow(label = "Name", value = info.name)
                    InfoRow(label = "Roll No", value = info.roll_no)
                    InfoRow(label = "Group No", value = info.group_no.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            timetableData?.let { timetable ->
                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                
                days.forEach { day ->
                    val daySchedule = timetable.filterKeys { it.startsWith(day, ignoreCase = true) }
                    
                    if (daySchedule.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = day, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 20.sp, 
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                daySchedule.toSortedMap().forEach { (timeKey, subject) ->
                                    val timeLabel = timeKey
                                        .replace(day, "", ignoreCase = true)
                                        .replace("_", " ")
                                        .trim()
                                    
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = if (timeLabel.isEmpty()) "Full Day" else timeLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(text = subject, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value)
    }
}
