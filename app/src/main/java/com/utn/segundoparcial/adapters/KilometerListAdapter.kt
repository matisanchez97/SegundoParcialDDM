package com.utn.segundoparcial.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class KilometerListAdapter (private var kmList: MutableList<Int>) : RecyclerView.Adapter<KilometerListAdapter.KilometerListHolder>(){
    class KilometerListHolder (v: View) : RecyclerView.ViewHolder(v){

        private var view: View

        init {
            this.view = v
        }

        fun setRace( timePerKm: Int, position: Int){
            val txt: TextView = view.findViewById(R.id.textKmItem)
            val txt2: TextView = view.findViewById(R.id.textTimeItem)
            val text = (position + 1).toString() + "° Kilometer of the Run:"
            val text2 = timePerKm.div(60).toString() + "' " + timePerKm.rem(60).toString() +"''"
            txt.text = text
            txt2.text = text2

        }

        fun getCardLayout(): CardView {
            return view.findViewById(R.id.cardKmItem)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KilometerListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.kilometer_item,parent,false)
        return (KilometerListHolder(view))
    }

    override fun onBindViewHolder(holder: KilometerListHolder, position: Int) {


        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        scope.launch {
            loadHolder(holder,position)
        }

    }
    suspend fun loadHolder(holder: KilometerListHolder, position: Int){

        holder.setRace(kmList[position],position)

    }

    override fun getItemCount(): Int {
        return kmList.size
    }
}