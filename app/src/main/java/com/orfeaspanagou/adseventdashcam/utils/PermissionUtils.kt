import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings


object PermissionUtils {

    const val PERMISSION_REQUEST_CODE = 1001

    fun checkAndRequestPermissions(activity: ComponentActivity) {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissionsNeeded.isNotEmpty()) {
            if (permissionsNeeded.any { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }) {
                // Show a rationale dialog if needed
                showPermissionSettings(activity)
            } else {
                // Request the permissions
                ActivityCompat.requestPermissions(
                    activity,
                    permissionsNeeded.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            (activity as? PermissionListener)?.onPermissionsGranted()
        }
    }

    private fun showPermissionRationale(activity: ComponentActivity, permissions: List<String>) {
        AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage("This app requires camera, audio recording, and location permissions to function correctly. Please grant these permissions.")
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    activity,
                    permissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                (activity as? PermissionListener)?.onPermissionsDenied()
            }
            .create()
            .show()
    }

    private fun showPermissionSettings(activity: ComponentActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage("The app needs certain permissions to function correctly. Please enable them in the app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                (activity as? PermissionListener)?.onPermissionsDenied()
            }
            .create()
            .show()
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        activity: ComponentActivity
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                (activity as? PermissionListener)?.onPermissionsGranted()
            } else {
                if (permissions.any { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }) {
                    // User denied permission and checked "Don't ask again"
                    showPermissionSettings(activity)
                } else {
                    // User denied permission but did not check "Don't ask again"
                    (activity as? PermissionListener)?.onPermissionsDenied()
                }
            }
        }
    }

    interface PermissionListener {
        fun onPermissionsGranted()
        fun onPermissionsDenied()
    }
}
