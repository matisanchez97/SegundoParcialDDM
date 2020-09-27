package com.utn.primerparcial.fragments

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
import com.utn.primerparcial.R
import com.utn.primerparcial.constants.AREA_CODES
import com.utn.primerparcial.constants.PRODUCTS_LIST
import com.utn.primerparcial.database.appDatabase
import com.utn.primerparcial.database.productDao
import com.utn.primerparcial.database.userDao
import com.utn.primerparcial.entities.User
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
    lateinit var textFieldPass2: TextInputLayout
    lateinit var textFieldName: TextInputLayout
    lateinit var textFieldPhone: TextInputLayout
    lateinit var textFieldDate: TextInputLayout
    lateinit var textFieldArea: TextInputLayout
    lateinit var textEditDate: AutoCompleteTextView
    lateinit var textListArea: AutoCompleteTextView
    lateinit var validationList: MutableList<TextInputLayout>
    lateinit var butRegister2: Button
    lateinit var newUser: User
    lateinit var birthDate: LocalDate
    lateinit var username: String
    lateinit var password: String
    lateinit var firstname: String
    lateinit var phone: String

    private var db: appDatabase? = null
    private var userDao: userDao? = null

    var users: MutableList<User>? = ArrayList<User>()

    var isValid = true
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

    override fun onDateSet(view: DatePicker?, year: Int,month:Int,day:Int) {
        birthDate = LocalDate.of(year,month+1,day)
        textEditDate.setText(birthDate.toString())
    }

    override fun onStart() {
        super.onStart()
        val adapter = ArrayAdapter<String>(requireContext(),R.layout.area_item, AREA_CODES)
        textListArea.setAdapter(adapter)
        db = appDatabase.getAppDataBase(v.context)
        userDao = db?.userDao()
        users = userDao?.loadAllPersons()!!
        if(users!=null)
            i = users!!.size

        butRegister2.setOnClickListener(){
            validationList = arrayListOf(textFieldUsr2,textFieldPass2,textFieldName,textFieldPhone,textFieldDate,textFieldArea)
            for(textField in validationList){
                if (textField.editText!!.text.isBlank())
                {
                    isValid = false
                    textField.error = getString(R.string.error_msg)
                }
            }
            username = textFieldUsr2.editText!!.text.toString()
            password = textFieldPass2.editText!!.text.toString()
            firstname = textFieldName.editText!!.text.toString()
            phone = textFieldArea.editText!!.text.toString() + textFieldPhone.editText!!.text.toString()
            for (user in users!!){
                if(user.checkUsername(username))
                {
                    isValid = false
                    textFieldUsr2.error = getString(R.string.error_msg_usr_2)
                }
            }
            if (isValid){
                newUser = User(i,firstname,phone,birthDate,username,password)
                i++
                userDao?.insertPerson(newUser)
                val action_3 = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                v.findNavController().navigate(action_3)
            }
            else
                isValid = true
        }

        textFieldDate.editText!!.setOnClickListener(){
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(requireContext(),this@RegisterFragment,year,month,day)
            datePickerDialog.show()
        }
    }




}