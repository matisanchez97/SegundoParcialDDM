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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Race
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RaceListAdapter (private var raceList: MutableList<Race>, val onItemClick : (Int, CardView) -> Unit, val onItemLongClick : (Int, CardView) -> Unit) : RecyclerView.Adapter<RaceListAdapter.RaceListHolder>(){
    class RaceListHolder (v: View) : RecyclerView.ViewHolder(v){

        private var view: View

        init {
            this.view = v
        }

        fun setRace( race_date: Long){
            val txt: TextView = view.findViewById(R.id.textAccItem)
            val txt2: TextView = view.findViewById(R.id.textDateItem)
            val txt3: TextView = view.findViewById(R.id.textTimeItem)
            val raceDate =  LocalDateTime.ofEpochSecond(race_date,0, ZoneOffset.UTC)
            txt.text = raceDate.format(DateTimeFormatter.ofPattern("EEEE")) + " Run"
            txt2.text = raceDate.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
            txt3.text = raceDate.format(DateTimeFormatter.ofPattern("h:mm a"))

        }


        fun getCardLayout(): CardView {
            return view.findViewById(R.id.cardAccItem)
        }

        fun getImageView(): ImageView {
            return view.findViewById(R.id.imageAccItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_item,parent,false)
        return (RaceListHolder(view))
    }

    override fun onBindViewHolder(holder: RaceListHolder, position: Int) {


        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Main + parentJob)
        scope.launch {
            loadHolder(holder,position)
        }

    }
    suspend fun loadHolder(holder: RaceListHolder, position: Int){

        holder.setRace(raceList[position].date)
        holder.getCardLayout().setOnClickListener {
            onItemClick(position,holder.getCardLayout())
        }
        holder.getCardLayout().setOnLongClickListener {
            onItemLongClick(position,holder.getCardLayout())
            true
        }
        try {
            Glide.with(holder.getImageView().context)
                .load(Uri.parse(raceList[position].downloadUri))
                .into(holder.getImageView())
        }
        catch (e:Exception){
            e.cause
        }
    }

    override fun getItemCount(): Int {
        return raceList.size
    }
}