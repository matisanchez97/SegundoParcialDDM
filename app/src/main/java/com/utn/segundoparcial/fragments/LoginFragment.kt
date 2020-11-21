package com.utn.segundoparcial.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.R
import com.utn.segundoparcial.constants.PRODUCTS_LIST
import com.utn.segundoparcial.entities.User
import com.wajahatkarim3.roomexplorer.RoomExplorer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    lateinit var v: View
    lateinit var textFieldMail: TextInputLayout
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
    val productsCollectionRef = db.collection("products")

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
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }
    override fun onStart() {
        super.onStart()
        scope.launch {
            dbInit()
            getUsers()
            userFound = false
        }

        butRegister.setOnClickListener {
            val action_1 = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            v.findNavController().navigate(action_1)
        }

        butLogin.setOnClickListener() {
            scope.launch {
                if (validateInput()) {
                    signInUser()
                } else {                                                                       //Si alguno de los campos esta vacion, devuelvo un mensaje de error
                    if (inputUser.username.isBlank())
                        textFieldMail.error = getString(R.string.error_msg)
                    if (inputUser.password.isBlank())
                        textFieldPass.error = getString(R.string.error_msg)
                }
            }
        }
    }
            /*inputUser = User(textFieldPass.editText!!.text.toString(),textFieldMail.editText!!.text.toString())
            textFieldMail.error = null
            textFieldPass.error =null
            if(){     //Si los dos campos estan completos
                mAuth.signInWithEmailAndPassword(inputUser.email,inputUser.password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (user in users!!)
                                if (user.checkEmail(inputUser.email)) {
                                    val action_2 =
                                        LoginFragmentDirections.actionLoginFragmentToWelcomeFragment(
                                            user.id
                                        )
                                    v.findNavController().navigate(action_2)
                                }
                        } else {
                            textFieldPass.error = getString(R.string.error_msg_pass)
                        }
                    }
                /*for (user in users!!)  {
                    if(user.checkEmail(inputUser.email)){                         //Chequeo si el usuario de la base de datos tiene ese nombre
                        userFound = true
                        if (user.checkPassword(inputUser.password)){                    //Cuando encuentro uno chqueo si tiene la contraseña ingresada
                            Snackbar.make(loginLayout,"Welcome " + user.name.toString(), Snackbar.LENGTH_SHORT).show()
                            val action_2 = LoginFragmentDirections.actionLoginFragmentToWelcomeFragment(user.id)
                            v.findNavController().navigate(action_2)
                        }
                        else{                                                           //Si la contraseña es incorrecta, devuelvo un mensaje de error
                            textFieldPass.error = getString(R.string.error_msg_pass)
                            break
                        }
                    }
                }
                if(!userFound)                                                          //Si no encuentro usuario, devuelvo un mensaje de error
                    textFieldMail.error = getString(R.string.error_msg_usr_1)*/
            }
                else {                                                                       //Si alguno de los campos esta vacion, devuelvo un mensaje de error
                if (inputUser.username.isBlank())
                    textFieldMail.error = getString(R.string.error_msg)
                if (inputUser.password.isBlank())
                    textFieldPass.error = getString(R.string.error_msg)
            }
        }
    }*/

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = prefs.edit()
        editor.putString("first_name", "")
        editor.putString("password", "")
        editor.apply()
    }

    suspend fun dbInit() {
        try {
            val data = productsCollectionRef
                .whereEqualTo("user", "debug")
                .get()
                .await()
            if (data.isEmpty) {
                for (product in PRODUCTS_LIST)
                    productsCollectionRef.add(product).await()
            }
            val data2 = usersCollectionRef
                .whereEqualTo("username", "debug")
                .get()
                .await()
            if (data.isEmpty) {
                usersCollectionRef.add(User("debug", "1234", "debug@gmail.com")).await()
            }
        } catch (e: Exception) {

        }
    }
    suspend fun getUsers(){
        try {
            val data = usersCollectionRef
                .get()
                .await()
            for (user in data)
                users?.add(user.toObject<User>())
        } catch (e:Exception){

        }
    }
    suspend fun validateInput():Boolean{
        inputUser = User(textFieldPass.editText!!.text.toString(),textFieldMail.editText!!.text.toString())
        textFieldMail.error = null
        textFieldPass.error =null
        return inputUser.email.isNotBlank() && inputUser.password.isNotBlank()
    }
    suspend fun signInUser(){
        val result = mAuth.signInWithEmailAndPassword(inputUser.email,inputUser.password).await()
        result.user.let {
            val action_2 =
                LoginFragmentDirections.actionLoginFragmentToWelcomeFragment(
                    it!!.uid
                )
            v.findNavController().navigate(action_2)
        }
    }
}