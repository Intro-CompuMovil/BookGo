package com.example.icm_proyecto01.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.icm_proyecto01.R
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth


object ExchangeNotificationManager {

    fun sendNotificationToUser(userId: String, title: String, message: String, context: Context) {
        Log.d("Notificacion", "Notificando a $userId: $title")

        // Obtener token de destino
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("Users").child(userId).child("notificationToken").get()
            .addOnSuccessListener { snapshot ->
                val token = snapshot.value?.toString()
                Log.d("Notificacion", "Token de destino: $token")

                // Mostrar notificación si es el usuario actual
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == userId) {
                    sendNotification(context, title, message)
                }
            }
    }



    fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "book_exchange_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal en Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de Intercambio",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logobookgo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }


    fun startListening(context: Context) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val token = task.result
                    val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    dbRef.child("Users").child(user.uid).child("notificationToken").setValue(token)
                }
            }
    }

}
