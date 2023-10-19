package com.shn.fh

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shn.fh.utils.Utils

class MainActivity : AppCompatActivity() {

    private val locationReqId: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ask for location permission
        if (!Utils.checkIfAccessFineLocationGranted(this)) {
            Utils.requestLocationPermission(
                this, locationReqId
            )
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
                        Toast.makeText(this@MainActivity, "loc: $latitude:: $longitude",Toast.LENGTH_LONG).show()

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
}