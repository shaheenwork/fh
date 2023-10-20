package com.shn.fh.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.shn.fh.R
import java.util.*


class Utils {

    companion object {

        fun requestLocationPermission(activity: AppCompatActivity, requestId: Int) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestId
            )
        }

        fun checkIfAccessFineLocationGranted(context: Context): Boolean {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_location_permission_required),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

         fun getLatLong(context: Context, callback: (Double, Double) -> Unit) {
            if (checkIfAccessFineLocationGranted(context)) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            callback(latitude, longitude)
                        } else {
                            // Location is null, handle the case
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                    }
            }
        }

        fun getCityName(latitude:Double,longitude:Double,context: Context):String{
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            val cityName: String = addresses!![0].locality

            return  cityName;
        }


    }

}


