#!/bin/bash
mvn verify
echo ""
echo ""
echo ""
java -jar target/LogicReporter-0.0.1-SNAPSHOT.jar -v -v -v -s ../picoprobe-OK.sal
