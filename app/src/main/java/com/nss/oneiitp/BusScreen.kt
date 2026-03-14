package com.nss.oneiitp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

@Composable
fun BusScreenUI() { // Renamed to avoid ambiguity
    val database = FirebaseDatabase.getInstance("https://oneiitp-71f82-default-rtdb.firebaseio.com/")
    val busesRef = database.getReference("buses")
    val context = LocalContext.current

    var busList by remember { mutableStateOf(listOf<String>()) } // All bus IDs
    var selectedBus by remember { mutableStateOf("") } // Currently selected bus
    var scheduleList by remember { mutableStateOf(listOf<String>()) } // Schedule for selected bus

    // Fetch bus IDs from Firebase
    LaunchedEffect(Unit) {
        busesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<String>()
                for (bus in snapshot.children) {
                    tempList.add(bus.key ?: "Unknown")
                }
                busList = tempList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ---------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select a Bus",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Buttons for all buses
        busList.forEach { busId ->
            Button(
                onClick = {
                    selectedBus = busId
                    fetchBusScheduleData(busId) { schedule -> // Renamed function call
                        scheduleList = schedule
                        if (schedule.isEmpty()) {
                            Toast.makeText(
                                context,
                                "No schedule found for $busId",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(busId)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show schedule for selected bus
        if (scheduleList.isNotEmpty()) {
            Text(
                text = "Schedule for $selectedBus",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            scheduleList.forEach { schedule ->
                Text(text = schedule)
            }
        }
    }
}

// ---------- Function to fetch schedule from Firebase ----------
fun fetchBusScheduleData(busId: String, onResult: (List<String>) -> Unit) { // Renamed to avoid ambiguity
    val ref = FirebaseDatabase.getInstance("https://oneiitp-71f82-default-rtdb.firebaseio.com/")
        .getReference("buses")
        .child(busId)
        .child("schedule")

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val temp = mutableListOf<String>()
            for (item in snapshot.children) {
                val time = item.child("time").getValue(String::class.java)
                val from = item.child("from").getValue(String::class.java)
                val to = item.child("to").getValue(String::class.java)
                if (time != null && from != null && to != null) {
                    temp.add("$time: $from → $to")
                }
            }
            onResult(temp)
        }

        override fun onCancelled(error: DatabaseError) {
            onResult(emptyList())
        }
    })
}
