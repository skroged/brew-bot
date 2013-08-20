/***********************************************************************
 * mcp3008SpiTest.cpp. Sample program that tests the mcp3008Spi class.
 * an mcp3008Spi class object (a2d) is created. the a2d object is instantiated
 * using the overloaded constructor. which opens the spidev0.0 device with 
 * SPI_MODE_0 (MODE 0) (defined in linux/spi/spidev.h), speed = 1MHz &
 * bitsPerWord=8.
 * 
 * call the spiWriteRead function on the a2d object 20 times. Each time make sure
 * that conversion is configured for single ended conversion on CH0
 * i.e. transmit ->  byte1 = 0b00000001 (start bit)
 *                   byte2 = 0b1000000  (SGL/DIF = 1, D2=D1=D0=0)
 *                   byte3 = 0b00000000  (Don't care)
 *      receive  ->  byte1 = junk
 *                   byte2 = junk + b8 + b9
 *                   byte3 = b7 - b0
 *     
 * after conversion must merge data[1] and data[2] to get final result 
 * 
 * 
 * 
 * *********************************************************************/
#include "HoneywellPressureSpi.h"
 
using namespace std;
 
int main(void)
{
    HoneywellPressureSpi sensor1("/dev/spidev0.0", SPI_MODE_0, 1000000, 8);
	HoneywellPressureSpi sensor2("/dev/spidev0.1", SPI_MODE_0, 1000000, 8);
    int i = 20;            
    unsigned char data[4];
	float topPart, bottomPart, psi1, psi2;
	unsigned int b1, b2, pressureHexCount, countMax, countMin;

	countMax = 16383;
	countMin = 0;

	bottomPart = countMax-countMin;

    while(true)
    { 
        sensor1.spiWriteRead(data, sizeof(data) );

        sleep(1);

		b1 = (data[0] << 8 ) & 0b0011111111111111;
		b2 = data[1];

		pressureHexCount = b1 | b2;

		//topPart = pressureHexCount - countMin;		

		psi1 = pressureHexCount;//topPart / bottomPart;
        	

		sensor2.spiWriteRead(data, sizeof(data) );       

		b1 = (data[0] << 8 ) & 0b0011111111111111;
		b2 = data[1];

		pressureHexCount = b1 | b2;

		//topPart = pressureHexCount - countMin;	

		psi2 = pressureHexCount;//topPart / bottomPart;
		    
		cout << "{\"BK_VOLUME\":" << psi1 << ",\"HLT_VOLUME\":" << psi2 << "}" << endl;
		

        i--;
    }
    return 0;
}