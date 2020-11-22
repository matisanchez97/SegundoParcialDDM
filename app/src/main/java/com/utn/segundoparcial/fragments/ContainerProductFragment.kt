package com.utn.segundoparcial.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.utn.segundoparcial.R

/**
 * A simple [Fragment] subclass.
 * Use the [ContainerProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContainerProductFragment : Fragment() {


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
        v = inflater.inflate(R.layout.fragment_container_product, container, false)
        viewPager = v.findViewById(R.id.viewPager)
        tabLayout = v.findViewById(R.id.tabLayout)
        return v
    }

    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences= requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        selectedRaceId = ContainerProductFragmentArgs.fromBundle(requireArguments()).selectedRaceId
        currentUserId = ContainerProductFragmentArgs.fromBundle(requireArguments()).currentUserId
        editor.putString("CURRENT_USER_ID",currentUserId)
        editor.putInt("SELECTED_RACE_ID",selectedRaceId)
        editor.apply()
        viewPager.adapter = ViewPageAdapter(requireActivity())
        TabLayoutMediator(tabLayout, viewPager, {tab, position ->
            when (position) {
                0 -> tab.text = "Selected Race"
                1 -> tab.text = "Similar"
                2 -> tab.text = "Brand"
                else -> tab.text = "undefined"
            }
        }).attach()
    }

    class ViewPageAdapter(fragmanetActivity: FragmentActivity) : FragmentStateAdapter(fragmanetActivity) {
        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> SelectedRaceFragment()
                1 -> SelectedRaceFragment()
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