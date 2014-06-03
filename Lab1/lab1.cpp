#include "mbed.h"

#define ON 1
#define OFF 0

Serial pc(USBTX, USBRX);

// For the servo, left turn and right turn
DigitalOut ml(p18);
DigitalOut mr(p15);

// For the 2 turning things on the board
AnalogIn left(p19);
AnalogIn right(p20);

// For measuring the voltage on the servo, such that we can determine the angle
AnalogIn voltage(p16);

void turn(DigitalOut *motor, float duration) {
	*motor = ON;
	wait(duration);
	*motor = OFF;
}

void calibrate(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage) {
	// Turn all the way left
	while (voltage->read() > 0.0F) {
		turn(motorLeft, 0.2F);
	}

	// Then sample voltage function by turning in steps to the right
	float step = 0.01F;

	for (int i = 0; voltage->read() < 1.0F; i++) {
		turn(motorRight, step);
		printf("%g, %g\r\n", i * step, voltage->read());
	}
}

// position should be a voltage value between 0 and 1
void goTo(DigitalOut *motorLeft, DigitalOut *motorRight, AnalogIn *voltage, float position) {
	// Check whether the given position is the current voltage
	if (position == voltage->read()) {
		printf("New position %g equal to current voltage!\r\n", position);
		return;
	}

	// Determine step size, aka duration
	float duration = 0.01;

	// Check whether the given position is left or right of the current voltage.
	if (position < voltage->read()) {
		// Turn left
		while (position < voltage->read()) {
			turn(motorLeft, duration);
		}
	} else {
		// Turn right
		while (position > voltage->read()) {
			turn(motorRight, duration);
		}
	}

	// Show the difference between the result and the position
	printf("Approached by %g\r\n", );
	return
}


// - Inverse van een logaritmische functie die weer lineair wordt dan
// - Benaderen met een aantal lineaire functiess


int main() {
	printf("Press 1 to turn the servo left, and 2 for right.\r\n");
	printf("Initial test: ml = %d, mr = %d.\r\n", ml.read(), mr.read());

	while(1) {
		switch(pc.getc()) {
			case '1':
				turn(&ml, 0.1);
				printf("Turned left\r\n");
				printf("Voltage = %g\r\n", voltage.read());
				break;
			case '2':
				turn(&mr, 0.1);
				printf("Turned right\r\n");
				printf("Voltage = %g\r\n", voltage.read());
				break;
			default:
				// printf("left read = %g\r\n", left.read());
				// printf("right read = %g\r\n", right.read());
				printf("Voltage = %g\r\n", voltage.read());
				break;
		}
	}
}