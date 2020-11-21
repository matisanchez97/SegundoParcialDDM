package com.utn.segundoparcial

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.utn.segundoparcial.constants.PRODUCTS_LIST
import com.utn.segundoparcial.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 3000 // 3 sec
    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")
    val productsCollectionRef = db.collection("products")
    val storage = Firebase.storage
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mAuth = FirebaseAuth.getInstance()

        scope.launch {
            dbInit()
        }


    }
    suspend fun dbInit() {
        try {
            lateinit var file : Uri
            lateinit var imageRef: StorageReference
            val packageName = "com.utn.segundoparcial/"
            var rootUri = "android.resource://"

            val data2 = usersCollectionRef
                .whereEqualTo("username", "debug")
                .get()
                .await()
            if (data2.isEmpty) {
                val debugUser = User("debug", "123456", "debug@gmail.com")
                val result = mAuth.createUserWithEmailAndPassword(debugUser.email,debugUser.password).await()
                result.user.let {
                    debugUser.id = it!!.uid
                    usersCollectionRef.add(debugUser).await()
                }
            }
            val data = productsCollectionRef
                .whereEqualTo("user", "debug")
                .get()
                .await()
            if (data.isEmpty) {
                for (product in PRODUCTS_LIST){
                    var uriToFile = rootUri+ packageName + "drawable/i" + product.id.toString()
                    file = Uri.parse(uriToFile)
                    imageRef = storage.reference.child("images/${file.lastPathSegment}")
                    imageRef.putFile(file).await()
                    product.addDownloadUri(imageRef.downloadUrl.await())
                    productsCollectionRef.add(product).await()
                }
            }
            startActivity(Intent(this@SplashActivity,MainActivity::class.java))
        } catch (e: Exception) {
            e.cause
        }
    }
}