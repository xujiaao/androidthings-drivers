# PCA9685 driver for Android Things

[![](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/xujiaao/android/things/driver-pca9685/maven-metadata.xml.svg)](http://jcenter.bintray.com/com/xujiaao/android/things/driver-pca9685/maven-metadata.xml)

This driver supports [PCA9685](https://www.adafruit.com/product/815) (Adafruit 16-Channel 12-bit PWM/Servo Driver).

<img src="https://cdn-shop.adafruit.com/1200x900/815-04.jpg" width="540px" height="auto" />


## How to use the driver

### Gradle dependency

To use the `pca9685` driver, simply add the line below to your project's `build.gradle`, where `<version>` matches the 
last version of the driver available on [jcenter][jcenter].

````
dependencies {
    implementation 'com.xujiaao.android.things:driver-pca9685:<version>'
}
````


### Sample usage

````java
import com.xujiaao.android.things.driver.pca9685.Pca9685;

// Create servo specifications:

Pca9685.ServoSpec mServoSpec0 = new Pca9685.ServoSpec(0)
        .setPulseDurationRange(1D, 2D)
        .setAngleRange(0D, 180D);

Pca9685.ServoSpec mServoSpec1 = new Pca9685.ServoSpec(1)
        .setPulseDurationRange(1D, 2D)
        .setAngleRange(0D, 180D);

// Access the driver:

Pca9685 mPca9685;

try {
    mPca9685 = new Pca9685(i2cBusName);
} catch (IOException e) {
    // couldn't configure the driver...
}

// Make the servos move:

try {
    mPca9685.setPwmSpec(mServoSpec0.setAngle(90D));
    mPca9685.setPwmSpec(mServoSpec1.setAngle(90D));

    // or

    mServoSpec0.setAngle(90D).apply(mPca9685);
    mServoSpec1.setAngle(90D).apply(mPca9685);
} catch (IOException e) {
    // error setting servos
}

// Close the driver when finished:

try {
    mPca9685.close();
} catch (IOException e) {
    // error closing driver
}
````


[jcenter]: https://bintray.com/xujiaao/android-things/driver-pca9685/_latestVersion
