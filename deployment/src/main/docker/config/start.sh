#!/bin/bash
for e in $(sudo cat /proc/1/environ | tr "\000" "\n")
do
    eval "export $e"
done

java -Dlogging.config=logback.xml -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar cnn-core-${project.version}.jar --spring.config.location=application.properties
