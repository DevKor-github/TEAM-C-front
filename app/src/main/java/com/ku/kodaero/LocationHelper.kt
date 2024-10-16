package com.ku.kodaero

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationHelper(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    fun checkAndRequestLocationPermission(
        onPermissionGranted: () -> Unit,
    ) {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            requestLocationPermissionAgain()
        }
    }

    private fun requestLocationPermissionAgain() {
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle("위치 권한 필요")
            .setMessage("해당 기능은 위치 권한이 필요합니다.\n위치 권한을 허용해 주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    fun checkGpsEnabledAndRequestLocation(
        onLocationReceived: (Double, Double) -> Unit,
    ) {
        val locationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isGpsEnabled) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                } else {
                    showFailureToast()
                }
            }.addOnFailureListener {
                showFailureToast()
            }
        } else {
            promptEnableGps()
        }
    }

    private fun showFailureToast() {
        Toast.makeText(activity, "위치를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun promptEnableGps() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("GPS 활성화 필요")
            .setMessage("해당 기능을 사용하려면 GPS를 활성화해야 합니다.")
            .setCancelable(false)
            .setPositiveButton("GPS 켜기") { _, _ ->
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
