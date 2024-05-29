package com.homemedics.app.twilio.call

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ProximitySensorUtils(var context: Context) {
    companion object{
        const val STATE_NEAR = "stateNear"
        const val STATE_FAR = "stateFar"
    }

    private lateinit var callback: (String) -> Unit

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private var proximitySensor: Sensor? = null

    fun setListener(callback: ((String)->Unit)? = null){
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if(callback != null){
            this.callback = callback

            if(proximitySensor != null){
                sensorManager.registerListener(
                    sensorListener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
        else sensorManager.unregisterListener(sensorListener)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if(it.sensor.type == Sensor.TYPE_PROXIMITY){
                    if(it.values[0] < 0.5){
                        callback.invoke(STATE_NEAR)
                    }
                    else{
                        callback.invoke(STATE_FAR)
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }
}