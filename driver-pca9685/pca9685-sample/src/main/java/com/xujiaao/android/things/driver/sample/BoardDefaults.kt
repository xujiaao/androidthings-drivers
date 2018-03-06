package com.xujiaao.android.things.driver.sample

import android.os.Build

private const val DEVICE_RPI3 = "rpi3"
private const val DEVICE_IMX6UL_PICO = "imx6ul_pico"
private const val DEVICE_IMX7D_PICO = "imx7d_pico"

/**
 * Return the preferred I2C port for each board.
 */
fun getI2CPort(): String {
    return when (Build.DEVICE) {
        DEVICE_RPI3 -> "I2C1"
        DEVICE_IMX6UL_PICO -> "I2C2"
        DEVICE_IMX7D_PICO -> "I2C1"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }
}