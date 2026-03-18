package com.example.sensor_testing

data class Vec3(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun copyFrom(other: Vec3) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
    }
}

data class SensorAvailability(
    val accelerometer: Boolean = false,
    val gyroscope: Boolean = false,
    val magnetometer: Boolean = false,
    val barometer: Boolean = false
)

data class IMUState(
    val accelerometer: Vec3 = Vec3(),
    val gyroscope: Vec3 = Vec3(),
    val magnetometer: Vec3 = Vec3(),
    val pressure: Float = 0f,
    val altitude: Float = 0f,
    val orientation: Vec3 = Vec3(),
    val availability: SensorAvailability = SensorAvailability()
)
