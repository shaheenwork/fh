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

        fun getTimeAgo(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - timestamp

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24
            val weeksInMilli = daysInMilli * 7

            val weeks = timeDifference / weeksInMilli
            val days = timeDifference % weeksInMilli / daysInMilli
            val hours = timeDifference % daysInMilli / hoursInMilli
            val minutes = timeDifference % hoursInMilli / minutesInMilli
            val seconds = timeDifference % minutesInMilli / secondsInMilli

            return when {
                weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
                else -> "$seconds sec${if (seconds > 1) "s" else ""} ago"
            }
        }


    }

}


