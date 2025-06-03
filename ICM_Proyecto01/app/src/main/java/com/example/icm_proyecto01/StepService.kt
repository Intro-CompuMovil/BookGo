package com.example.icm_proyecto01

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class StepService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialStepCount: Int = -1
    private var totalStepsSaved: Int = 0

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.e("StepService", "Sensor de pasos no disponible")
            stopSelf()
        } else {
            val sharedPref = getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
            totalStepsSaved = sharedPref.getInt("steps", 0)
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val currentSteps = event.values[0].toInt()

            if (initialStepCount == -1) {
                initialStepCount = currentSteps
                return
            }

            val stepsSinceStart = currentSteps - initialStepCount
            val updatedTotalSteps = totalStepsSaved + stepsSinceStart

            Log.d("StepService", "Pasos detectados: $stepsSinceStart, Total acumulado: $updatedTotalSteps")

            // Guardar localmente
            val sharedPref = getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
            sharedPref.edit().putInt("steps", updatedTotalSteps).apply()

            // Enviar a Firebase
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
                userRef.child("readerLvl").setValue(updatedTotalSteps)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Pasos actualizados en Firebase: $updatedTotalSteps")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Error al actualizar pasos en Firebase", it)
                    }
            }

            totalStepsSaved = updatedTotalSteps
            initialStepCount = currentSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
