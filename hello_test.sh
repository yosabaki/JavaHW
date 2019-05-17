#!/usr/bin/env bash
MY_CLASS_PATH=src/ru/ifmo/rain/ustinov
GOSHA_CLASS_PATH=artifacts/info.kgeorgiy.java.advanced.hello.jar
mkdir run
cp ./lib/* run
javac -d ./run/ -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ $MY_CLASS_PATH/hello/*.java
#java -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ -m info.kgeorgiy.java.advanced.mapper $1 ru.ifmo.rain.ustinov.mapper.ParallelMapperImpl,ru.ifmo.rain.ustinov.concurrent.IterativeParallelism $2

java -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ -m info.kgeorgiy.java.advanced.hello client-i18n ru.ifmo.rain.ustinov.hello.HelloUDPClient "$1"
java -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ -m info.kgeorgiy.java.advanced.hello server-i18n ru.ifmo.rain.ustinov.hello.HelloUDPServer "$1"
rm -rf run
