package com.drejkim.androidthings.peripheralio;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.security.Key;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";
    // 'A' Button on i.MX7D
    private static final String BUTTON_PIN_NAME = "GPIO6_IO14";
    // Red LED on i.MX7D
    private static final String LED_PIN_NAME = "GPIO2_IO02";

    // Driver for the GPIO button
    private ButtonInputDriver mButtonInputDriver;
    // GPIO connection to LED output
    private Gpio mLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        try {
            // Initialize button driver to emit SPACE key events
            mButtonInputDriver = new ButtonInputDriver(
                    BUTTON_PIN_NAME,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_SPACE
            );
            mButtonInputDriver.register();

            // Open GPIO connection for LED
            mLedGpio = pioManager.openGpio(LED_PIN_NAME);
            // Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.w(TAG,"Error opening GPIO", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the driver and close
        if (mButtonInputDriver != null) {
            mButtonInputDriver.unregister();
            try {
                mButtonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            }
        }

        // Close the LED
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing GPIO", e);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn on the LED
            setLedValue(true);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn off the LED
            setLedValue(false);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Update the value of the LED output.
     */
    private void setLedValue(boolean value) {
        try {
            mLedGpio.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }
}
