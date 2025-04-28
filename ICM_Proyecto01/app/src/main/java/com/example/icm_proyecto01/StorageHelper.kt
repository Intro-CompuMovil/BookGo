package com.example.icm_proyecto01.helpers

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object StorageHelper {

    private val storage = FirebaseStorage.getInstance()

    fun uploadProfileImage(userId: String, imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val ref = storage.reference.child("profileImages/$userId.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
