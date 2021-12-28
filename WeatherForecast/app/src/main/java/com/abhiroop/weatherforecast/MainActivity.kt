package com.abhiroop.weatherforecast

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.abhiroop.weatherforecast.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var volleyQueue: RequestQueue

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        volleyQueue = Volley.newRequestQueue(this)

        getCurrLocWeather()

        binding.findWeatherButton.setOnClickListener{updateWeather()}
        binding.weatherLocationInputField.setOnEditorActionListener{ _, actionId, _ ->
            if(actionId ==  EditorInfo.IME_ACTION_DONE){

                // hide soft keyboard programmatically on enter key press
                hideSoftKeyboard()

                // optionally clear focus and hide cursor from edit text
                binding.weatherLocationInputField.clearFocus()
                binding.weatherLocationInputField.isCursorVisible = false

                // optional, set focus on root constraint layout
                binding.rootConstraintLayout.isFocusable = true

                updateWeather()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCurrLocWeather(){

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "Location Permission not granted", Toast.LENGTH_LONG)
                .show()
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                val query = "${location?.latitude}, ${location?.longitude}"
                makeJSONRequest(generateURL(query))
            }


    }

    private fun generateURL(location: String): String{
        return String.format(getString(R.string.url), location)
    }

    private fun makeJSONRequest(url: String){
        val jsonRequest=  JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                if(response.has("error"))
                    Toast.makeText(applicationContext, "Error: location not found", Toast.LENGTH_SHORT).show()
                else
                    updateWeatherLayout(response)
            },
            {
                Toast.makeText(applicationContext, "Error: No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the RequestQueue.
        volleyQueue.add(jsonRequest)
    }

    private fun updateWeather() {
        if (binding.weatherLocationInputField.text.toString() == "")
            return

        val url = generateURL(binding.weatherLocationInputField.text.toString().lowercase())
        binding.weatherLocationInputField.text?.clear()
        makeJSONRequest(url)
    }

    private fun updateWeatherLayout(response: JSONObject?) {
        val locationName = response?.getJSONObject("location")?.getString("name")
        binding.location.text = locationName
        val tempC = response?.getJSONObject("current")?.getString("temp_c")?.toDouble()?.roundToInt()
        binding.degreeNumber.text =  String.format(getString(R.string.weather_format), tempC)
        binding.weatherDetail.text = response?.getJSONObject("current")?.getJSONObject("condition")?.getString("text")
    }

    // extension function to hide soft keyboard programmatically
    private fun Activity.hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}