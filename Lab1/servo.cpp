#include "mbed.h"
#include "Servo.h"

#define ON 1
#define OFF 0

Serial pc(USBTX, USBRX);

DigitalOut ml(p18);
DigitalOut mr(p15);

void turn(DigitalOut *motor, float duration) {
	*motor = ON;
	wait(duration);
	*motor = OFF;
}

int main() {
	printf("Press 1 to turn the servo left, and 2 for right.\r\n");
	printf("Initial test: ml = %d, mr = %d.\r\n", ml.read(), mr.read());

	while(1) {
		switch(pc.getc()) {
			case '1': turn(&ml, 0.05); printf("Turned left\r\n"); break;
			case '2': turn(&mr, 0.05); printf("Turned right\r\n"); break;
			default: break;
		}
	}
}