#include "mbed.h"

#define LED_OFF 0
#define LED_ON 1
#define PAUSE 0.15

int main() {
	DigitalOut led1(LED1);
	DigitalOut led2(LED2);
	DigitalOut led3(LED3);
	DigitalOut led4(LED4);

	unsigned int i = 0;

	printf("U MAD FGT\n");

	while(1) {
		led1 = LED_ON;
		wait(PAUSE);
		led2 = LED_ON;
		wait(PAUSE);
		led3 = LED_ON;
		wait(PAUSE);
		led4 = LED_ON;
		wait(PAUSE);

		led1 = LED_OFF;
		wait(PAUSE);
		led2 = LED_OFF;
		wait(PAUSE);
		led3 = LED_OFF;
		// wait(PAUSE);

		// And back...
		wait(0.5 * PAUSE);
		led4 = LED_ON;
		wait(PAUSE);
		led3 = LED_ON;
		wait(PAUSE);
		led2 = LED_ON;
		wait(PAUSE);
		led1 = LED_ON;
		wait(PAUSE);

		led4 = LED_OFF;
		wait(PAUSE);
		led3 = LED_OFF;
		wait(PAUSE);
		led2 = LED_OFF;
		wait(0.5 * PAUSE);

		printf("U MAD FGT\n");
	}
}
