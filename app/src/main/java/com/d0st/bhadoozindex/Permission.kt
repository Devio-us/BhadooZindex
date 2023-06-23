package com.d0st.bhadoozindex

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

typealias SimpleCallback = () -> Unit

object Permission {

    private const val STORAGE_PERMISSION = 124

    fun verifyStoragePermission(activity: Activity, callback: SimpleCallback? = null) {
        checkPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            STORAGE_PERMISSION,
            callback
        )
    }

    fun verifyStoragePermission(fragment: Fragment, callback: SimpleCallback? = null) {
        checkPermission(
            fragment,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            STORAGE_PERMISSION,
            callback
        )
    }

    fun processStoragePermission(
        requestCode: Int,
        grantResults: IntArray,
        callback: SimpleCallback? = null
    ) {
        if (requestCode == STORAGE_PERMISSION && grantResults.isNotEmpty())
            callback?.invoke()
    }

    private fun checkPermission(
        context: Activity,
        permission: String,
        requestCode: Int,
        callback: SimpleCallback? = null
    ) = with(context) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            // Since Android 13, we can't request external storage permission,
            // so don't check it.
            || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
//            Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show()
        } else if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                requestCode
            )
            return@with
        }
        callback?.invoke()
    }

    fun verifyReadStoragePermission(fragment: Fragment, callback: SimpleCallback? = null) {
        checkPermission(
            fragment,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            STORAGE_PERMISSION,
            callback
        )
    }

    private fun checkPermission(
        context: Fragment,
        permission: String,
        requestCode: Int,
        callback: SimpleCallback? = null
    ) = with(context) {
//        if (ContextCompat.checkSelfPermission(
//                this.requireContext(),
//                permission
//            ) != PackageManager.PERMISSION_GRANTED
//            || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
//        ) {
//            Toast.makeText(this.requireContext(), "Storage Permission Denied", Toast.LENGTH_LONG)
//                .show()
//            ActivityCompat.requestPermissions(
//                this.requireActivity(),
//                arrayOf(permission),
//                requestCode
//            )
//            return@with
//        } else
            if (requireContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(permission),
                requestCode
            )
            return@with
        }
        callback?.invoke()
    }

}