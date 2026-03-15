package com.nss.oneiitp.bus.model

class BusSchedule {
    data class BusSchedule(
        val busNo: String = "",
        val route: String = "",
        val departure: String = "",
        val arrival: String = "",
        val stop: String = ""
    )
}