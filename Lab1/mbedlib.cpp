/* Code for mbed stuff */
#include "mbedlib.h"

/* Approached functions, used for calibrating. */
float domain[MAX_FUNCTIONS + 1] = {0, 2, 6.2, 7, 7.6, 10.28};
float range[MAX_FUNCTIONS + 1] = {0, 0.0156898787014, 0.214407914083, 0.294238191139, 0.396721209605, 1};
float a[MAX_FUNCTIONS] = {0.00922009473772, 0.0474728300136, 0.0997636509611, 0.168463364824, 0.236023044057};
float b[MAX_FUNCTIONS] = {-0.00275031077403, -0.079923632001, -0.404107365588, -0.883600363055, -1.40072158895};

/* Turn the motor on for a certain duration */
void turnOn(DigitalOut *motor, float duration) {
	*motor = ON;
	wait(duration);
	*motor = OFF;
}

/* DO NOT USE */
void calibrate(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage) {
	// Turn all the way left
	while (voltage->read() > 0.0F) {
		turnOn(motorLeft, 0.2F);

		#ifdef DEBUG
		printf("Voltage = %g\r\n", voltage->read());
		#endif
	}

	// Then sample voltage function by turning in steps to the right
	float step = 0.01F;
	float max_x = 0.0;

	for (int i = 0; voltage->read() < 1.0F; i++) {
		turnOn(motorRight, step);
		max_x = i * step;
		printf("%g, %g\r\n", max_x, voltage->read());
	}

	// TODO: Actual calibration...???
	// max_x now has the max value
	// domain[MAX_FUNCTIONS] = max_x;
}

/* Makes sure that the value is between 0 and 1 inclusive. */
void setBounds(float *value) {
	if (*value < 0.0F) {
		*value = 0.0F;
	}

	if (*value > 1.0F) {
		*value = 1.0F;
	}
}

/* Go to a voltage position between 0 and 1 (actually an approximation). */
void goToVoltagePosition(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage, float voltage_position) {
	// Check whether the given voltage_position is the current voltage
	if (voltage_position == voltage->read()) {
		#ifdef DEBUG
		printf("New voltage_position %g equal to current voltage!\r\n", voltage_position);
		#endif

		return;
	}

	// Determine step size, aka duration
	float duration = 0.01;

	// Check whether the given voltage_position is left or right of the current voltage.
	if (voltage_position < voltage->read()) {
		// Turn left
		while (voltage_position < voltage->read()) {
			turnOn(motorLeft, duration);
		}
	} else {
		// Turn right
		while (voltage_position > voltage->read()) {
			turnOn(motorRight, duration);
		}
	}

	// Show the difference between the result and the voltage_position
	#ifdef DEBUG
	printf("Approached voltage_position %g by %g\r\n", voltage_position, fabsf(voltage->read() - voltage_position));
	#endif

	return;
}

/* Go to a lineair position between [0, 1] inclusive. */
void goToPosition(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage, float position) {
	// Perform bounds check
	setBounds(&position);

	// Multiply the position with the max x value
	float x_position = position * domain[MAX_FUNCTIONS];

	// Determine in which domain the given voltage x_position falls
	int index = -1;

	for (int i = 0; i < MAX_FUNCTIONS; i++) {
		// Determine the correct domain
		if (domain[i] <= x_position && x_position < domain[i + 1]) {
			index = i;
			break;
		}
	}

	// If no index found, that means its the last domain.
	if (index == -1) {
		index = MAX_FUNCTIONS - 1;
	}

	// Bounds check...
	if (index < 0 || index >= MAX_FUNCTIONS) {
		#ifdef DEBUG
		printf("DAFUQ?\r\n");
		#endif

		return;
	}

	#ifdef DEBUG
	printf("Index = %d\r\n", index);
	#endif

	// Calculate the y value (voltage, given the x_position and the correct
	// function in the correct domain.
	float voltage_position = a[index] * x_position + b[index];

	// Check the bounds of the voltage_position
	setBounds(&voltage_position);

	#ifdef DEBUG
	printf("Voltage position = %g\r\n", voltage_position);
	#endif

	// Then go to the voltage position
	goToVoltagePosition(motorLeft, motorRight, voltage, voltage_position);
}

/*
	Returns a lineair position value in the range [0, 1] inclusive, based on
	the current position, measured by the voltage.
*/
float getPosition(AnalogIn *voltage) {
	// Read current voltage
	float voltage_position = voltage->read();

	// Check the bounds of the voltage position
	setBounds(&voltage_position);

	// Given the voltage position, determine in which range of functions it falls
	int index = -1;

	for (int i = 0; i < MAX_FUNCTIONS; i++) {
		// If the voltage_position is less than the range in the current one, that
		// means that the voltage_position belongs to the previous range.
		if (range[i] <= voltage_position && voltage_position < range[i + 1]) {
			index = i;
			break;
		}
	}

	// If no index found, that means its the last range.
	if (index == -1) {
		index = MAX_FUNCTIONS - 1;
	}

	// Bounds check...
	if (index < 0 || index >= MAX_FUNCTIONS) {
		#ifdef DEBUG
		printf("DAFUQ?\r\n");
		#endif

		return -1;
	}

	#ifdef DEBUG
	printf("Index = %d\r\n", index);
	#endif

	// Calculate the x value given the voltage_position and the correct function
	// in the correct range
	float x = (voltage_position - b[index]) / a[index];

	#ifdef DEBUG
	printf("x value = %g, position = %g\r\n", x, x / domain[MAX_FUNCTIONS]);
	#endif

	// Return a position in the range [0, 1] inclusive.
	return x / domain[MAX_FUNCTIONS];
}
