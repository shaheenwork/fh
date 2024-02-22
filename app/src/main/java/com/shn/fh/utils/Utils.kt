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
import com.shn.fh.notifications.model.GroupedNotification
import com.shn.fh.notifications.model.Notification
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
            return if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_location_permission_required),
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }

         fun getLatLong(context: Context, callback: (Double, Double) -> Unit) {
            if (checkIfAccessFineLocationGranted(context)) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
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
        fun getActionForNotification(act:Int):String{
            var action:String=""
            when (act) {
                Consts.ACTION_LIKE -> {
                    action = "liked your post"
                }
                Consts.ACTION_COMMENT -> {
                    action="commented on your post"
                }
                Consts.ACTION_FOLLOW -> {
                    action = "followed you"
                }
            }
            return  action
        }

        fun getNotificationText(notification: GroupedNotification): String {
            val actionText = getActionForNotification(notification.action)
            return when {
                notification.userIds.size > 2 -> {
                    "${notification.users.last().userName} and ${notification.userIds.size - 1} others $actionText"
                }
                notification.userIds.size == 1 -> {
                    "${notification.users.first().userName} $actionText"
                }
                else -> {
                    "${notification.users[0].userName} and ${notification.users[1].userName} $actionText"
                }
            }
        }



        fun getTimeAgo(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - (timestamp)

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
                weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""}"
                days > 0 -> "$days day${if (days > 1) "s" else ""}"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
                minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""}"
                else -> "$seconds sec${if (seconds > 1) "s" else ""}"
            }
        }



    }

}


