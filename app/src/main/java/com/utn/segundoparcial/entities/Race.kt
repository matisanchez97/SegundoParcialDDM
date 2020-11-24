package com.utn.segundoparcial.entities

import com.google.android.gms.maps.model.LatLng

class Race(id:Int,user:String,distance:Int,time:Long,route:String,date:Long,timePerKm: List<Int>) {
    var id: Int
    var user: String
    var distance: Int
    var time: Long
    var downloadUri: String
    var route: String
    var date: Long
    var timePerKm: List<Int>

    constructor() : this(0,"",0,0,"",0, listOf<Int>())

    init{
        this.id = id
        this.user = user
        this.distance = distance
        this.time = time
        this.route = route
        this.date = date
        this.downloadUri = ""
            this.timePerKm = timePerKm
    }

}