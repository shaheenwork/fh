package com.shn.fh

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.shn.fh.databinding.ActivityMainBinding
import com.shn.fh.utils.Utils


class MainActivity : AppCompatActivity() {

    private val locationReqId: Int = 1
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        // ask for location permission
        if (!Utils.checkIfAccessFineLocationGranted(this)) {
            Utils.requestLocationPermission(
                this, locationReqId
            )
        } else {

            setupSpinner()

            Toast.makeText(this, Utils.getCityName(10.79185, 76.19365, this), Toast.LENGTH_LONG)
                .show()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationReqId -> {
                if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Utils.getLatLong(this@MainActivity) { latitude, longitude ->
                        Toast.makeText(
                            this@MainActivity,
                            "loc: $latitude:: $longitude",
                            Toast.LENGTH_LONG
                        ).show()

                        Toast.makeText(
                            this@MainActivity,
                            Utils.getCityName(latitude, longitude, this@MainActivity),
                            Toast.LENGTH_LONG
                        ).show()

                    }


                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_location_permission_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupSpinner() {
        // Set up spinner
        val items = arrayOf("Item 1", "Item 2", "Item 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        binding.placeSpinner.adapter = adapter

        // Handle spinner item selection
        binding.placeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = items[position]
                Toast.makeText(this@MainActivity, "Selected: $selectedItem", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected if needed
            }
        }
    }
}
