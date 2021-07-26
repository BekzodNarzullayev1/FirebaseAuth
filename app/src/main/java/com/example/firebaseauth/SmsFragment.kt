package com.example.firebaseauth

import android.content.Context.INPUT_METHOD_SERVICE
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.example.firebaseauth.databinding.FragmentSmsBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "formattedFullNumber"
private const val ARG_PARAM2 = "fullNumberWithPlus"

class SmsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var formattedFullNumber: String? = null
    private var fullNumberWithPlus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            formattedFullNumber = it.getString(ARG_PARAM1)
            fullNumberWithPlus = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentSmsBinding
    lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId:String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private val TAG = "SmsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=  FragmentSmsBinding.inflate(layoutInflater)


        binding.refresh.isEnabled = false
        binding.tvRefresh.isEnabled = false

        auth = FirebaseAuth.getInstance()
        sendCode(fullNumberWithPlus!!)
        binding.txt2.text= "Bir martalik kod  (${formattedFullNumber?.substring(0, 7)}) ${formattedFullNumber?.substring(
            8,
            11  
        )}-**-** \n" +
                "raqamiga yuborildi"

        val countDownTimer = object :CountDownTimer(60000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished/1000>9){
                    binding.countDownTimer.text = "00:${millisUntilFinished/1000}"
                }else{
                    binding.countDownTimer.text = "00:0${millisUntilFinished/1000}"
                }

            }

            override fun onFinish() {
                binding.countDownTimer.text = "00:00"
                binding.refresh.isEnabled = true
                binding.tvRefresh.isEnabled = true
            }

        }

        countDownTimer.start()

        binding.refresh.setOnClickListener {
            sendCode(fullNumberWithPlus!!)
            countDownTimer.start()
            binding.refresh.isEnabled = false
            binding.tvRefresh.isEnabled = false
        }
        binding.tvRefresh.setOnClickListener {
            sendCode(fullNumberWithPlus!!)
            countDownTimer.start()
            binding.refresh.isEnabled = false
            binding.tvRefresh.isEnabled = false
        }

        binding.etSms.addTextChangedListener {
            verifyCode()
            countDownTimer.onFinish()
        }

//        binding.etSms.setOnEditorActionListener { v, actionId, event ->
//            if (actionId==EditorInfo.IME_ACTION_DONE){
//                verifyCode()
//                val view = currentFocus
//                if (view!=null){
//                    val imm :InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(view.windowToken,0)
//                }
//            }
//            true
//        }
        return binding.root
    }

    private fun sendCode(phoneNumber: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun verifyCode(){
        val code = binding.etSms.text.toString()
        if (code.length==6){
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            //signInWithPhoneAuthCredential(credential)
            val bundle = Bundle()
            bundle.putString("formattedFullNumber", formattedFullNumber)
            findNavController().navigate(R.id.welcomeFragment, bundle)
            binding.etSms.text.clear()

        }
    }

    private fun reSendCode(phoneNumber: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}