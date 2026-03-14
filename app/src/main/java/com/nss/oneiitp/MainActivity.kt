package com.nss.oneiitp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.PropertyName
import com.nss.oneiitp.ui.theme.OneiitpTheme
import org.json.JSONArray
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import com.google.firebase.database.DataSnapshot

data class StudentInfo(
    @get:PropertyName("NAME") @set:PropertyName("NAME") var name: String = "",
    @get:PropertyName("GROUP NO.") @set:PropertyName("GROUP NO.") var group_no: Int = 0,
    @get:PropertyName("ROLL NO.") @set:PropertyName("ROLL NO.") var roll_no: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            OneiitpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black
                    ) { innerPadding ->
                        MainNavigation(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            onTimetableClick = { currentScreen = "timetable" },
            onProfileClick = { currentScreen = "profile" },
            modifier = modifier
        )
        "timetable" -> StudentTimetableScreen(
            onBack = { currentScreen = "home" },
            modifier = modifier
        )
        "profile" -> ProfileScreen(
            onBack = { currentScreen = "home" },
            modifier = modifier
        )
    }
}

@Composable
fun HomeScreen(
    onTimetableClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
    val email = prefs.getString("email", "") ?: ""
    
    val displayName = remember(email) {
        val namePart = email.substringBefore("_")
        if (namePart.isNotEmpty()) {
            namePart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else {
            "Student"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = "Hello $displayName! 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OneIITP",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        title = "Time\nTable",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onTimetableClick,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DashboardCard(
                        title = "Mess\nMenu",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            val intent = Intent(context, MessMenuActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        title = "Bus\nSchedule",
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            val intent = Intent(context, BusScheduleActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    // Empty space to balance the row
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
    val email = prefs.getString("email", "") ?: ""
    
    val studentInfo = remember(email) { getStudentInfoFromEmail(context, email) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                studentInfo?.let { info ->
                    ProfileInfoRow(label = "Name", value = info.name)
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileInfoRow(label = "Roll NO", value = info.roll_no)
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileInfoRow(label = "Group NO.", value = info.group_no.toString())
                    Spacer(modifier = Modifier.height(12.dp))
                }
                ProfileInfoRow(label = "Email", value = email)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // Clear login state and navigate to LoginActivity
                prefs.edit().clear().apply()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Logout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "$label:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White)
    }
}

@Composable
fun StudentTimetableScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
    val userEmail = prefs.getString("email", "") ?: ""
    
    // Fetch user's own info from JSON immediately
    val ownInfo = remember(userEmail) { getStudentInfoFromEmail(context, userEmail) }

    var displayedStudentInfo by remember { mutableStateOf<StudentInfo?>(ownInfo) }
    var timetableData by remember { mutableStateOf<Map<String, String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var isSearchingOther by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val database = remember { 
        FirebaseDatabase.getInstance("https://oneiitp-71f82-default-rtdb.firebaseio.com/").reference 
    }

    // Effect to load own timetable automatically when entering
    LaunchedEffect(ownInfo) {
        if (ownInfo != null) {
            displayedStudentInfo = ownInfo
            fetchTimetableData(context, database, ownInfo.roll_no, 
                onLoading = { isLoading = it },
                onSuccess = { info, table -> 
                    // Update only if info is found, otherwise keep local
                    if (info != null) {
                        displayedStudentInfo = info
                    }
                    timetableData = table
                },
                onError = { 
                    errorMessage = it
                    // Keep displayedStudentInfo even on error to show Name/Roll
                }
            )
        } else {
            errorMessage = "Could not find your student record."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { 
                isSearchingOther = !isSearchingOther 
                if (!isSearchingOther && ownInfo != null) {
                    displayedStudentInfo = ownInfo
                    errorMessage = null
                    fetchTimetableData(context, database, ownInfo.roll_no,
                        onLoading = { isLoading = it },
                        onSuccess = { info, table ->
                            if (info != null) displayedStudentInfo = info
                            timetableData = table
                        },
                        onError = { errorMessage = it }
                    )
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isSearchingOther) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }

        // Heading: Timetable
        Text(
            text = "Timetable",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            textAlign = TextAlign.Start
        )

        // Search Field
        if (isSearchingOther) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Enter Roll Number", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            fetchTimetableData(context, database, searchQuery.trim().uppercase(), 
                                onLoading = { isLoading = it },
                                onSuccess = { info, table -> 
                                    displayedStudentInfo = info
                                    timetableData = table
                                    errorMessage = null
                                },
                                onError = { 
                                    errorMessage = it 
                                    timetableData = null
                                }
                            )
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            )
        }

        // User Info Box (ALWAYS SHOWN IMMEDIATELY)
        displayedStudentInfo?.let { info ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Name: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = info.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Roll NO: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = info.roll_no, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Group NO.: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = info.group_no.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Subtle loading indicator below the info box
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        errorMessage?.let {
            Text(
                text = it, 
                color = MaterialTheme.colorScheme.error, 
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Timetable Section
        timetableData?.let { timetable ->
            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            days.forEach { day ->
                val daySchedule = timetable.filterKeys { it.startsWith(day, ignoreCase = true) }
                if (daySchedule.isNotEmpty()) {
                    TimetableDayCard(day, daySchedule)
                }
            }
        }
    }
}

@Composable
fun TimetableDayCard(day: String, schedule: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = day, 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            
            schedule.toSortedMap().forEach { (timeKey, subject) ->
                val timeLabel = timeKey
                    .replace(day, "", ignoreCase = true)
                    .replace("_", " ")
                    .trim()
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (timeLabel.isEmpty()) "Full Day" else timeLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = subject,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

private fun getStudentInfoFromEmail(context: Context, email: String): StudentInfo? {
    try {
        val `is`: InputStream = context.resources.openRawResource(R.raw.student_data)
        val json = `is`.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (email.equals(obj.optString("INSTITUTE_EMAIL_ID"), ignoreCase = true)) {
                return StudentInfo(
                    name = obj.optString("NAME"),
                    roll_no = obj.optString("ROLL NO."),
                    group_no = obj.optInt("GROUP NO.")
                )
            }
        }
    } catch (e: Exception) {
        Log.e("MainActivity", "Error reading student data JSON", e)
    }
    return null
}

private fun getStudentInfoByRoll(context: Context, roll: String): StudentInfo? {
    try {
        val `is`: InputStream = context.resources.openRawResource(R.raw.student_data)
        val json = `is`.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (roll.equals(obj.optString("ROLL NO."), ignoreCase = true)) {
                return StudentInfo(
                    name = obj.optString("NAME"),
                    roll_no = obj.optString("ROLL NO."),
                    group_no = obj.optInt("GROUP NO.")
                )
            }
        }
    } catch (e: Exception) {
        Log.e("MainActivity", "Error reading student data JSON", e)
    }
    return null
}

private fun fetchTimetableData(
    context: Context,
    database: com.google.firebase.database.DatabaseReference,
    roll: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: (StudentInfo?, Map<String, String>?) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    database.child(roll).get()
        .addOnSuccessListener { snapshot ->
            onLoading(false)
            if (snapshot.exists()) {
                // Safe extraction of student info using map to avoid dot in path crash
                val studentNode = snapshot.child("student")
                val studentMap = studentNode.value as? Map<*, *>
                
                val fName = studentMap?.get("NAME")?.toString() 
                    ?: studentMap?.get("name")?.toString() ?: ""
                val fRoll = studentMap?.get("ROLL NO.")?.toString() 
                    ?: studentMap?.get("roll_no")?.toString() ?: ""
                val fGroupStr = studentMap?.get("GROUP NO.")?.toString()
                    ?: studentMap?.get("group_no")?.toString() ?: "0"
                val fGroup = fGroupStr.toIntOrNull() ?: 0
                
                // Always fetch from local JSON as fallback for correct identification
                val localInfo = getStudentInfoByRoll(context, roll)
                
                // Merge: Prefer local info for identifying details if firebase has blanks
                val finalInfo = StudentInfo(
                    name = if (fName.isNotBlank()) fName else (localInfo?.name ?: "Unknown"),
                    roll_no = if (fRoll.isNotBlank()) fRoll else (localInfo?.roll_no ?: roll),
                    group_no = if (fGroup != 0) fGroup else (localInfo?.group_no ?: 0)
                )
                
                @Suppress("UNCHECKED_CAST")
                val timetableNode = snapshot.child("timetable")
                val tableRaw = timetableNode.value as? Map<*, *>
                val table = tableRaw?.map { it.key.toString() to it.value.toString() }?.toMap()

                onSuccess(finalInfo, table)
            } else {
                onError("No timetable found for Roll No: $roll")
            }
        }
        .addOnFailureListener {
            onLoading(false)
            onError("Network error: ${it.localizedMessage}")
        }
}
