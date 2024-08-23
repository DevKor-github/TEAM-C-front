package com.devkor.kodaero

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationHelper(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    fun checkAndRequestLocationPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 이미 부여된 경우
            onPermissionGranted()
            Log.e("LocationHelper", "Permission granted")
        } else {
            onPermissionDenied()
            Log.e("LocationHelper", "Permission not granted")
        }
    }

    fun requestLocationPermissionAgain() {
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle("위치 권한 필요")
            .setMessage("해당 기능은 위치 권한이 필요합니다.\n위치 권한을 허용해 주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                // 설정 화면으로 이동
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
        onGpsNotEnabled: () -> Unit,
        onFailure: () -> Unit
    ) {
        val locationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isGpsEnabled) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                } else {
                    onFailure()
                }
            }.addOnFailureListener {
                onFailure()
            }
        } else {
            onGpsNotEnabled()
        }
    }

    fun promptEnableGps() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("GPS 활성화 필요")  // 제목 추가
            .setMessage("해당 기능을 사용하려면 GPS를 활성화해야 합니다.")
            .setCancelable(false)
            .setPositiveButton("GPS 켜기") { _, _ ->
                // GPS 설정 화면으로 이동
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}