package com.utn.segundoparcial.entities

import com.google.android.gms.maps.model.LatLng

class Race(id:Int,user:String,distance:Int,time:Long,route:String) {
    var id: Int
    var user: String
    var distance: Int
    var time: Long
    var downloadUri: String
    var route: String

    constructor() : this(0,"",0,0,"")

    init{
        this.id = id
        this.user = user
        this.distance = distance
        this.time = time
        this.route = route
        this.downloadUri = ""
    }

}