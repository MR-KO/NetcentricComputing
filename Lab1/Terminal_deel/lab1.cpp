#include "mbed.h"
#include "mbedlib.h"

Serial pc(USBTX, USBRX);

// For the servo, left turn and right turn
DigitalOut ml(p18);
DigitalOut mr(p15);

// For the 2 turning things on the board
AnalogIn left(p19);
AnalogIn right(p20);

// For measuring the voltage on the servo, such that we can determine the angle
AnalogIn voltage(p16);


int main() {
	printf("Press 1 to turn the servo left, 2 for right, q for quit.\r\n");
	printf("Initial test: ml = %d, mr = %d.\r\n", ml.read(), mr.read());

	bool stop = false;

	while(!stop) {
		switch(pc.getc()) {
			case '1':
				turnOn(&ml, 0.1);
				printf("Turned left\r\n");
				printf("Voltage = %g\r\n", voltage.read());
				break;
			case '2':
				turnOn(&mr, 0.1);
				printf("Turned right\r\n");
				printf("Voltage = %g\r\n", voltage.read());
				break;
			case '3':
				goToVoltagePosition(&ml, &mr, &voltage, 0.5F);
				break;
			case '4':
				goToVoltagePosition(&ml, &mr, &voltage, 0.25F);
				break;
			case '5':
				goToVoltagePosition(&ml, &mr, &voltage, 0.75F);
				break;
			case 'c':
				printf("Calibrating...\r\n");
				calibrate(&ml, &mr, &voltage);
				printf("Calibrating done!\r\n");
				break;

			case '8':
				printf("Going to position 0.25 ...\r\n");
				goToPosition(&ml, &mr, &voltage, 0.25);
				printf("Done\r\n");
				getPosition(&voltage);
				break;
			case '9':
				printf("Going to position 0.5 ...\r\n");
				goToPosition(&ml, &mr, &voltage, 0.5);
				printf("Done\r\n");
				getPosition(&voltage);
				break;
			case '0':
				printf("Going to position 0.75 ...\r\n");
				goToPosition(&ml, &mr, &voltage, 0.75);
				printf("Done\r\n");
				getPosition(&voltage);
				break;
			case '-':
				printf("Going to position 1 ...\r\n");
				goToPosition(&ml, &mr, &voltage, 1);
				printf("Done\r\n");
				getPosition(&voltage);
				break;
			case '=':
				printf("Going to position 0 ...\r\n");
				goToPosition(&ml, &mr, &voltage, 0);
				printf("Done\r\n");
				getPosition(&voltage);
				break;

			case 'g':
				printf("GetPosition = %g\r\n", getPosition(&voltage));
				break;

			case 'q':
				stop = true;
				break;
			default:
				// printf("left read = %g\r\n", left.read());
				// printf("right read = %g\r\n", right.read());
				printf("Voltage = %g\r\n", voltage.read());
				break;
		}
	}

	printf("Goodbye\r\n");
}