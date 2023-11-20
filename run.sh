#!/bin/bash
mvn verify
echo ""
echo ""
echo ""
java -jar target/LogicReporter-0.0.1-SNAPSHOT.jar -v -v -v -swdio digital_5.bin -swclk digital_4.bin
