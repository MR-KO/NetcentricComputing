# This file was automagically generated by mbed.org. For more information, 
# see http://mbed.org/handbook/Exporting-to-GCC-ARM-Embedded
# Modified by Ilja Kamps and Robin de Vries for the Net-centric computing course

PROJECT = helloworld
OBJECTS = $(patsubst %.cpp,%.o,$(wildcard *.cpp)) 
SYS_OBJECTS = ./mbed/LPC1768/GCC_ARM/cmsis_nvic.o ./mbed/LPC1768/GCC_ARM/system_LPC17xx.o ./mbed/LPC1768/GCC_ARM/core_cm3.o ./mbed/LPC1768/GCC_ARM/startup_LPC17xx.o 
INCLUDE_PATHS = -I. -I./mbed -I./mbed/LPC1768 -I./mbed/LPC1768/GCC_ARM 
LIBRARY_PATHS = -L./mbed/LPC1768/GCC_ARM 
LIBRARIES = -lmbed 
LINKER_SCRIPT = ./mbed/LPC1768/GCC_ARM/LPC1768.ld

############################################################################### 
AS      = arm-none-eabi-as
CC      = arm-none-eabi-gcc
CPP     = arm-none-eabi-g++
LD      = arm-none-eabi-gcc
OBJCOPY = arm-none-eabi-objcopy

CPU = -mcpu=cortex-m3 -mthumb
CC_FLAGS = $(CPU) -c -Os -fno-common -fmessage-length=0 -Wall -fno-exceptions -ffunction-sections -fdata-sections 
CC_SYMBOLS = -DTARGET_LPC1768 -DTOOLCHAIN_GCC_ARM -DNDEBUG -D__CORTEX_M3

LD_FLAGS = -mcpu=cortex-m3 -mthumb -Wl,--gc-sections --specs=nano.specs -u _printf_float -u _scanf_float
LD_SYS_LIBS = -lstdc++ -lsupc++ -lm -lc -lgcc -lnosys

all: $(PROJECT).bin

clean:
	rm -f $(PROJECT).bin $(PROJECT).elf $(OBJECTS)

.s.o:
	$(AS) $(CPU) -o $@ $<

.c.o:
	$(CC)  $(CC_FLAGS) $(CC_SYMBOLS) -std=gnu99   $(INCLUDE_PATHS) -o $@ $<

.cpp.o:
	$(CPP) $(CC_FLAGS) $(CC_SYMBOLS) -std=gnu++98 $(INCLUDE_PATHS) -o $@ $<


$(PROJECT).elf: $(OBJECTS) $(SYS_OBJECTS)
	$(LD) $(LD_FLAGS) -T$(LINKER_SCRIPT) $(LIBRARY_PATHS) -o $@ $^ $(LIBRARIES) $(LD_SYS_LIBS) $(LIBRARIES) $(LD_SYS_LIBS)

$(PROJECT).bin: $(PROJECT).elf
	$(OBJCOPY) -O binary $< $@
