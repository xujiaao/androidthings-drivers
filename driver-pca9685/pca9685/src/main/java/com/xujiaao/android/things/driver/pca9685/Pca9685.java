package com.xujiaao.android.things.driver.pca9685;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Android Things driver for 'Adafruit 16-Channel 12-bit PWM/Servo Driver'.
 *
 * @see <a href="https://github.com/adafruit/Adafruit-PWM-Servo-Driver-Library">Adafruit PCA9685 PWM Servo Driver Library</a>
 */
public class Pca9685 implements AutoCloseable {

    /**
     * Default I2C address for the Pca9685 PWM driver chip.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_I2C_ADDRESS = 0x40;

    /**
     * Default PWM frequency for the entire chip.
     */
    @SuppressWarnings("WeakerAccess")
    public static final double DEFAULT_FREQUENCY_HZ = 50D;

    private static final int REG_PCA9685_MODE1 = 0x00;
    private static final int REG_PCA9685_PRESCALE = 0xFE;

    private static final int REG_LED0_ON_L = 0x06;
    private static final int REG_LED0_ON_H = 0x07;
    private static final int REG_LED0_OFF_L = 0x08;
    private static final int REG_LED0_OFF_H = 0x09;

    private double mPwmFrequencyHz;

    private I2cDevice mDevice;

    /**
     * Create a new Pca9685 chip driver connected on the given bus.
     *
     * @param bus I2C bus the chip is connected to.
     */
    @SuppressWarnings("unused")
    public Pca9685(String bus) throws IOException {
        this(bus, DEFAULT_I2C_ADDRESS);
    }

    /**
     * Create a new Pca9685 chip driver connected on the given bus and address.
     *
     * @param bus     I2C bus the chip is connected to.
     * @param address I2C address of the chip.
     */
    @SuppressWarnings("WeakerAccess")
    public Pca9685(String bus, int address) throws IOException {
        this(bus, address, DEFAULT_FREQUENCY_HZ);
    }

    /**
     * Create a new Pca9685 chip driver connected on the given bus and address and uses the specified frequency.
     *
     * @param bus         I2C bus the chip is connected to.
     * @param address     I2C address of the chip.
     * @param frequencyHz the frequency in Hertz.
     */
    @SuppressWarnings("WeakerAccess")
    public Pca9685(String bus, int address, double frequencyHz) throws IOException {
        final PeripheralManagerService pioService = new PeripheralManagerService();
        final I2cDevice device = pioService.openI2cDevice(bus, address);

        try {
            connect(device, frequencyHz);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }

            throw e;
        }
    }

    private void connect(I2cDevice device, double frequencyHz) throws IOException {
        mDevice = device;

        reset();

        setPwmFrequencyHz(frequencyHz);
    }

    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            IOException exception = null;

            try {
                reset();
            } catch (IOException e) {
                exception = e;
            }

            try {
                mDevice.close();
            } catch (IOException e) {
                exception = e;
            } finally {
                mDevice = null;
            }

            if (exception != null) {
                throw exception;
            }
        }
    }

    private void reset() throws IOException {
        final I2cDevice device = mDevice;
        if (device != null) {
            mDevice.writeRegByte(REG_PCA9685_MODE1, (byte) 0x80);
        }
    }

    /**
     * Gets the PWM frequency for the entire chip.
     */
    @SuppressWarnings("WeakerAccess")
    public double getPwmFrequencyHz() {
        return mPwmFrequencyHz;
    }

    /**
     * Sets the PWM frequency for the entire chip, up to ~1.6 KHz.
     *
     * @param frequencyHz Frequency in Hertz to use for the signal. Must be positive.
     */
    @SuppressWarnings("WeakerAccess")
    public void setPwmFrequencyHz(double frequencyHz) throws IOException {
        final I2cDevice device = mDevice;
        if (device == null) {
            throw new IllegalStateException("I2C device not opened");
        }

        mPwmFrequencyHz = frequencyHz;

        /*
         * Correct for overshoot in the frequency setting.
         * (See issue: https://github.com/adafruit/Adafruit-PWM-Servo-Driver-Library/issues/11)
         */
        frequencyHz *= .9;

        double prescale = 25000000D; // 25MHz.
        prescale /= 4096D;           // 12-bit.
        prescale /= frequencyHz;
        prescale -= 1D;

        final byte oldMode = device.readRegByte(REG_PCA9685_MODE1);
        final byte newMode = (byte) ((oldMode & 0x7F) | 0x10); // sleep.

        device.writeRegByte(REG_PCA9685_MODE1, newMode); // go to sleep.
        device.writeRegByte(REG_PCA9685_PRESCALE, (byte) Math.round(prescale)); // set the prescale.
        device.writeRegByte(REG_PCA9685_MODE1, oldMode);

        try {
            Thread.sleep(5L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return;
        }

        // Sets the MODE1 register to turn on auto increment.
        device.writeRegByte(REG_PCA9685_MODE1, (byte) (oldMode | 0xA0));
    }

    /**
     * Sets the PWM output of one of the Pca9685 pins.
     *
     * @param channel One of the PWM output pins, from 0 to 15
     * @param on      At what point in the 4096-part cycle to turn the PWM output ON
     * @param off     At what point in the 4096-part cycle to turn the PWM output OFF
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public void setPWM(int channel, int on, int off) throws IOException {
        final I2cDevice device = mDevice;
        if (device == null) {
            throw new IllegalStateException("I2C device not opened");
        }

        final int offset = 4 * channel;
        device.writeRegByte(REG_LED0_ON_L + offset, (byte) (on & 0xFF));
        device.writeRegByte(REG_LED0_ON_H + offset, (byte) (on >> 8));
        device.writeRegByte(REG_LED0_OFF_L + offset, (byte) (off & 0xFF));
        device.writeRegByte(REG_LED0_OFF_H + offset, (byte) (off >> 8));
    }

    /**
     * Helper to set PWM with duty cycle.
     */
    @SuppressWarnings("WeakerAccess")
    public void setPwmDutyCycle(int channel, double dutyCycle) throws IOException {
        final int value = Math.max(Math.min((int) (dutyCycle * 4095D / 100D), 4095), 0);

        setPWM(channel, 0, value);
    }

    /**
     * Helper to set PWM with {@link PwmSpec}.
     */
    @SuppressWarnings("unused")
    public void setPwmSpec(PwmSpec spec) throws IOException {
        if (spec != null) {
            spec.apply(this);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // PwmSpec
    // -----------------------------------------------------------------------------------------------------------------

    public interface PwmSpec {

        void apply(Pca9685 pwm) throws IOException;
    }

    /**
     * Servo Spec.
     *
     * @see <a href="https://github.com/androidthings/contrib-drivers/blob/master/pwmservo/src/main/java/com/google/android/things/contrib/driver/pwmservo/Servo.java">Servo driver for Android Things</a>
     */
    @SuppressWarnings("unused")
    public static class ServoSpec implements PwmSpec {

        private static final double DEFAULT_CHANNEL = 1;

        private static final double DEFAULT_MIN_PULSE_DURATION_MS = 1;
        private static final double DEFAULT_MAX_PULSE_DURATION_MS = 2;

        private static final double DEFAULT_MIN_ANGLE_DEG = 0.0;
        private static final double DEFAULT_MAX_ANGLE_DEG = 180.0;

        private int mChannel;

        private double mMinPulseDuration = DEFAULT_MIN_PULSE_DURATION_MS; // milliseconds.
        private double mMaxPulseDuration = DEFAULT_MAX_PULSE_DURATION_MS; // milliseconds.

        private double mMinAngle = DEFAULT_MIN_ANGLE_DEG; // degrees.
        private double mMaxAngle = DEFAULT_MAX_ANGLE_DEG; // degrees.

        private double mAngle = mMinAngle;

        /**
         * Create a Servo Spec with default settings.
         */
        public ServoSpec() {
        }

        /**
         * Create a Servo Spec use the specified channel.
         */
        public ServoSpec(int channel) {
            mChannel = channel;
        }

        @Override
        public void apply(Pca9685 pwm) throws IOException {
            if (pwm == null) {
                return;
            }

            // normalize angle ratio.
            final double t = (mAngle - mMinAngle) / (mMaxAngle - mMinAngle);

            // linearly interpolate angle between servo ranges to get a pulse width in milliseconds.
            final double pw = mMinPulseDuration + (mMaxPulseDuration - mMinPulseDuration) * t;

            // convert the pulse width into a percentage of the mPeriod of the wave form.
            final double dutyCycle = pw * pwm.getPwmFrequencyHz() * 100D / 1000D;

            pwm.setPwmDutyCycle(mChannel, dutyCycle);
        }

        /**
         * Sets the channel id.
         */
        public ServoSpec setChannel(int channel) {
            mChannel = channel;

            return this;
        }

        /**
         * Set the pulse duration range. These determine the duty cycle range, where {@code minMs} corresponds to the
         * minimum angle value and {@code maxMs} corresponds to the maximum angle value.
         *
         * @param minMs the minimum pulse duration in milliseconds
         * @param maxMs the maximum pulse duration in milliseconds
         * @throws IllegalArgumentException if minMs is not less than maxMs or if minMs < 0
         */
        public ServoSpec setPulseDurationRange(double minMs, double maxMs) throws IOException {
            if (minMs >= maxMs) {
                throw new IllegalArgumentException("MinMs must be less than maxMs");
            }

            if (minMs < 0) {
                throw new IllegalArgumentException("MinMs must be greater than 0");
            }

            mMinPulseDuration = minMs;
            mMaxPulseDuration = maxMs;

            return this;
        }

        /**
         * Gets the current minimum pulse duration.
         */
        public double getMinimumPulseDuration() {
            return mMinPulseDuration;
        }

        /**
         * @return the current maximum pulse duration
         */
        public double getMaximumPulseDuration() {
            return mMaxPulseDuration;
        }

        /**
         * Set the range of angle values the servo accepts. If the servo is enabled and its current
         * position is outside this range, it will update its position to the new minimum or maximum,
         * whichever is closest.
         *
         * @param minAngle the minimum angle in degrees
         * @param maxAngle the maximum angle in degrees
         * @throws IllegalArgumentException if minAngle is not less than maxAngle
         */
        public ServoSpec setAngleRange(double minAngle, double maxAngle) throws IOException {
            if (minAngle >= maxAngle) {
                throw new IllegalArgumentException("MinAngle must be less than maxAngle");
            }

            mMinAngle = minAngle;
            mMaxAngle = maxAngle;

            return setAngle(mAngle);
        }

        /**
         * Gets the minimum angle in degrees.
         */
        public double getMinimumAngle() {
            return mMinAngle;
        }

        /**
         * Gets the maximum angle in degrees.
         */
        public double getMaximumAngle() {
            return mMaxAngle;
        }

        /**
         * Set the angle position. If this servo is enabled, it will update its position immediately.
         *
         * @param angle the angle position in degrees.
         */
        @SuppressWarnings("WeakerAccess")
        public ServoSpec setAngle(double angle) throws IOException {
            if (angle < mMinAngle) {
                mAngle = mMinAngle;
            } else if (angle > mMaxAngle) {
                mAngle = mMaxAngle;
            } else {
                mAngle = angle;
            }

            return this;
        }

        /**
         * Gets the current angle in degrees.
         */
        public double getAngle() {
            return mAngle;
        }
    }
}