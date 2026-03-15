package com.nss.oneiitp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

data class BusStop(
    val time: String = "",
    val from: String = "",
    val to: String = ""
)

data class Bus(
    val bus_name: String = "",
    val bus_number: String = "",
    val driver: String = "",
    val contact: String = "",
    val schedule: List<BusStop> = emptyList()
)

class BusScheduleActivity : ComponentActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            database = FirebaseDatabase.getInstance("https://bus-schedule-31b63-default-rtdb.asia-southeast1.firebasedatabase.app/")
            ref = database.getReference("buses")
        } catch (e: Exception) {
            Log.e("BusSchedule", "Firebase init error", e)
        }

        setContent {
            BusScheduleMainScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BusScheduleMainScreen() {
        var busList by remember { mutableStateOf(listOf<Bus>()) }
        var selectedBusName by remember { mutableStateOf("Bus 1") }
        var isDataLoaded by remember { mutableStateOf(false) }
        var errorOccurred by remember { mutableStateOf<String?>(null) }
        
        val selectedBus = remember(selectedBusName, busList) {
            busList.find { 
                val dbName = it.bus_name.replace(" ", "").lowercase()
                val uiName = selectedBusName.replace(" ", "").lowercase()
                
                dbName == uiName || 
                dbName == uiName.replace("bus", "bus0") ||
                dbName.replace("0", "") == uiName.replace("0", "") ||
                (uiName == "inst.bus" && (dbName.contains("inst") || it.bus_name.contains("Institute")))
            }
        }

        LaunchedEffect(Unit) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val buses = mutableListOf<Bus>()
                    if (snapshot.exists()) {
                        for (busSnapshot in snapshot.children) {
                            try {
                                val busName = busSnapshot.child("bus_name").getValue(String::class.java) 
                                    ?: busSnapshot.key ?: ""
                                val busNumber = busSnapshot.child("bus_number").getValue(String::class.java) ?: ""
                                val driver = busSnapshot.child("driver").getValue(String::class.java) ?: ""
                                val contact = busSnapshot.child("contact").getValue(String::class.java) ?: ""
                                
                                val schedule = mutableListOf<BusStop>()
                                val scheduleSnapshot = busSnapshot.child("schedule")
                                
                                if (scheduleSnapshot.exists()) {
                                    for (stopSnapshot in scheduleSnapshot.children) {
                                        val stop = stopSnapshot.getValue(BusStop::class.java)
                                        if (stop != null) {
                                            schedule.add(stop)
                                        } else {
                                            val sTime = stopSnapshot.child("time").getValue(String::class.java) ?: ""
                                            val sFrom = stopSnapshot.child("from").getValue(String::class.java) ?: ""
                                            val sTo = stopSnapshot.child("to").getValue(String::class.java) ?: ""
                                            if (sTime.isNotEmpty()) {
                                                schedule.add(BusStop(sTime, sFrom, sTo))
                                            }
                                        }
                                    }
                                }
                                buses.add(Bus(busName, busNumber, driver, contact, schedule))
                            } catch (e: Exception) {
                                Log.e("BusSchedule", "Error parsing bus", e)
                            }
                        }
                    }
                    busList = buses
                    isDataLoaded = true
                }

                override fun onCancelled(error: DatabaseError) {
                    errorOccurred = error.message
                    isDataLoaded = true
                }
            })
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("BUS SCHEDULE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Ultra-compact Bus Selector
                val predefinedBuses = listOf("Bus 1", "Bus 2", "Bus 3", "Bus 4", "Inst. Bus")
                
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(predefinedBuses) { busName ->
                        val isSelected = selectedBusName == busName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBusName = busName },
                            label = { Text(busName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF1E1E1E),
                                labelColor = Color.White,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black
                            ),
                            border = null,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                if (!isDataLoaded) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (errorOccurred != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading data", color = Color.Red)
                    }
                } else if (selectedBus != null) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        // Ultra-compact Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selectedBus.bus_name, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(selectedBus.bus_number, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(selectedBus.driver, style = MaterialTheme.typography.bodySmall, color = Color.White, fontSize = 11.sp)
                                    Text(selectedBus.contact, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedBus.schedule.isEmpty()) {
                            Text("No schedule available.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(top = 20.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                itemsIndexed(selectedBus.schedule) { index, stop ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        // Minimized Timeline visual elements
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(16.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                            if (index < selectedBus.schedule.lastIndex) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(1.dp)
                                                        .height(28.dp) // Drastically reduced height
                                                        .background(Color.DarkGray)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(6.dp))

                                        // Ultra-compact Schedule Card
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF222222))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = stop.from,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(10.dp).padding(horizontal = 2.dp), tint = Color.DarkGray)
                                                    Text(
                                                        text = stop.to,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                }
                                                
                                                Text(
                                                    text = stop.time,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Please select a bus to view schedule", color = Color.Gray)
                    }
                }
            }
        }
    }
}
