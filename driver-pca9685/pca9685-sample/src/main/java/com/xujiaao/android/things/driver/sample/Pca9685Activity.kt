package com.xujiaao.android.things.driver.sample

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.xujiaao.android.things.driver.pca9685.Pca9685
import java.io.IOException

private const val MIN_PULSE_DURATION_MS = 1.0
private const val MAX_PULSE_DURATION_MS = 2.0

private const val MIN_ANGLE_DEG = 0.0
private const val MAX_ANGLE_DEG = 180.0

private const val STEP_ANGLE = 30
private const val STEP_DELAY_MS = 5000L // 5 seconds

class Pca9685Activity : Activity() {

    private val mServoSpec0 by lazy {
        Pca9685.ServoSpec(0)
                .setPulseDurationRange(MIN_PULSE_DURATION_MS, MAX_PULSE_DURATION_MS)
                .setAngleRange(MIN_ANGLE_DEG, MAX_ANGLE_DEG)
                .setAngle(90.0)
    }

    private val mServoSpec1 by lazy {
        Pca9685.ServoSpec(1)
                .setPulseDurationRange(MIN_PULSE_DURATION_MS, MAX_PULSE_DURATION_MS)
                .setAngleRange(MIN_ANGLE_DEG, MAX_ANGLE_DEG)
                .setAngle(90.0)
    }

    private lateinit var mHandler: Handler

    private var mPca9685: Pca9685? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            mPca9685 = Pca9685(getI2CPort())
        } catch (e: IOException) {
            Log.e(TAG, "Error creating Servo", e)

            return
        }

        mHandler = Handler()
        mHandler.post(mMoveServoRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()

        mHandler.removeCallbacks(mMoveServoRunnable)

        mPca9685?.apply {
            try {
                close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing Servo")
            } finally {
                mPca9685 = null
            }
        }
    }

    private val mMoveServoRunnable = object : Runnable {

        override fun run() {
            mPca9685?.also {
                try {
                    it.setPwmSpec(mServoSpec0)
                    it.setPwmSpec(mServoSpec1)
                } catch (e: IOException) {
                    Log.e(TAG, "Error setting Servo angle")

                    return
                }

                mServoSpec0.incrementAngle()
                mServoSpec1.incrementAngle()

                mHandler.postDelayed(this, STEP_DELAY_MS)
            }
        }

        private fun Pca9685.ServoSpec.incrementAngle() {
            angle = with(angle + STEP_ANGLE) {
                if (this > maximumAngle) {
                    minimumAngle
                } else {
                    this
                }
            }
        }
    }
}