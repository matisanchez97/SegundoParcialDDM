package com.utn.segundoparcial.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.utn.segundoparcial.R
import com.utn.segundoparcial.constants.AREA_CODES
import com.utn.segundoparcial.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment(),DatePickerDialog.OnDateSetListener {
    lateinit var v: View
    lateinit var textFieldUsr2: TextInputLayout
    lateinit var textFieldMail2: TextInputLayout
    lateinit var textFieldPass2: TextInputLayout
    lateinit var textFieldName: TextInputLayout
    lateinit var textFieldPhone: TextInputLayout
    lateinit var textFieldDate: TextInputLayout
    lateinit var textFieldArea: TextInputLayout
    lateinit var textEditDate: AutoCompleteTextView     //AutoCompleteView para que sea como un textView, pero con la misma aperiencia del resto d elos campos
    lateinit var textListArea: AutoCompleteTextView
    lateinit var validationList: MutableList<TextInputLayout>
    lateinit var butRegister2: Button
    lateinit var newUser: User
    lateinit var birthDate: LocalDate
    lateinit var username: String
    lateinit var email: String
    lateinit var password: String
    lateinit var firstname: String
    lateinit var phone: String
    private lateinit var mAuth: FirebaseAuth

    val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + parentJob)
    val db = Firebase.firestore
    val usersCollectionRef = db.collection("users")


    var users: MutableList<User>? = ArrayList<User>()

    var day = 0
    var month = 0
    var year = 0
    var i = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_register, container, false)
        textFieldUsr2 = v.findViewById(R.id.textFieldUsr_2)
        textFieldMail2 = v.findViewById(R.id.textFieldMail_2)
        textFieldPass2 = v.findViewById(R.id.textFieldPass_2)
        textFieldName = v.findViewById(R.id.textFieldName)
        textFieldPhone = v.findViewById(R.id.textFieldPhone)
        textFieldDate = v.findViewById(R.id.textFieldDate)
        textEditDate = v.findViewById(R.id.textEditDate)
        textFieldArea = v.findViewById(R.id.textFieldArea)
        textListArea = v.findViewById(R.id.textEditListArea)
        butRegister2 = v.findViewById(R.id.buttonRegister_2)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onDateSet(view: DatePicker?, year: Int,month:Int,day:Int) {        //Cuando se setea la fecha del datepicker, creo una variable del tipo localdate
        birthDate = LocalDate.of(year,month+1,day)                         //Y la cargo en el View
        textEditDate.setText(birthDate.toString())
    }

    override fun onStart() {
        super.onStart()
        val adapter = ArrayAdapter<String>(requireContext(),R.layout.area_item, AREA_CODES)     //Creo un adaptador para el AutoCompleteTextView del las areas
        textListArea.setAdapter(adapter)
        usersCollectionRef
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot!= null)
                    for(user in snapshot){
                        users?.add(user.toObject())
                    }
            }
        if(users!=null)
            i = users!!.size

        butRegister2.setOnClickListener() {
            scope.launch {
                if (validateInput()) {
                    createNewUser()
                }
            }
        }


        textFieldDate.editText!!.setOnClickListener(){          //Si hago click en el campo birthday, primero cargo el dia de hoy en variables
            val calendar: Calendar = Calendar.getInstance()     //Y luego ejecuto el dialog del date picker
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(requireContext(),this@RegisterFragment,year,month,day)
            datePickerDialog.show()
        }
    }
    suspend fun createNewUser(){
        val result = mAuth.createUserWithEmailAndPassword(email,password).await()
        result?.user.let {
            newUser = User(it!!.uid,firstname,phone,birthDate.toEpochDay(),username,password, email)
            usersCollectionRef.add(newUser).await()
            val action_3 = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            v.findNavController().navigate(action_3)
        }
    }
    suspend fun validateInput():Boolean{
        var isValid = true
        validationList = arrayListOf(textFieldUsr2,textFieldPass2,textFieldName,textFieldPhone,textFieldDate,textFieldArea,textFieldMail2)
        for(textField in validationList){               //Creo una lista text inputlayout, para verificar que esten todas completas
            if (textField.editText!!.text.isBlank())    //Si no lo estan envio un mensaje de error
            {
                isValid = false
                textField.error = getString(R.string.error_msg)
            }
        }
        email = textFieldMail2.editText!!.text.toString()
        username = textFieldUsr2.editText!!.text.toString()
        password = textFieldPass2.editText!!.text.toString()
        firstname = textFieldName.editText!!.text.toString()
        phone = textFieldArea.editText!!.text.toString() + textFieldPhone.editText!!.text.toString()
        for (user in users!!){                          //Chequeo si el usuario ya existe, para que no se creen dos usuarios iguales
            if(user.checkEmail(email))
            {
                isValid = false
                textFieldMail2.error = getString(R.string.error_msg_usr_2)
            }
        }
        if(password.length<6){
            isValid = false
            textFieldPass2.error = getString(R.string.error_msg_pass_2)
        }
        return isValid
    }




}