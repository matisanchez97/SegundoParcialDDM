package com.utn.primerparcial.fragments

import android.app.Activity
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
import com.utn.primerparcial.R

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
    var selectedProductId: Int = 0
    var currentUserId: Int = 0
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
        selectedProductId = ContainerProductFragmentArgs.fromBundle(requireArguments()).selectedProductId
        currentUserId = ContainerProductFragmentArgs.fromBundle(requireArguments()).currentUserId
        editor.putInt("CURRENT_USER_ID",currentUserId)
        editor.putInt("SELECTED_PRODUCT_ID",selectedProductId)
        editor.apply()
        viewPager.adapter = ViewPageAdapter(requireActivity(),selectedProductId)
        TabLayoutMediator(tabLayout, viewPager, {tab, position ->
            when (position) {
                0 -> tab.text = "Detail"
                1 -> tab.text = "Similar"
                2 -> tab.text = "Brand"
                else -> tab.text = "undefined"
            }
        }).attach()
    }

    class ViewPageAdapter(fragmanetActivity: FragmentActivity,selectedProductId: Int) : FragmentStateAdapter(fragmanetActivity) {
        var productId = selectedProductId
        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> DetailProductFragment()
                1 -> SimilarProductFragment()
                2 -> BrandProductFragment()
                else -> DetailProductFragment()
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