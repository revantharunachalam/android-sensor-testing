package com.example.sensor_testing

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorDataManager(context: Context) : SensorEventListener {

    interface Listener {
        fun onSensorDataUpdated()
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magnetometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val barometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    val availability = SensorAvailability(
        accelerometer = accelerometerSensor != null,
        gyroscope = gyroscopeSensor != null,
        magnetometer = magnetometerSensor != null,
        barometer = barometerSensor != null
    )

    val accel = Vec3()
    val gyro = Vec3()
    val mag = Vec3()
    var pressure: Float = SensorManager.PRESSURE_STANDARD_ATMOSPHERE
        private set

    @Volatile
    var lastGyroTimestampNs: Long = 0L
        private set

    var listener: Listener? = null

    fun start(rate: Int = SensorManager.SENSOR_DELAY_GAME) {
        accelerometerSensor?.let { sensorManager.registerListener(this, it, rate) }
        gyroscopeSensor?.let { sensorManager.registerListener(this, it, rate) }
        magnetometerSensor?.let { sensorManager.registerListener(this, it, rate) }
        barometerSensor?.let { sensorManager.registerListener(this, it, rate) }
        lastGyroTimestampNs = 0L
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        lastGyroTimestampNs = 0L
    }

    fun getAltitude(): Float {
        return SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accel.set(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyro.set(event.values[0], event.values[1], event.values[2])
                lastGyroTimestampNs = event.timestamp
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                mag.set(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_PRESSURE -> {
                pressure = event.values[0]
            }
        }
        listener?.onSensorDataUpdated()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
