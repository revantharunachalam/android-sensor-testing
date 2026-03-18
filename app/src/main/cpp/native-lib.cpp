#include <jni.h>
#include <string>
#include <cmath>

static float g_pitch = 0.0f;
static float g_roll  = 0.0f;
static float g_yaw   = 0.0f;
static bool  g_initialized = false;

static constexpr float ALPHA = 0.98f;
static constexpr float RAD_TO_DEG = 180.0f / M_PI;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_sensor_1testing_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "IMU Sensor Fusion Active";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_sensor_1testing_MainActivity_updateSensorFusion(
        JNIEnv* env,
        jobject /* this */,
        jfloat ax, jfloat ay, jfloat az,
        jfloat gx, jfloat gy, jfloat gz,
        jfloat mx, jfloat my, jfloat mz,
        jfloat dt,
        jboolean hasMag) {

    float accelPitch = atan2f(ay, sqrtf(ax * ax + az * az));
    float accelRoll  = atan2f(-ax, az);

    if (!g_initialized) {
        g_pitch = accelPitch;
        g_roll  = accelRoll;
        g_yaw   = 0.0f;
        g_initialized = true;
    } else if (dt > 0.0f && dt < 1.0f) {
        g_pitch = ALPHA * (g_pitch + gx * dt) + (1.0f - ALPHA) * accelPitch;
        g_roll  = ALPHA * (g_roll  + gy * dt) + (1.0f - ALPHA) * accelRoll;
    }

    if (hasMag) {
        float cosPitch = cosf(g_pitch);
        float sinPitch = sinf(g_pitch);
        float cosRoll  = cosf(g_roll);
        float sinRoll  = sinf(g_roll);

        float magXh = mx * cosPitch + mz * sinPitch;
        float magYh = mx * sinRoll * sinPitch + my * cosRoll - mz * sinRoll * cosPitch;
        g_yaw = atan2f(-magYh, magXh);
    } else {
        if (dt > 0.0f && dt < 1.0f) {
            g_yaw += gz * dt;
        }
    }

    jfloatArray result = env->NewFloatArray(3);
    if (result != nullptr) {
        float orientation[3];
        orientation[0] = g_pitch * RAD_TO_DEG;
        orientation[1] = g_roll  * RAD_TO_DEG;
        orientation[2] = g_yaw   * RAD_TO_DEG;
        env->SetFloatArrayRegion(result, 0, 3, orientation);
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_sensor_1testing_MainActivity_resetFusion(
        JNIEnv* env,
        jobject /* this */) {
    g_pitch = 0.0f;
    g_roll  = 0.0f;
    g_yaw   = 0.0f;
    g_initialized = false;
}
