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
import com.utn.segundoparcial.R
import com.utn.segundoparcial.constants.PRODUCTS_LIST
import com.utn.segundoparcial.database.appDatabase
import com.utn.segundoparcial.database.productDao
import com.utn.segundoparcial.database.userDao
import com.utn.segundoparcial.entities.User
import com.wajahatkarim3.roomexplorer.RoomExplorer


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    lateinit var v: View
    lateinit var textFieldUsr: TextInputLayout
    lateinit var textFieldPass: TextInputLayout
    lateinit var butRegister: Button
    lateinit var butLogin: Button
    lateinit var inputUser: User
    lateinit var loginLayout: ConstraintLayout
    lateinit var user: User
    private var db: appDatabase? = null
    private var userDao: userDao? = null
    private var productDao: productDao? = null
    var users: MutableList<User>? = ArrayList<User>()
    var userFound = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_login, container, false)
        textFieldUsr = v.findViewById(R.id.textFieldUsr)
        textFieldPass = v.findViewById(R.id.textFieldPass)
        butLogin = v.findViewById(R.id.buttonLogin)
        butRegister = v.findViewById(R.id.buttonRegister)
        loginLayout = v.findViewById(R.id.loginLayout)
        return v
    }

    override fun onStart() {
        super.onStart()

        db = appDatabase.getAppDataBase(v.context)
        userDao = db?.userDao()
        productDao = db?.productDao()
        productDao?.insertMultipleProduct(PRODUCTS_LIST.toMutableList())    //Cargo las constantes en la base de datos
        userDao?.insertPerson(User("debug","1234"))     //Genero el usuario de debug
        users = userDao?.loadAllPersons()
        userFound = false

        butRegister.setOnClickListener {
            val action_1 = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            v.findNavController().navigate(action_1)
        }

        butLogin.setOnClickListener(){
            inputUser = User(textFieldUsr.editText!!.text.toString(), textFieldPass.editText!!.text.toString())
            textFieldUsr.error = null
            textFieldPass.error =null
            if(inputUser.username.isNotBlank() && inputUser.password.isNotBlank()){     //Si los dos campos estan completos
                for (user in users!!)  {
                    if(user.checkUsername(inputUser.username)){                         //Chequeo si el usuario de la base de datos tiene ese nombre
                        userFound = true
                        if (user.checkPassword(inputUser.password)){                    //Cuando encuentro uno chqueo si tiene la contraseña ingresada
                            if(inputUser.checkUsername("debug"))              //Si el usuario es el de debug, empiezo el RoomExplorer
                                RoomExplorer.show(context, appDatabase::class.java, "myDB")
                            else{                                                       //Sino voy a la Pantalla Principal
                                Snackbar.make(loginLayout,"Welcome " + user.name.toString(), Snackbar.LENGTH_SHORT).show()
                                val action_2 = LoginFragmentDirections.actionLoginFragmentToWelcomeFragment(user.id)
                                v.findNavController().navigate(action_2)
                            }
                        }
                        else{                                                           //Si la contraseña es incorrecta, devuelvo un mensaje de error
                            textFieldPass.error = getString(R.string.error_msg_pass)
                            break
                        }
                    }
                }
                if(!userFound)                                                          //Si no encuentro usuario, devuelvo un mensaje de error
                    textFieldUsr.error = getString(R.string.error_msg_usr_1)
            }
            else{                                                                       //Si alguno de los campos esta vacion, devuelvo un mensaje de error
                if (inputUser.username.isBlank())
                    textFieldUsr.error = getString(R.string.error_msg)
                if (inputUser.password.isBlank())
                    textFieldPass.error = getString(R.string.error_msg)
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
}