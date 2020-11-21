package com.utn.segundoparcial.entities

class Race(id:Int,user:String,distance:Int,time:Long) {
    var id: Int
    var user: String
    var distance: Int
    var time: Long

    constructor() : this(0,"",0,0)

    init{
        this.id = id
        this.user = user
        this.distance = distance
        this.time = time
    }
}