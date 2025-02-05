#!/bin/bash
mvn verify && echo "" && echo "" && echo "" && time java -jar target/LogicReporter-0.0.1-SNAPSHOT.jar -mode 0 -clk digital_3.bin -ncs digital_2.bin -miso digital_4.bin -mosi digital_5.bin
#-v -v -v -swdio digital_5.bin -swclk digital_4.bin -regtrans arm_cortex_m_registers.txt -regtrans rp2040_regtrans.txt > log.txt
