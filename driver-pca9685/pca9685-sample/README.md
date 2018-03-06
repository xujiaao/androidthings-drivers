# PCA9685 sample for Android Things

This sample demonstrates how to control servos using [PCA9685](https://www.adafruit.com/product/815) with Android Things.


## Pre-requisites

- Android Things compatible board

- Android Studio 2.3+

- 1 [PCA9685](https://www.adafruit.com/product/815)

- 2 [servos](https://www.adafruit.com/product/169)

- jumper wires

- 1 breadboard

<!--
## Schematics

![Schematics for Raspberry Pi 3](rpi3_schematics.png)
-->


## Build and install

On Android Studio, click on the "Run" button.

If you prefer to run on the command line, from this repository's root directory, type

```bash
./gradlew :driver-pca9685:pca9685-sample:installDebug
adb shell am start com.xujiaao.android.things.driver.sample/.Pca9685Activity
```

If you have everything set up correctly, you will see the servos periodically update their position.





