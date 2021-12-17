package com.abhiroop.rupeedollarcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abhiroop.rupeedollarcalculator.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private var oneDollarValue = 70.0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        getOneDollarValueByAPICall()
        setContentView(binding.root)

        binding.calculateButton.setOnClickListener{ convert() }
    }

    private fun getOneDollarValueByAPICall() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://free-currency-converter.herokuapp.com/list/convert?source=USD&destination=INR"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                //parse the response using Gson
                binding.APIRequest.text = response
                extractCurrencyValueFromResponse()
            },
            { print("error") }
        )

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

    }

    private fun extractCurrencyValueFromResponse() {
        val response = binding.APIRequest.text.toString()
        binding.APIRequest.text = ""

        data class APIModel(
            val success: Boolean,
            val message: String,
            val source: String,
            val destination: String,
            val price: Int,
            val converted_value: Double
        )
        val gson = Gson()
        val apiCall:APIModel = gson.fromJson(response, APIModel::class.java)
        oneDollarValue = apiCall.converted_value
    }

    private fun convert(){
        val value = binding.enterAmount.text.toString().toDoubleOrNull()

        if(value == null || value == 0.0){
            displayResult(0.0)
            return
        }

        val convertedResult = when(binding.currencySelect.checkedRadioButtonId){
            binding.toRupee.id -> value * oneDollarValue
            else -> value / oneDollarValue
        }

        displayResult(convertedResult)

    }

    private fun displayResult(result: Double){
        if(result == 0.0){
            binding.result.text = ""
            return
        }

        val formattedResult =  when(binding.currencySelect.checkedRadioButtonId){
            binding.toRupee.id -> NumberFormat.getCurrencyInstance(Locale("en", "in")).format(result)
            else -> NumberFormat.getCurrencyInstance().format(result)
        }

        binding.result.text = getString(R.string.result,formattedResult)
    }
}