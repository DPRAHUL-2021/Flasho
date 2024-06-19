package com.example.flasho.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.flasho.R
import com.example.flasho.Utils
import com.example.flasho.activity.UsersMainActivity
import com.example.flasho.databinding.FragmentOTPBinding
import com.example.flasho.models.Users
import com.example.flasho.viewmodels.AuthViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(layoutInflater)
        getUserNumber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
        onBackButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Signing you...")
            val editTexts = arrayOf(
                binding.etOTP1,
                binding.etOTP2,
                binding.etOTP3,
                binding.etOTP4,
                binding.etOTP5,
                binding.etOTP6
            )
            val otp = editTexts.joinToString("") { it.text.toString() }
            if (otp.length < editTexts.size) {
                Utils.showToast(requireContext(), "Please Enter Valid OTP")
            } else {
                editTexts.forEach { it.text?.clear();it.clearFocus() }
                verifyOtp(otp)
            }
        }
    }

    private fun verifyOtp(otp: String) {
        viewModel.signInWithPhoneAuthCredential(otp, userNumber, requireContext()) { userId ->
            if (userId != null) {
                val user = Users(uid = userId, userPhoneNumber = userNumber, userAddress = null)
                // Now you can safely save the user data to Firebase
                FirebaseDatabase.getInstance().getReference("Allusers").child("Users").child(userId).setValue(user)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Utils.showToast(requireContext(), "User data saved successfully.")
                            Utils.hideDialog()
                            Utils.showToast(requireContext(), "Logged In")
                            val intent = Intent(requireContext(), UsersMainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Utils.showToast(requireContext(), "Failed to save user data. Please try again.")
                        }
                    }
            } else {
                // Handle the case where authentication failed and userId is null
                Utils.showToast(requireContext(), "Authentication failed. Please try again.")
            }
        }
    }



    private fun sendOTP() {
        Utils.showDialog(requireContext(), "Sending OTP....")

        viewModel.apply {
            sendOTP(userNumber, requireActivity())
            lifecycleScope.launch {
                otpSent.collect { otpSent ->
                    if (otpSent) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Otp Sent...")
                    }
                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbotpFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_OTPFragment_to_signinFragment)
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(
            binding.etOTP1,
            binding.etOTP2,
            binding.etOTP3,
            binding.etOTP4,
            binding.etOTP5,
            binding.etOTP6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Not needed for your implementation
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Not needed for your implementation
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
            })
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()
        binding.tvUserNumber.text = userNumber
    }
}