package com.utn.segundoparcial.framework

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.Product
import com.utn.segundoparcial.entities.Race
import com.utn.segundoparcial.entities.User
import kotlinx.coroutines.tasks.await

suspend fun getUserById(userid:String): User? {
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    var currentUser: User? = null

    try{
        val data = usersCollectionRef
            .whereEqualTo("id",userid)
            .get()
            .await()

        currentUser = data.elementAt(0).toObject<User>()
    }
    catch (e:Exception){

    }
    return currentUser
}

suspend fun getRaceByIdandUser(userId:String,raceId:Int): Race? {
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    val racesCollectionRef = db.collection("races")
    var currentUser: User? = null
    var selectedRace: Race? = null

    try{
        val data = usersCollectionRef
            .whereEqualTo("id",userId)
            .get()
            .await()

        currentUser = data.elementAt(0).toObject<User>()
        val data2 = racesCollectionRef
            .whereEqualTo("user",currentUser.username)
            .whereEqualTo("id",raceId)
            .get()
            .await()
        selectedRace =  data2.elementAt(0).toObject<Race>()

    }
    catch (e:Exception){

    }
    return selectedRace
}

suspend fun getRacesByUser(userId:String): MutableList<Race> {
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    val racesCollectionRef = db.collection("races")
    var currentUser: User? = null
    var selectedRaces: MutableList<Race> = ArrayList<Race>()

    try{
        val data = usersCollectionRef
            .whereEqualTo("id",userId)
            .get()
            .await()

        currentUser = data.elementAt(0).toObject<User>()
        val data2 = racesCollectionRef
            .whereEqualTo("user",currentUser.username)
            .get()
            .await()
        for(race in data2){
            selectedRaces.add(race.toObject<Race>())
        }
    }
    catch (e:Exception){

    }
    return selectedRaces
}

suspend fun deleteAllRaces(selectedRaces: MutableList<Race>?){

    val db = Firebase.firestore
    val storage = Firebase.storage
    val racesCollectionRef = db.collection("races")
    val isValid = !(selectedRaces!!.any {
        it.user != selectedRaces!!.elementAt(0).user
    })
    try {
        if (isValid) {
            val data = racesCollectionRef
                .whereEqualTo("user",selectedRaces.elementAt(0).user)
                .get()
                .await()
            for (race in data) {
                if (selectedRaces!!.any {
                        it.user == race.toObject<Race>().user && it.id == race.toObject<Race>().id
                    }) {
                    val raceRef = storage.reference.child("images/" + race.toObject<Race>().user + race.toObject<Race>().id.toString() )
                    race.reference.delete().await()
                    raceRef.delete().await()

                }
            }
        }
    }
    catch (e:Exception){

    }
}

suspend fun addRace(race: Race){
    val db = Firebase.firestore
    val racesCollectionRef = db.collection("races")
    try{

        val data2 = racesCollectionRef
            .add(race)
            .await()

    }
    catch (e:Exception){

    }
}

suspend fun updateRace(race: Race){
    val db = Firebase.firestore
    val racesCollectionRef = db.collection("races")
    try{

        val data2 = racesCollectionRef
            .whereEqualTo("user",race.user)
            .whereEqualTo("id",race.id)
            .get()
            .await()
        data2.elementAt(0).reference.set(race).await()

    }
    catch (e:Exception){

    }
}

suspend fun setPrefs(currentUser: User?,context: Context):Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")

    lateinit var settingName: String
    lateinit var settingPassword: String

    settingName = prefs.getString("first_name", currentUser?.name)!!
    settingPassword = prefs.getString("password", currentUser?.password)!!
    if (settingName.isEmpty()) {
        editor.putString("first_name", currentUser?.name)
        settingName = currentUser!!.name
    }
    if (settingPassword.isEmpty()) {
        editor.putString("password", currentUser?.password)
        settingPassword = currentUser!!.password
    }
    editor.apply()
    if (currentUser?.name != settingName)
        currentUser?.name = settingName
    if (currentUser?.password != settingPassword)
        currentUser?.password = settingPassword
    try {
        val data = usersCollectionRef
            .whereEqualTo("id",currentUser?.id)
            .get()
            .await()
        data
            .elementAt(0)
            .reference
            .set(currentUser!!)
    }
    catch (e:Exception){

    }

    return prefs.getString("sort","0")!!.toInt()

}

