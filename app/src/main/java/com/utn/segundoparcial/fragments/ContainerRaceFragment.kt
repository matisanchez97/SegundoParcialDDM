package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.utn.segundoparcial.MainActivity
import com.utn.segundoparcial.R

/**
 * A simple [Fragment] subclass.
 * Use the [ContainerRaceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContainerRaceFragment : Fragment() {


    lateinit var v:View
    lateinit var viewPager: ViewPager2
    lateinit var tabLayout: TabLayout
    private val PREF_NAME = "myPreferences"
    var selectedRaceId: Int = 0
    var currentUserId: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_container_race, container, false)
        viewPager = v.findViewById(R.id.viewPager)
        tabLayout = v.findViewById(R.id.tabLayout)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPref: SharedPreferences= requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val isUpdating = sharedPref.getBoolean("IS_UPDATING",true)
        if (isUpdating)
            return super.onOptionsItemSelected(item)
        else {
            return when (item.itemId) {
                android.R.id.home -> {
                    val action =
                        ContainerRaceFragmentDirections.actionContainerProductFragmentToShoppinglistFragment(
                            currentUserId
                        )
                    findNavController().navigate(action)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)

    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences= requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        selectedRaceId = ContainerRaceFragmentArgs.fromBundle(requireArguments()).selectedRaceId
        currentUserId = ContainerRaceFragmentArgs.fromBundle(requireArguments()).currentUserId
        editor.putString("CURRENT_USER_ID",currentUserId)
        editor.putInt("SELECTED_RACE_ID",selectedRaceId)
        editor.apply()
        viewPager.adapter = ViewPageAdapter(requireActivity())
        TabLayoutMediator(tabLayout, viewPager, {tab, position ->
            when (position) {
                0 -> tab.text = "Selected Race"
                1 -> tab.text = "Race Details"
                2 -> tab.text = "Brand"
                else -> tab.text = "undefined"
            }
        }).attach()
    }

    class ViewPageAdapter(fragmanetActivity: FragmentActivity) : FragmentStateAdapter(fragmanetActivity) {
        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> SelectedRaceFragment()
                1 -> RacesDetailsFragment()
                2 -> SelectedRaceFragment()
                else -> SelectedRaceFragment()
            }
        }

        override fun getItemCount(): Int {
            return TAB_COUNT
        }

        companion object{
            private const val TAB_COUNT = 3
        }
    }
}