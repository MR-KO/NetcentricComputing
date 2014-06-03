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

void calibrate(DigitalOut *motor) {
	// Turn all the way left
	while (voltage.read() > 0.0F) {
		turn(&ml, 0.2F);
	}

	// Then sample voltage function by turning in steps to the right
	float step = 0.01F;

	for (int i = 0; voltage.read() < 1.0F; i++) {
		turn(&mr, step);
		printf("%g, %g\r\n", i * step, voltage.read());
	}
}

int main() {
	// printf("Press 1 to turn the servo left, and 2 for right.\r\n");
	// printf("Initial test: ml = %d, mr = %d.\r\n", ml.read(), mr.read());

	// while(1) {
	// 	switch(pc.getc()) {
	// 		case '1':
	// 			turn(&ml, 0.1);
	// 			printf("Turned left\r\n");
	// 			printf("Voltage = %g\r\n", voltage.read());
	// 			break;
	// 		case '2':
	// 			turn(&mr, 0.1);
	// 			printf("Turned right\r\n");
	// 			printf("Voltage = %g\r\n", voltage.read());
	// 			break;
	// 		default:
	// 			printf("left read = %g\r\n", left.read());
	// 			printf("right read = %g\r\n", right.read());
	// 			break;
	// 	}
	// }
}