package com.utn.segundoparcial.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.utn.segundoparcial.R
import com.utn.segundoparcial.entities.User
import com.utn.segundoparcial.framework.getRaceByIdandUser
import com.wajahatkarim3.roomexplorer.RoomExplorer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.LocalDate


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    lateinit var v: View
    lateinit var textFieldMail: TextInputLayout
    lateinit var imageView: ImageView
    lateinit var textFieldPass: TextInputLayout
    lateinit var butRegister: Button
    lateinit var butLogin: Button
    lateinit var inputUser: User
    lateinit var loginLayout: ConstraintLayout
    lateinit var user: User
    private lateinit var mAuth: FirebaseAuth
    var users: MutableList<User>? = ArrayList<User>()
    var userFound = false

    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_login, container, false)
        textFieldMail = v.findViewById(R.id.textFieldMail)
        textFieldPass = v.findViewById(R.id.textFieldPass)
        butLogin = v.findViewById(R.id.buttonLogin)
        butRegister = v.findViewById(R.id.buttonRegister)
        loginLayout = v.findViewById(R.id.loginLayout)
        imageView = v.findViewById(R.id.imageView2)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }
    override fun onStart() {
        super.onStart()
        scope.launch {
            getUsers()
            userFound = false
        }

        butRegister.setOnClickListener {
            val action_1 = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            v.findNavController().navigate(action_1)
        }

        butLogin.setOnClickListener() {
            scope.launch {
                if(validateInput())
                    signInUser()
                else {
                    if (inputUser.email.isBlank())
                        textFieldMail.error = getString(R.string.error_msg)
                    if (inputUser.password.isBlank())
                        textFieldPass.error = getString(R.string.error_msg)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = prefs.edit()
        editor.putString("first_name", "")
        editor.putString("password", "")
        editor.apply()
    }

    suspend fun getUsers(){
        try {
            val data = usersCollectionRef
                .get()
                .await()
            for (user in data)
                users?.add(user.toObject<User>())
        }
        catch (e:Exception){
        }
    }

    suspend fun validateInput():Boolean{
        inputUser = User(textFieldPass.editText!!.text.toString(),textFieldMail.editText!!.text.toString())
        textFieldMail.error = null
        textFieldPass.error =null
        return inputUser.email.isNotBlank() && inputUser.password.isNotBlank()
    }

    suspend fun signInUser(){
        try {
            val result =
                mAuth.signInWithEmailAndPassword(inputUser.email, inputUser.password).await()
            result.user.let {
                val action_2 =
                    LoginFragmentDirections.actionLoginFragmentToWelcomeFragment(
                        it!!.uid
                    )
                v.findNavController().navigate(action_2)
            }
        }
        catch (e:FirebaseAuthException){
            if (e.errorCode == "ERROR_WRONG_PASSWORD")
                textFieldPass.error = e.message
            if (e.errorCode == "ERROR_USER_NOT_FOUND")
                textFieldMail.error = e.message
            if (e.errorCode == "ERROR_INVALID_EMAIL")
                textFieldMail.error = e.message
            if (e.errorCode == "ERROR_USER_MISMATCH")
                textFieldMail.error = e.message
        }
    }
}