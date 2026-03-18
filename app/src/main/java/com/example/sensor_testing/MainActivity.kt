package com.example.sensor_testing

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sensor_testing.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorDataManager.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorData: SensorDataManager

    private val uiHandler = Handler(Looper.getMainLooper())
    private var lastGyroNs: Long = 0L
    private var running = false

    private val uiUpdateRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            updateUI()
            uiHandler.postDelayed(this, UI_UPDATE_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorData = SensorDataManager(this)
        sensorData.listener = this

        applyAvailabilityIndicators()

        binding.btnReset.setOnClickListener {
            resetFusion()
            lastGyroNs = 0L
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        sensorData.start()
        uiHandler.post(uiUpdateRunnable)
        binding.fusionStatus.text = getString(R.string.fusion_status_active)
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorData.stop()
        uiHandler.removeCallbacks(uiUpdateRunnable)
        binding.fusionStatus.text = getString(R.string.fusion_status_idle)
    }

    override fun onSensorDataUpdated() {
        // Sensor data is stored in SensorDataManager fields; UI updates on timer
    }

    private fun updateUI() {
        val a = sensorData.accel
        val g = sensorData.gyro
        val m = sensorData.mag

        val currentGyroNs = sensorData.lastGyroTimestampNs
        val dt = if (lastGyroNs != 0L && currentGyroNs > lastGyroNs) {
            (currentGyroNs - lastGyroNs) / 1_000_000_000f
        } else {
            0f
        }
        lastGyroNs = currentGyroNs

        val hasMag = sensorData.availability.magnetometer
        val orientation = updateSensorFusion(
            a.x, a.y, a.z,
            g.x, g.y, g.z,
            m.x, m.y, m.z,
            dt, hasMag
        )

        if (orientation != null && orientation.size == 3) {
            binding.orientationPitch.text = formatDegree(orientation[0])
            binding.orientationRoll.text = formatDegree(orientation[1])
            binding.orientationYaw.text = formatDegree(orientation[2])
        }

        binding.accelX.text = formatValue(a.x)
        binding.accelY.text = formatValue(a.y)
        binding.accelZ.text = formatValue(a.z)

        binding.gyroX.text = formatValue(g.x)
        binding.gyroY.text = formatValue(g.y)
        binding.gyroZ.text = formatValue(g.z)

        binding.magX.text = formatValue(m.x)
        binding.magY.text = formatValue(m.y)
        binding.magZ.text = formatValue(m.z)

        binding.baroPressure.text = String.format(Locale.US, "%.1f hPa", sensorData.pressure)
        binding.baroAltitude.text = String.format(Locale.US, "%.1f m", sensorData.getAltitude())
    }

    private fun applyAvailabilityIndicators() {
        val avail = sensorData.availability
        setStatusColor(binding.accelStatus, avail.accelerometer)
        setStatusColor(binding.gyroStatus, avail.gyroscope)
        setStatusColor(binding.magStatus, avail.magnetometer)
        setStatusColor(binding.baroStatus, avail.barometer)
    }

    private fun setStatusColor(view: android.view.View, available: Boolean) {
        val colorRes = if (available) R.color.sensor_available else R.color.sensor_unavailable
        val bg = view.background
        if (bg is GradientDrawable) {
            bg.setColor(ContextCompat.getColor(this, colorRes))
        }
    }

    private fun formatValue(v: Float): String =
        String.format(Locale.US, "%+.2f", v)

    private fun formatDegree(v: Float): String =
        String.format(Locale.US, "%.1f°", v)

    private external fun updateSensorFusion(
        ax: Float, ay: Float, az: Float,
        gx: Float, gy: Float, gz: Float,
        mx: Float, my: Float, mz: Float,
        dt: Float, hasMag: Boolean
    ): FloatArray?

    private external fun resetFusion()

    external fun stringFromJNI(): String

    companion object {
        private const val UI_UPDATE_INTERVAL_MS = 33L

        init {
            System.loadLibrary("sensor_testing")
        }
    }
}
