# Android Things user-space drivers [![Build Status](https://travis-ci.org/xujiaao/androidthings-drivers.svg?branch=master)](https://travis-ci.org/xujiaao/androidthings-drivers)

Sample peripheral drivers for Android Things.

NOTE: these drivers are not production-ready. They are offered as sample implementations of Android Things user space 
drivers for common peripherals as part of the Developer Preview release. There is no guarantee of correctness, 
completeness or robustness.


## How to use a driver

For your convenience, drivers in this repository are also published to JFrog Bintray<!-- JCenter --> as Maven artifacts. Look at their 
artifact and group ID in their build.gradle and add them as dependencies to your own project.

For example, to use the `pca9685` driver, version `0.1`, simply add the line below to your project's `build.gradle`:

```
repositories {
    maven {
        url 'https://bintray.com/xujiaao/android-things'
    }
}

dependencies {
    implementation 'com.xujiaao.android.things:driver-pca9685:0.1'
}
```


## Current contrib drivers

<!-- DRIVER_LIST_START -->
Driver | Type | Usage (add to your gradle dependencies) | Note
:---:|:---:| --- | ---
[driver-pca9685](driver-pca9685/pca9685) | Adafruit 16-Channel 12-bit PWM/Servo Driver | `implementation 'com.xujiaao.android.things:driver-pca9685:0.1'` |  [sample](driver-pca9685/pca9685-sample) [changelog](driver-pca9685/pca9685/CHANGELOG.md)
<!-- DRIVER_LIST_END -->


## License

Copyright 2018 Xujiaao.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file 
distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you 
under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
specific language governing permissions and limitations under the License.
