/* Header for various mbed functions. */
#ifndef MBEDLIB_H
#define MBEDLIB_H

/* Approached functions, used for calibrating. */
#define MAX_FUNCTIONS 5



/* Turn the motor on for a certain duration */
void turnOn(DigitalOut *motor, float duration);

/* DO NOT USE */
void calibrate(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage);

/* Makes sure that the value is between 0 and 1 inclusive. */
void setBounds(float *value);

/* Go to a voltage position between 0 and 1 (actually an approximation). */
void goToVoltagePosition(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage, float voltage_position);

/* Go to a lineair position between [0, 1] inclusive. */
void goToPosition(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage, float position);

/*
	Returns a lineair position value in the range [0, 1] inclusive, based on
	the current position, measured by the voltage.
*/
float getPosition(AnalogIn *voltage);

#endif