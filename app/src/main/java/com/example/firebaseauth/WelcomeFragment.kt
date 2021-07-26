package com.example.firebaseauth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.firebaseauth.databinding.FragmentWelcomeBinding
import kotlin.concurrent.fixedRateTimer

private const val ARG_PARAM1 = "formattedFullNumber"
private const val ARG_PARAM2 = "param2"

class WelcomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var formattedFullNumber: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            formattedFullNumber = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentWelcomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(layoutInflater)

        binding.txt2.text = "(${formattedFullNumber?.substring(0,7)}) ${formattedFullNumber?.substring(8,11)}-${formattedFullNumber?.substring(12,14)}-${formattedFullNumber?.substring(15)}"

//        val manager: FragmentManager = activity?.supportFragmentManager!!
//        val trans: FragmentTransaction = manager.beginTransaction()
//        trans.remove(manager.)
//        trans.commit()
//        manager.popBackStack()

        return binding.root
    }

}