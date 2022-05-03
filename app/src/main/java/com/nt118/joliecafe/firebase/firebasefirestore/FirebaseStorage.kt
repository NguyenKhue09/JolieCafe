package com.nt118.joliecafe.firebase.firebasefirestore

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.storage.FirebaseStorage
import com.nt118.joliecafe.ui.fragments.profile_bottom_sheet.ProfileBottomSheetFragment

class FirebaseStorage {

    private val mFireStorage = FirebaseStorage.getInstance()

    fun uploadFile(file: Uri, fileName: String, profileBottomSheetFragment: ProfileBottomSheetFragment, root: String) {
        val ref = mFireStorage.reference
            .child("$root/$fileName")
        val uploadTask = ref.putFile(file)

        uploadTask.addOnPausedListener {
            println("Upload image paused")
        }.addOnFailureListener {
            Toast.makeText(profileBottomSheetFragment.requireContext(), "Upload image failed", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { _ ->
            Toast.makeText(profileBottomSheetFragment.requireContext(), "Upload image successfully", Toast.LENGTH_LONG).show()
            ref.downloadUrl.addOnSuccessListener {
                profileBottomSheetFragment.updateUserData(thumbnail = it.toString())
            }.addOnFailureListener {
                println(it.message)
            }
        }
    }

    fun chooseFile(ActivityResultLauncher: ActivityResultLauncher<Intent>) {
        var getFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        getFileIntent = Intent.createChooser(getFileIntent, "Select picture")
        ActivityResultLauncher.launch(getFileIntent)
    }
}