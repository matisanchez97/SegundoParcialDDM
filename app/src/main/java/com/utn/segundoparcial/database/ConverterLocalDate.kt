package com.utn.segundoparcial.database

import androidx.room.TypeConverter
import java.time.LocalDate

class ConverterLocalDate {
    companion object{
        @TypeConverter
        @JvmStatic
        fun fromLocalDate(value: LocalDate):String {
            return value.toString()
        }

        @TypeConverter
        @JvmStatic
        fun toLocalDate(value: String):LocalDate {
            var date: LocalDate
            var dateInt: MutableList<Int> = ArrayList<Int>()
            var aux1: String
            var aux2: String
            aux1 = value
            aux2 = aux1.substringBefore("-")
            while (aux2!=aux1)
            {
                dateInt.add(aux2.toInt())
                aux2+="-"
                aux1 = aux1.removePrefix(aux2)
                aux2 = aux1.substringBefore("-")
            }
            dateInt.add(aux2.toInt())
            date = LocalDate.of(dateInt[0],dateInt[1],dateInt[2])
            return date

        }
    }
}