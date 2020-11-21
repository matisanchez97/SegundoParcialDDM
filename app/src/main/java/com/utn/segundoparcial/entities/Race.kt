package com.utn.segundoparcial.entities

import com.google.android.gms.maps.model.LatLng

class Race(id:Int,user:String,distance:Int,time:Long,route:String) {
    var id: Int
    var user: String
    var distance: Int
    var time: Long
    var route: String

    constructor() : this(0,"",0,0,"")

    init{
        this.id = id
        this.user = user
        this.distance = distance
        this.time = time
        this.route = route
    }

    fun routePositions(): MutableList<LatLng>{
        val allPos = this.route.split("#")
        var Positions: MutableList<LatLng> = ArrayList<LatLng>()
        allPos.toMutableList().removeAt(allPos.lastIndex)
        for (pos in allPos){
            val position = pos.split(",")
            Positions.add(LatLng(position.elementAt(0).toDouble(),position.elementAt(1).toDouble()))
        }
        return Positions
    }
}